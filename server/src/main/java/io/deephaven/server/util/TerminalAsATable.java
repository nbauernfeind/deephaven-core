package io.deephaven.server.util;

import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import io.deephaven.chunk.LongChunk;
import io.deephaven.chunk.WritableChunk;
import io.deephaven.chunk.WritableLongChunk;
import io.deephaven.chunk.attributes.Values;
import io.deephaven.configuration.Configuration;
import io.deephaven.engine.rowset.RowSet;
import io.deephaven.engine.rowset.RowSetFactory;
import io.deephaven.engine.rowset.RowSetShiftData;
import io.deephaven.engine.rowset.WritableRowSet;
import io.deephaven.engine.rowset.chunkattributes.RowKeys;
import io.deephaven.engine.table.ChunkSink;
import io.deephaven.engine.table.ChunkSource;
import io.deephaven.engine.table.ColumnSource;
import io.deephaven.engine.table.impl.QueryTable;
import io.deephaven.engine.table.impl.TableUpdateImpl;
import io.deephaven.engine.table.impl.perf.PerformanceEntry;
import io.deephaven.engine.table.impl.perf.UpdatePerformanceTracker;
import io.deephaven.engine.table.impl.sources.ArrayBackedColumnSource;
import io.deephaven.engine.table.impl.sources.RedirectedColumnSource;
import io.deephaven.engine.table.impl.util.WritableRowRedirection;
import io.deephaven.engine.updategraph.LogicalClock;
import io.deephaven.engine.updategraph.NotificationQueue;
import io.deephaven.engine.updategraph.UpdateGraphProcessor;
import io.deephaven.engine.updategraph.UpdateSourceRegistrar;
import io.deephaven.extensions.barrage.BarrageStreamGenerator;
import io.deephaven.extensions.barrage.BarrageStreamGeneratorImpl;
import io.deephaven.internal.log.LoggerFactory;
import io.deephaven.io.log.LogLevel;
import io.deephaven.io.logger.LogBuffer;
import io.deephaven.io.logger.LogBufferRecord;
import io.deephaven.io.logger.Logger;
import io.deephaven.server.barrage.BarrageMessageProducer;
import io.deephaven.server.console.ScopeTicketResolver;
import io.deephaven.time.DateTime;
import io.deephaven.time.DateTimeUtils;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TerminalAsATable extends QueryTable implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TerminalAsATable.class);

    /**
     * This table will try to update much more frequently than a typical LTM table. This simply means that users will
     * receive terminal output on a much more immediate basis; whether the LTM is busy or even locked.
     */
    public static final int SHELL_MIN_UPDATE_INTERVAL =
            Configuration.getInstance().getIntegerWithDefault("console.minUpdateInterval", 50);

    @AssistedFactory
    public interface Factory {
        TerminalAsATable create(@Assisted("rows") int numRows, @Assisted("cols") int numCols);
    }

    // ANSI Controls
    /** everytime the user sends a command we prevent the cursor from reaching it */
    private long baseRow = 0;
    /** current row of the cursor */
    private int currRow = 1;
    /** current col of the cursor */
    private int currCol = 1;
    private int numRows;
    private int numCols;
    private final FontState outFontState = new FontState();
    private final FontState errFontState = new FontState();

    // Hooks into the live table monitor for when this object is actually used like a table
    private final UpdateSourceRegistrar registrar;
    private final NotificationQueue notificationQueue;
    private final PerformanceEntry refreshEntry;
    private final Scheduler scheduler;

    /** contains the majority of our table's state */
    private final TableAccess data;

    /** represents which rows in writable source exist but are not mapped to any parent rows */
    private long nextOuterIndex = 0;
    private WritableRowSet freeset = RowSetFactory.empty();

    /**
     * This lock is held by any writer to ShellTable; this enables our off-ltm BarrageMessageProducer to still produce
     * valid snapshots.
     */
    private final Lock updateLock = new ReentrantLock();

    /**
     * This lock is held whenever we are adding or removing a mapping to an entry.
     */
    private final Lock mappingLock = new ReentrantLock();

    /**
     * We lie to the barrage message producer about the LTM; so this QueryTable references curr-only column sources.
     */
    private final QueryTable barrageTable;
    private long barrageStep = 0;
    private final BarrageMessageProducer<BarrageStreamGeneratorImpl.View> barrage;
    private final BarrageMessageProducer<BarrageStreamGeneratorImpl.View>.DeltaListener barrageListener;
    private final UpdatePropagationHelper barragePropagationHelper;

    private final WritableRowSet ltmAddPending = RowSetFactory.empty();
    private final WritableRowSet barrageAddPending = RowSetFactory.empty();
    private final WritableRowSet ltmModPending = RowSetFactory.empty();
    private final WritableRowSet barrageModPending = RowSetFactory.empty();

    /**
     * We remember which row contains the active mapping for a variable. Whenever this variable is removed, or replaced,
     * we disable the reference to the variable in the table and update the mapping as appropriate.
     */
    private final Map<String, VarRef> liveVariableMap = new HashMap<>();

    /**
     * Construct a table that represents a terminal shell window. It has rich columns to enable neat features on the UI.
     * In addition to being a QueryTable (and therefore participating as an implementation of TableOperations), the
     * ShellTable has a custom integrating with a BarrageMessageProducer so that it may propagate updates to users even
     * when the LTM is held (such as when executing a script command) and the table would not be allowed to update.
     */
    @AssistedInject
    public TerminalAsATable(final UpdateGraphProcessor updateGraphProcessor,
            final TableAccess data,
            final LogBuffer logBuffer,
            final Scheduler scheduler,
            final BarrageStreamGenerator.Factory<BarrageStreamGeneratorImpl.View> streamGeneratorFactory,
            @Assisted("rows") final int numRows,
            @Assisted("cols") final int numCols) {
        super(RowSetFactory.empty().toTracking(), data.ltmColumns);
        getModifiedColumnSetForUpdates().setAllDirty();

        this.barragePropagationHelper = new UpdatePropagationHelper(scheduler, this::onFastPropagation,
                this::onFastPropagationError, SHELL_MIN_UPDATE_INTERVAL);
        this.data = data;
        this.numRows = numRows;
        this.numCols = numCols;

        this.registrar = updateGraphProcessor;
        this.notificationQueue = updateGraphProcessor;
        this.scheduler = scheduler;

        refreshEntry = UpdatePerformanceTracker.getInstance()
                .getEntry("ShellTable refresh " + System.identityHashCode(this));

        // we always start empty, and can be notified this cycle if we receive any messages
        final long currentClockValue = LogicalClock.DEFAULT.currentValue();
        setLastNotificationStep(LogicalClock.getState(currentClockValue) == LogicalClock.State.Updating
                ? LogicalClock.getStep(currentClockValue) - 1
                : LogicalClock.getStep(currentClockValue));

        registrar.addSource(this);

        barrageTable = new QueryTable(RowSetFactory.empty().toTracking(), this.data.barrageColumns);
        barrageTable.setRefreshing(true);
        barrage = new BarrageMessageProducer<>(scheduler, streamGeneratorFactory, barrageTable,
                SHELL_MIN_UPDATE_INTERVAL, () -> barrageStep, updateLock, null);
        barrageListener = barrage.constructListener();

        // always need to have one blank row; get it before the logBuffer replay
        baseRow = appendBlankRow();
        logBuffer.subscribe(this::onLogBufferRecord);
    }

    private long acquireRow() {
        boolean needsResizing = false;
        if (data.capacity == 0) {
            data.capacity = Integer.highestOneBit(Math.max(numRows, 32));
            freeset = RowSetFactory.flat(data.capacity);
            needsResizing = true;
        } else if (freeset.isEmpty()) {
            freeset.insertRange(data.capacity, data.capacity * 2L - 1);
            data.capacity *= 2L;
            needsResizing = true;
        }

        if (needsResizing) {
            data.ensureCapacity(data.capacity);
        }

        final long result = freeset.firstRowKey();
        freeset.remove(result);
        return result;
    }

    private static final Pattern ansiPattern = Pattern.compile(""
            + "\\u001b(8|7|H|>|\\[)"
            + "((h|l|m)"
            + "|([0-2]?)(K|J)"
            + "|(\\d*)(A|B|C|D|D\\D|E|F|G|g|i|n|S|s|T|u)"
            + "|(\\d*);(\\d*)(f|H|r)"
            + "|([\\d;]+)(m))");

    private void onLogBufferRecord(final LogBufferRecord logBufferRecord) {
        // Skip debug level; better to configure logback than to ruin our ability to provide decent history
        if (logBufferRecord.getLevel().getPriority() <= LogLevel.INFO.getPriority()) {
            return;
        }

        if (logBufferRecord.getLevel() == LogLevel.STDOUT) {
            // TODO: STDERR can have ANSI codes, too
            onLogStdout(logBufferRecord);
            return;
        }

        final long innerIndex;
        final long outerIndex;

        mappingLock.lock();
        try {
            innerIndex = acquireRow();
            outerIndex = nextOuterIndex++;
            data.redirectionIndex.put(outerIndex, innerIndex);
            currRow = 1;

//            data.lastUpdateTime.currValues.set(innerIndex, logBufferRecord.getTimestampMicros() * 1000);
            data.message.currValues.set(innerIndex, ansiPattern.matcher(logBufferRecord.getDataString())
                    .replaceAll(""));
            data.logLevel.currValues.set(innerIndex, logBufferRecord.getLevel().getName());

            baseRow = appendBlankRow();
        } finally {
            mappingLock.unlock();
        }

        scheduleAddRowProp(outerIndex);
    }

    private void onLogStdout(final LogBufferRecord logBufferRecord) {
        int rawOffset = 0;
        final FontState state = logBufferRecord.getLevel() == LogLevel.STDOUT ? outFontState : errFontState;
        final String rawText = logBufferRecord.getDataString();
        final Matcher ansiMatches = ansiPattern.matcher(rawText);
        while (ansiMatches.find()) {
            doFlushTerminalOut(state, rawText.substring(rawOffset, ansiMatches.start()));
            rawOffset = ansiMatches.end();

            final int p1, p2;
            Integer[] colorParamList = null;
            String code;
            if ((code = ansiMatches.group(3)) != null) {
                // we're parameter less
                p1 = p2 = 0;
            } else if ((code = ansiMatches.group(5)) != null) {
                // we're K/J parameter
                final String v1 = ansiMatches.group(4);
                p1 = v1 == null || v1.isEmpty() ? 0 : Integer.parseInt(v1);
                p2 = 0;
            } else if ((code = ansiMatches.group(7)) != null) {
                // we're single parameter
                final String v1 = ansiMatches.group(6);
                p1 = v1 == null || v1.isEmpty() ? 0 : Integer.parseInt(v1);
                p2 = 0;
            } else if ((code = ansiMatches.group(10)) != null) {
                // we're double parameter
                final String v1 = ansiMatches.group(8);
                final String v2 = ansiMatches.group(9);
                p1 = v1 == null || v1.isEmpty() ? 0 : Integer.parseInt(v1);
                p2 = v2 == null || v2.isEmpty() ? 0 : Integer.parseInt(v2);
            } else if ((code = ansiMatches.group(12)) != null) {
                // we're color parameter
                p1 = p2 = 0;
                final String paramList = ansiMatches.group(11);
                colorParamList = Arrays.stream(paramList.split(";"))
                        .map(Integer::parseInt)
                        .toArray(Integer[]::new);
            } else {
                throw new IllegalStateException("Matched pattern, but no sub pattern; impossible!");
            }

            switch (code) {
                case "F": // prev line (column 1)
                    currCol = 1;
                    // fallthrough
                case "A": // up
                    currRow = Math.max(1, currRow - p1);
                    continue;

                case "E": // next line (column 1)
                    currCol = 1;
                    // fallthrough
                case "B": // down
                    currRow = Math.min(numRows, currRow + p1);
                    continue;

                case "C": // forward
                    currCol = Math.min(numCols, currCol + p1);
                    continue;
                case "D": // backward
                    currCol = Math.max(1, currCol - p1);
                    continue;

                case "G": // goto column
                    currCol = Math.max(1, Math.min(numCols, p1));
                    continue;

                case "J":
                    if (p1 == 0) {
                        // TODO: ERASE TO EOL
                    } else if (p1 == 1) {
                        // TODO: ERASE TO START
                    } else if (p2 == 2) {
                        // TODO: ERASE EVERYTHING
                    } else if (p2 == 3) {
                        // TODO: ERASE EVERYTHING AND SCROLLBACK BUFFER
                    }
                    continue;

                case "K":
                    if (p1 == 0) {
                        // TODO: ERASE TO EOL
                    } else if (p1 == 1) {
                        // TODO: ERASE TO BOL
                    } else if (p2 == 2) {
                        // TODO: ERASE LINE
                    }
                    continue;

                case "S":
                    for (int si = (p1 == 0) ? 1 : p1; si > 0; --si) {
                        appendBlankRow();
                    }
                    continue;

                case "m":
                    setColorMode(state, colorParamList);
                    continue;

                case "H":
                    // set cursor pos
                    currRow = Math.max(1, Math.min(numRows, p1));
                    currCol = Math.max(1, Math.min(numCols, p2));
                    continue;

                case "T":
                    // add new lines at top; lines at bottom disappear; do not move cursor
                    // Unsupported for now; would require shifting rows around which I prefer to avoid.
                    continue;

                case "h": // enable cursor
                case "l": // disable cursor
                    // could these be useful?
                    break;
            }
        }
        doFlushTerminalOut(state, rawText.substring(rawOffset));
    }

    private void doFlushTerminalOut(final FontState state, final String stringFlushed) {
        if (stringFlushed.isEmpty()) {
            return;
        }

        int numRead = 0;
        while (stringFlushed.length() > numRead) {
            int nextNewLine = stringFlushed.indexOf('\n', numRead);
            final int snipLength = (nextNewLine == -1 ? stringFlushed.length() : nextNewLine) - numRead;

            // currRow must always be mapped
            mappingLock.lock();
            long mappedRow = data.redirectionIndex.get(baseRow + currRow - 1);
            mappingLock.unlock();
            String line = data.message.currValues.get(mappedRow);

            // add blank spaces if they are needed before our msg
            if (line.length() < currCol - 1) {
                line += " ".repeat(currCol - 1 - line.length());
            }

            // capture what will remain after the snippet is placed and trim line
            String suffix = null;
            int lastColIndex = currCol - 1 + snipLength;
            if (line.length() > lastColIndex) {
                suffix = line.substring(lastColIndex);
            }
            if (line.length() > currCol) {
                line = line.substring(0, currCol - 1);
            }

            // add text color markup; overwrite pre-existing markup
            final String markup = state.getJsonPrefix();
            if (markup != null) {
                final WritableRowSet range = RowSetFactory.fromRange(currCol, lastColIndex);
                String[] markupPrefixes = data.markupPrefixes.get(mappedRow);
                WritableRowSet[] markupRowSets = data.markupRowSets.get(mappedRow);

                int numToRemove = 0;
                boolean foundMarkup = false;
                for (int ii = 0; ii < markupRowSets.length; ++ii) {
                    final WritableRowSet set = markupRowSets[ii];
                    if (markupPrefixes[ii].equals(markup)) {
                        foundMarkup = true;
                        set.insert(range);
                    } else {
                        set.remove(range);
                        if (set.isEmpty()) {
                            ++numToRemove;
                        }
                    }
                }

                if (!foundMarkup || numToRemove > 0) {
                    final int newLength = markupPrefixes.length - numToRemove + (foundMarkup ? 0 : 1);
                    final ArrayList<String> newPrefixes = new ArrayList<>(newLength);
                    final ArrayList<WritableRowSet> newColumnSets = new ArrayList<>(newLength);
                    for (int ii = 0; ii < markupPrefixes.length; ++ii) {
                        if (markupRowSets[ii].isEmpty()) {
                            continue;
                        }
                        newPrefixes.add(markupPrefixes[ii]);
                        newColumnSets.add(markupRowSets[ii]);
                    }

                    if (!foundMarkup) {
                        newPrefixes.add(markup);
                        newColumnSets.add(range);
                    }

                    markupPrefixes = newPrefixes.toArray(new String[0]);
                    markupRowSets = newColumnSets.toArray(new WritableRowSet[0]);
                    data.markupPrefixes.set(mappedRow, markupPrefixes);
                    data.markupRowSets.set(mappedRow, markupRowSets);
                }

                if (foundMarkup) {
                    range.close();
                }

                data.markup.currValues.set(mappedRow, generateMarkup(markupPrefixes, markupRowSets));
            }

            // add this snippet
            line += stringFlushed.substring(numRead, numRead + snipLength);
            currCol += snipLength;
            numRead += snipLength;

            if (suffix != null) {
                line += suffix;
            }

            data.message.currValues.set(mappedRow, line);
            scheduleModRowProp(baseRow + currRow - 1);

            if (nextNewLine != -1) {
                numRead += 1; // snip the newline

                final long innerIndex;
                final long outerIndex;

                mappingLock.lock();
                try {
                    currCol = 1;

                    if (nextOuterIndex == baseRow + currRow) {
                        innerIndex = acquireRow();
                        outerIndex = nextOuterIndex++;
                        data.redirectionIndex.put(outerIndex, innerIndex);
                        if (data.redirectionIndex.get(outerIndex) == RowSet.NULL_ROW_KEY) {
                            throw new IllegalStateException("Failed to acquire row");
                        }
                    } else {
                        innerIndex = outerIndex = -1;
                    }

                    if (data.redirectionIndex.get(outerIndex) == RowSet.NULL_ROW_KEY) {
                        throw new IllegalStateException("Failed to acquire row");
                    }
                    currRow += 1;
                } finally {
                    mappingLock.unlock();
                }

                if (innerIndex != -1) {
                    initOuputRow(innerIndex);
                    scheduleAddRowProp(outerIndex);
                }
            }
        }
    }

    private String[] generateMarkup(final String[] markupPrefixes, final WritableRowSet[] markupRowSets) {
        final String[] result = new String[markupPrefixes.length];
        for (int ii = 0; ii < result.length; ++ii) {
            final MutableBoolean first = new MutableBoolean(true);
            final MutableObject<String> rows = new MutableObject<>("\"");
            markupRowSets[ii].forAllRowKeyRanges((start, end) -> {
                if (!first.booleanValue()) {
                    rows.setValue(rows.getValue() + ",");
                } else {
                    first.setValue(false);
                }
                if (start == end) {
                    rows.setValue(rows.getValue() + start);
                } else {
                    rows.setValue(rows.getValue() + start + "-" + end);
                }
            });
            result[ii] = markupPrefixes[ii] + rows.getValue() + "\"}";
        }
        return result;
    }

    private void initOuputRow(long innerIndex) {
        data.message.currValues.set(innerIndex, "");
//        data.lastUpdateTime.currValues.set(innerIndex, scheduler.currentTimeNanos());
        data.markup.currValues.set(innerIndex, new String[] {});
        data.markupPrefixes.set(innerIndex, new String[] {});
        data.markupRowSets.set(innerIndex, new WritableRowSet[] {});
        data.logLevel.currValues.set(innerIndex, "STDOUT");
    }

    private long appendBlankRow() {
        final long innerIndex;
        final long outerIndex;

        mappingLock.lock();
        try {
            innerIndex = acquireRow();
            outerIndex = nextOuterIndex++;
            // TODO: shouldn't we free old rows?
            baseRow = Math.max(baseRow, outerIndex + 1 - numRows);
            data.redirectionIndex.put(outerIndex, innerIndex);
        } finally {
            mappingLock.unlock();
        }

        initOuputRow(innerIndex);
        scheduleAddRowProp(outerIndex);
        return outerIndex;
    }

    private static void setColorMode(FontState state, Integer[] colorParams) {
        if (colorParams == null) {
            colorParams = new Integer[] {0};
        }
        Iterator<Integer> colorParamIter = Arrays.stream(colorParams).iterator();
        while (colorParamIter.hasNext()) {
            setColorMode(state, colorParamIter);
        }
    }

    private static void setColorMode(FontState state, Iterator<Integer> colorModeParams) {
        int p1 = colorModeParams.next();
        switch (p1) {
            case 0:
                state.resetAttributes();
                break;
            case 1:
                state.bold = true;
                break;
            case 2:
                state.faint = true;
                break;
            case 3:
                state.italic = true;
                break;
            case 4:
                state.underline = true;
                break;
            case 5:
                state.blink = true;
                break;
            case 6:
                state.fastBlink = true;
                break;
            case 7:
                state.reverse = true;
                break;
            case 8:
                state.conceal = true;
                break;
            case 9:
                state.strike = true;
                break;
            case 21:
                state.doubleUnderline = true;
                break;
            case 22:
                state.bold = false;
                state.faint = false;
                break;
            case 23:
                state.italic = false;
                break;
            case 24:
                state.underline = false;
                state.doubleUnderline = false;
                break;
            case 25:
                state.blink = false;
                state.fastBlink = false;
                break;
            case 27:
                state.reverse = false;
                break;
            case 28:
                state.conceal = false;
                break;
            case 29:
                state.strike = false;
                break;
            case 38:
                state.fgColor = parseColor(colorModeParams);
                break;
            case 39:
                state.fgColor = null;
                break;
            case 48:
                state.bgColor = parseColor(colorModeParams);
                break;
            case 49:
                state.bgColor = null;
                break;
            case 53:
                state.overline = true;
                break;
            case 55:
                state.overline = false;
                break;
            case 58:
                state.ulColor = parseColor(colorModeParams);
                break;
            case 59:
                state.ulColor = null;
                break;
            case 73:
                state.superscript = true;
                break;
            case 74:
                state.subscript = true;
                break;
            case 75:
                state.superscript = false;
                state.subscript = false;
                break;
            default:
                if (p1 >= 30 && p1 <= 37) {
                    state.fgColor = c8(p1);
                } else if (p1 >= 40 && p1 <= 47) {
                    state.bgColor = c8(p1);
                } else if (p1 >= 90 && p1 <= 97) {
                    state.fgColor = c8(p1);
                } else if (p1 >= 100 && p1 <= 107) {
                    state.bgColor = c8(p1);
                }
        }
    }

    private static String parseColor(Iterator<Integer> colorModeParams) {
        final MutableBoolean failed = new MutableBoolean();

        final Callable<Integer> nextParam = () -> {
            if (!colorModeParams.hasNext()) {
                failed.setTrue();
                return 0;
            }
            return colorModeParams.next();
        };

        // we are reading one of these patterns: d1;5;c1m, d1;2;r1;g1;b1m
        String result = null;
        try {
            int p1 = nextParam.call();
            if (p1 == 2) {
                int r = nextParam.call();
                int g = nextParam.call();
                int b = nextParam.call();
                result = cRGB(r, g, b);
            } else if (p1 == 5) {
                int c = nextParam.call();
                result = c256(c);
            }
        } catch (final Exception ignored) {
            failed.setTrue();
        }

        if (failed.booleanValue()) {
            return c8(0);
        }
        return result;
    }

    private static String c8(final int p0) {
        // Colors picked from Visual Studio Console as referenced on https://en.wikipedia.org/wiki/ANSI_escape_code
        // Except that the white matches the Web UI
        switch (p0) {
            case 31:
            case 41:
                return cRGB(205, 49, 49);
            case 32:
            case 42:
                return cRGB(13, 188, 121);
            case 33:
            case 43:
                return cRGB(229, 229, 16);
            case 34:
            case 44:
                return cRGB(36, 114, 200);
            case 35:
            case 45:
                return cRGB(188, 63, 188);
            case 36:
            case 46:
                return cRGB(17, 168, 205);
            case 37:
            case 47:
            case 97:
            case 107:
                return cRGB(0xf0, 0xf0, 0xee);
            case 90:
            case 100:
                return cRGB(102, 102, 102);
            case 91:
            case 101:
                return cRGB(241, 76, 76);
            case 92:
            case 102:
                return cRGB(35, 209, 139);
            case 93:
            case 103:
                return cRGB(245, 245, 67);
            case 94:
            case 104:
                return cRGB(59, 142, 234);
            case 95:
            case 105:
                return cRGB(214, 112, 214);
            case 96:
            case 106:
                return cRGB(41, 184, 219);
            default:
                return cRGB(0, 0, 0);
        }
    }

    private static String c256(int n) {
        if (n < 0 || n > 255) {
            n = 0;
        }
        if (n < 8) {
            return c8(n + 30);
        }
        if (n < 16) {
            return c8(n + 82);
        }
        if (n < 232) {
            n -= 16;
            final int r = n / 36;
            final int g = (n / 6) % 6;
            final int b = n % 6;
            return cRGB(r * 51, g * 51, b * 51);
        }
        n -= 232;
        final int grey = n * 10 + 8;
        return cRGB(grey, grey, grey);
    }

    private static String cRGB(final int r, final int g, final int b) {
        return String.format("#%02X%02X%02X", r & 0xff, g & 0xff, b & 0xff);
    }

    private void scheduleAddRowProp(long index) {
        mappingLock.lock();
        long innerIndex = data.redirectionIndex.get(index);
        if (innerIndex == RowSet.NULL_ROW_KEY) {
            throw new IllegalStateException("Row " + index + " is not redirected");
        }
        mappingLock.unlock();

        updateLock.lock();
        try {
            ltmAddPending.insert(index);
            barrageAddPending.insert(index);
            barragePropagationHelper.schedulePropagation();
            registrar.requestRefresh();
        } finally {
            updateLock.unlock();
        }
    }

    private void scheduleModRowProp(long index) {
        updateLock.lock();
        try {
            ltmModPending.insert(index);
            barrageModPending.insert(index);
            barragePropagationHelper.schedulePropagation();
            registrar.requestRefresh();
        } finally {
            updateLock.unlock();
        }
    }

    private void onFastPropagation() {
        final RowSet added;
        final WritableRowSet modified;

        updateLock.lock();
        try {
            added = barrageAddPending.copy();
            barrageAddPending.clear();
            modified = barrageModPending.copy();
            modified.remove(added);
            barrageModPending.clear();
        } finally {
            updateLock.unlock();
        }

        barrageTable.getRowSet().writableCast().insert(added);

        // TODO: retire old rows based on some sort of cost estimate?

        final TableUpdateImpl update = new TableUpdateImpl();
        try {
            ++barrageStep;
            update.added = added;
            update.modified = modified;
            update.removed = RowSetFactory.empty();
            update.shifted = RowSetShiftData.EMPTY;
            update.modifiedColumnSet = getModifiedColumnSetForUpdates();
            barrageListener.onUpdate(update);
        } finally {
            update.release();
        }
    }

    private void onFastPropagationError(final Exception err) {
        log.error().append("Error when propagating updates to barrage side of TerminalAsATable: ").append(err).endl();
    }

    @Override
    public void run() {
        propagateToLTM();
    }

    private void propagateToLTM() {
        final RowSet added;
        final WritableRowSet modified;
        try {
            updateLock.lock();
            added = ltmAddPending.copy();
            ltmAddPending.clear();
            modified = ltmModPending.copy();
            modified.remove(added);
            ltmModPending.clear();
        } finally {
            updateLock.unlock();
        }

        if (added.isEmpty() && modified.isEmpty()) {
            return;
        }

        final WritableRowSet rowSet = getRowSet().writableCast();
        rowSet.insert(added);

        // copy data to our LTM table; it's ok if we aren't super consistent (we might coalesce or dupe updates)
        try (final RowSet both = added.union(modified);
                final ChunkSource.FillContext rContext =
                        data.redirectionIndex.makeFillContext(both.intSize(), null);
                final WritableLongChunk<RowKeys> mappedKeyChunk =
                        WritableLongChunk.makeWritableChunk(both.intSize())) {
            mappingLock.lock();
            data.redirectionIndex.fillChunk(rContext, mappedKeyChunk, both);
            for (int ii = 0; ii < mappedKeyChunk.size(); ++ii) {
                if (mappedKeyChunk.get(ii) == -1) {
                    throw new IllegalStateException("Unable to find mapping for row " + both.get(ii));
                }
            }
            data.columns.forEach(c -> c.propagateToLTM(mappedKeyChunk));
        } finally {
            mappingLock.unlock();
        }

        final TableUpdateImpl update = new TableUpdateImpl();
        update.added = added;
        update.modified = modified;
        update.removed = RowSetFactory.empty();
        update.shifted = RowSetShiftData.EMPTY;
        update.modifiedColumnSet = getModifiedColumnSetForUpdates();
        notifyListeners(update);
    }

    /**
     * We cheat w/Barrage so the terminal can continue to tick even when the LTM cycles times are slow or the LTM is
     * locked. Instead, we offer both a layer that can be consumed in TableOperations that are bound by the LTM update
     * cycle and a layer that is an immediate dissemination to a BarrageMessageProducer bypassing the LTM entirely. As
     * we back a Terminal, we would like to update even when the user is running a long script holding up the LTM.
     */
    public BarrageMessageProducer<BarrageStreamGeneratorImpl.View> getBarrageMessageProducer() {
        return barrage;
    }

    /**
     * When a user submits a request they are allocated a row for that request. The row is filled out as the request is
     * fulfilled.
     *
     * @param request the source of the request
     * @return a new instance of InputEntry to fill in details on the results of the request
     */
    public InputEntry newInputEntry(final String request) {
        final long innerIndex;
        final long outerIndex;

        mappingLock.lock();
        try {
            innerIndex = acquireRow();
            outerIndex = nextOuterIndex++;
            data.redirectionIndex.put(outerIndex, innerIndex);
            currRow = 1;
            baseRow = appendBlankRow();
        } finally {
            mappingLock.unlock();
        }

        // create the entry before the flush so the request is visible to the user
        final InputEntry entry = new InputEntry(innerIndex, outerIndex, request);
        scheduleAddRowProp(outerIndex);
        return entry;
    }

    /**
     * InputEntry is meant to be used by the console processing code. This code runs on the single threaded executor and
     * therefore the source is intrinsically single threaded.
     */
    public class InputEntry {
        final long innerIndex;
        final long outerIndex;
        final long origTime;
        long startTime;

        private InputEntry(final long innerIndex, final long outerIndex, final String source) {
            this.innerIndex = innerIndex;
            this.outerIndex = outerIndex;
            origTime = scheduler.currentTimeNanos();
//            data.lastUpdateTime.currValues.set(innerIndex, origTime);
            data.source.currValues.set(innerIndex, source);
        }

        public void setLanguage(final String language) {
            data.language.currValues.set(innerIndex, language.toLowerCase());
        }

        public void onRunStart() {
            startTime = scheduler.currentTimeNanos();
            data.queueDuration.currValues.set(innerIndex, startTime - origTime);
        }

        public void onRunFinish() {
            data.runDuration.currValues.set(innerIndex, scheduler.currentTimeNanos() - startTime);
        }

        public void setCreatedVars(final Map<String, String> createdVars) {
            final MutableInt idx = new MutableInt();
            final String[] json = createdVars.entrySet().stream().map(entry -> {
                final VarRef old = liveVariableMap.put(entry.getKey(),
                        new VarRef(outerIndex, idx.getAndIncrement(), false));
                if (old != null) {
                    disableVar(old);
                }
                return makeVarJSON(entry);
            }).toArray(String[]::new);
            data.createdVars.currValues.set(innerIndex, json);
        }

        public void setUpdatedVars(final Map<String, String> updatedVars) {
            final MutableInt idx = new MutableInt();
            final String[] json = updatedVars.entrySet().stream().map(entry -> {
                final VarRef old = liveVariableMap.put(entry.getKey(),
                        new VarRef(outerIndex, idx.getAndIncrement(), true));
                if (old != null) {
                    disableVar(old);
                }
                return makeVarJSON(entry);
            }).toArray(String[]::new);
            data.updatedVars.currValues.set(innerIndex, json);
        }

        public void setRemovedVars(final Map<String, String> removedVars) {
            final String[] json = removedVars.entrySet().stream().map(entry -> {
                final VarRef old = liveVariableMap.remove(entry.getKey());
                if (old != null) {
                    disableVar(old);
                }
                return makeVarJSON(entry);
            }).toArray(String[]::new);
            data.removedVars.currValues.set(innerIndex, json);
        }

        public void scheduleUpdate() {
//            data.lastUpdateTime.currValues.set(innerIndex, scheduler.currentTimeNanos());
            scheduleModRowProp(outerIndex);
        }

        private void disableVar(final VarRef ref) {
            final long innerRow;
            mappingLock.lock();
            try {
                innerRow = data.redirectionIndex.get(ref.rowKey);
            } finally {
                mappingLock.unlock();
            }

            if (innerRow == RowSet.NULL_ROW_KEY) {
                return;
            }

            final ColumnInfo<String[]> column = ref.isUpdate ? data.updatedVars : data.createdVars;
            final String[] varList = column.currValues.get(innerRow);
            final int offset = ref.arrayOffset;
            if (varList == null || varList.length <= offset) {
                return;
            }
            String json = varList[offset];
            json = json.substring(0, json.length() - 1); // remove final close brace
            json += ", \"disabled\": true}";
            varList[offset] = json;
            scheduleModRowProp(ref.rowKey);
        }
    }

    private static String makeVarJSON(final Map.Entry<String, String> entry) {
        return String.format("{\"title\": \"%s\", \"type\": \"%s\", \"id\": \"%s\"}",
                entry.getKey(), entry.getValue(), ScopeTicketResolver.ticketStringForName(entry.getKey()));
    }

    private static class VarRef {
        final long rowKey;
        final int arrayOffset;
        final boolean isUpdate;

        public VarRef(final long rowKey, final int arrayOffset, final boolean isUpdate) {
            this.rowKey = rowKey;
            this.arrayOffset = arrayOffset;
            this.isUpdate = isUpdate;
        }
    }

    private static class ColumnInfo<T> {
        final String name;
        /** no history; always current; fed to BarrageMessageProducer enabling faster than LTM cycle updates */
        final ArrayBackedColumnSource<T> currValues;
        /** the values of this table as adheres to the LTM clock; this enables normal table usage */
        final ArrayBackedColumnSource<T> ltmValues;

        public ColumnInfo(final String name, final Class<T> dataType) {
            this.name = name;
            this.currValues = ArrayBackedColumnSource.getMemoryColumnSource(0, dataType, null);
            this.ltmValues = ArrayBackedColumnSource.getMemoryColumnSource(0, dataType, null);
            this.ltmValues.startTrackingPrevValues();
        }

        public void ensureCapacity(long capacity) {
            currValues.ensureCapacity(capacity);
            ltmValues.ensureCapacity(capacity);
        }

        public void propagateToLTM(final LongChunk<RowKeys> updated) {
            final int size = updated.size();
            try (final ChunkSource.FillContext currContext = currValues.makeFillContext(size);
                    final WritableChunk<Values> currChunk = currValues.getChunkType().makeWritableChunk(size);
                    final ChunkSink.FillFromContext fillContext = ltmValues.makeFillFromContext(size)) {
                currValues.fillChunkUnordered(currContext, currChunk, updated);
                ltmValues.fillFromChunkUnordered(fillContext, currChunk, updated);
            }
        }
    }

    private static class TableCreator {
        final public WritableRowRedirection redirectionIndex = WritableRowRedirection.FACTORY.createRowRedirection(8);
        final public LinkedHashMap<String, ColumnSource<?>> barrageColumns = new LinkedHashMap<>();
        final public LinkedHashMap<String, ColumnSource<?>> ltmColumns = new LinkedHashMap<>();
        final public List<ColumnInfo<?>> columns = new ArrayList<>();

        protected <T> ColumnInfo<T> newColumn(final String name, final Class<T> dataType) {
            final ColumnInfo<T> info = new ColumnInfo<>(name, dataType);
            barrageColumns.put(name, RedirectedColumnSource.maybeRedirect(redirectionIndex, info.currValues));
            ltmColumns.put(name, RedirectedColumnSource.maybeRedirect(redirectionIndex, info.ltmValues));
            columns.add(info);
            return info;
        }

        public void ensureCapacity(long capacity) {
            columns.forEach(col -> col.ensureCapacity(capacity));
        }
    }

    public static class TableAccess extends TableCreator {
        /** the capacity that the destSources been set to */
        private int capacity = 0;

//        protected final ColumnInfo<DateTime> lastUpdateTime = newColumn("lastUpdateTime", DateTime.class);
        protected final ColumnInfo<String> logLevel = newColumn("logLevel", String.class);
        protected final ColumnInfo<String> source = newColumn("source", String.class);
        protected final ColumnInfo<String> language = newColumn("language", String.class);
        protected final ColumnInfo<String> message = newColumn("message", String.class);
        protected final ColumnInfo<Long> queueDuration = newColumn("queueDuration", long.class);
        protected final ColumnInfo<Long> runDuration = newColumn("runDuration", long.class);
        protected final ColumnInfo<String[]> createdVars = newColumn("createdVars", String[].class);
        protected final ColumnInfo<String[]> updatedVars = newColumn("updatedVars", String[].class);
        protected final ColumnInfo<String[]> removedVars = newColumn("removedVars", String[].class);
        protected final ColumnInfo<String[]> markup = newColumn("markup", String[].class);
        protected final ArrayBackedColumnSource<String[]> markupPrefixes =
                ArrayBackedColumnSource.getMemoryColumnSource(0, String[].class, String.class);
        protected final ArrayBackedColumnSource<WritableRowSet[]> markupRowSets =
                ArrayBackedColumnSource.getMemoryColumnSource(0, WritableRowSet[].class, WritableRowSet.class);

        @Inject
        public TableAccess() {}

        @Override
        public void ensureCapacity(long capacity) {
            super.ensureCapacity(capacity);
            markupPrefixes.ensureCapacity(capacity);
            markupRowSets.ensureCapacity(capacity);
        }
    }

    private static class FontState {
        boolean bold = false;
        boolean faint = false;
        boolean italic = false;
        boolean underline = false;
        boolean blink = false;
        boolean fastBlink = false;
        boolean reverse = false;
        boolean conceal = false;
        boolean strike = false;
        boolean doubleUnderline = false;
        boolean overline = false;
        boolean subscript = false;
        boolean superscript = false;

        // default colors are terminal widgets foreground/background
        String fgColor = null;
        String bgColor = null;
        String ulColor = null;

        void resetAttributes() {
            bold = false;
            faint = false;
            italic = false;
            underline = false;
            blink = false;
            fastBlink = false;
            reverse = false;
            conceal = false;
            strike = false;
            doubleUnderline = false;
            overline = false;
            superscript = false;
            subscript = false;
            fgColor = null;
            bgColor = null;
            ulColor = null;
        }

        String getJsonPrefix() {
            String json = "{";
            boolean first = true;

            String flagStr = "";
            if (bold) {
                flagStr += 'o';
            }
            if (faint) {
                flagStr += 'f';
            }
            if (italic) {
                flagStr += 'i';
            }
            if (underline) {
                flagStr += 'u';
            }
            if (blink) {
                flagStr += 'b';
            }
            if (fastBlink) {
                flagStr += 'B';
            }
            if (reverse) {
                flagStr += 'r';
            }
            if (conceal) {
                flagStr += 'c';
            }
            if (strike) {
                flagStr += 't';
            }
            if (doubleUnderline) {
                flagStr += 'U';
            }
            if (overline) {
                flagStr += 'O';
            }
            if (subscript) {
                flagStr += 's';
            }
            if (superscript) {
                flagStr += 'S';
            }
            if (!flagStr.isEmpty()) {
                first = false;
                json += "\"fl\":\"" + flagStr + "\"";
            }
            if (fgColor != null) {
                if (!first) {
                    json += ",";
                }
                first = false;
                json += "\"fg\":\"" + fgColor + "\"";
            }
            if (bgColor != null) {
                if (!first) {
                    json += ",";
                }
                first = false;
                json += "\"bg\":\"" + bgColor + "\"";
            }
            if (ulColor != null) {
                if (!first) {
                    json += ",";
                }
                first = false;
                json += "\"ul\":\"" + ulColor + "\"";
            }

            if (first) {
                // no flags or colors
                return null;
            } else {
                json += ",";
            }
            json += "\"c\":";

            return json;
        }
    }
}
