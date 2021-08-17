import io.deephaven.db.appmode.ApplicationContext
import io.deephaven.db.tables.utils.TableTools

app = ApplicationContext.get()
app.setField("hello", TableTools.emptyTable(42))
app.setField("world", TableTools.timeTable("00:00:01"))
