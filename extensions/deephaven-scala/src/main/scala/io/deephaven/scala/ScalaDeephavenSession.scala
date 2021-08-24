package io.deephaven.scala

import ammonite.compiler.Parsers
import ammonite.repl.Repl
import ammonite.repl.api.FrontEnd
import ammonite.util.{Colors, ImportData, Res}
import io.deephaven.UncheckedDeephavenException
import io.deephaven.db.exceptions.QueryCancellationException
import io.deephaven.db.tables.lang.DBLanguageParser
import io.deephaven.db.tables.live.LiveTableMonitor
import io.deephaven.db.tables.select.QueryScope
import io.deephaven.db.util.scripts.{ScriptPathLoader, ScriptPathLoaderState}
import io.deephaven.db.util.{AbstractScriptSession, ScriptSession}
import io.deephaven.internal.log.LoggerFactory
import io.deephaven.io.log.LogEntry

import java.io.{ByteArrayOutputStream, InputStream, OutputStream, Reader}
import java.lang.reflect.{Method, Modifier}
import java.util
import java.util.Collections
import java.util.function.Supplier
import scala.collection.mutable

/**
 * Interactive Console Session using a Scala Interpreter
 */
object ScalaDeephavenSession {
  private val log = LoggerFactory.getLogger(classOf[ScalaDeephavenSession])
  var SCRIPT_TYPE = "Scala"

  class ExposedByteArrayOutputStream extends ByteArrayOutputStream {
    def peekBuffer: Array[Byte] = buf
  }
}

class ScalaDeephavenSession(listener: ScriptSession.Listener, val isDefaultScriptSession: Boolean)
  extends AbstractScriptSession(listener, isDefaultScriptSession) with ScriptSession {
  import ScalaDeephavenSession._

  val importString: String =  {
    """| import scala.language.postfixOps
       | import io.deephaven.db.tables.utils.TableTools._
       | import io.deephaven.db.v2.utils.TableLoggers._
       | import io.deephaven.db.v2.utils.PerformanceQueries._
       | import io.deephaven.db.tables.utils.WhereClause.whereClause
       | import io.deephaven.db.tables.DataColumn
       | import io.deephaven.db.tables.Table
       | import java.lang.reflect.Array
       | import io.deephaven.util.`type`.TypeUtils
       | import io.deephaven.db.tables.utils.ArrayUtils
       | import io.deephaven.db.tables.utils.DBDateTime
       | import io.deephaven.db.tables.utils.DBTimeUtils
       | import io.deephaven.base.string.cache.CompressedString
       | import io.deephaven.base.string.cache.CompressedString.compress
       | import org.joda.time.LocalTime
       | import io.deephaven.db.tables.utils.DBPeriod
       | import io.deephaven.db.tables.select.Param
       | import io.deephaven.db.tables.select.QueryScope
       | import java.util._
       | import java.lang._
       | import io.deephaven.util.QueryConstants._
       | import io.deephaven.libs.GroovyStaticImports._
       | import io.deephaven.db.tables.utils.DBTimeUtils._
       | import io.deephaven.db.tables.utils.DBTimeZone._
       | import io.deephaven.db.tables.lang.DBLanguageFunctionUtil._
       | import io.deephaven.db.v2.by.ComboAggregateFactory._
       | """.stripMargin
  }

  val sysOut = new ExposedByteArrayOutputStream()
  val errOut = new ExposedByteArrayOutputStream()
  val parser: Parsers.type = ammonite.compiler.Parsers
  val amm: ammonite.Main = ammonite.Main(
    predefCode = importString,
    colors = Colors.BlackWhite,
    verboseOutput = false,
    outputStream = sysOut,
    errorStream = errOut,
    parser = () => parser
  )
  val repl: ammonite.repl.Repl = amm.instantiateRepl() match {
    case Left(_) => throw new UncheckedDeephavenException("Could not initialize scala repl")
    case Right(repl) =>
      repl.initializePredef().getOrElse {
        // Warm up the compilation logic in the background, hopefully while the
        // user is typing their first command, so by the time the command is
        // submitted it can be processed by a warm compiler
        val warmupThread = new Thread(() => repl.warmup())

        // This thread will terminal eventually on its own, but if the
        // JVM wants to exit earlier this thread shouldn't stop it
        warmupThread.setDaemon(true)
        warmupThread.start()
      }
      repl
  }
  // we need to fake this to get the pretty printing
  repl.frontEnd.bind(new FrontEnd {
    override def width: Int = 120 // assume reasonable width
    override def height: Int = 100 // assume reasonable length
    override def action(input: InputStream,
                        reader: Reader,
                        output: OutputStream,
                        prompt: String,
                        colors: Colors,
                        compilerComplete: (Int, String) => (Int, Seq[String], Seq[String]),
                        history: IndexedSeq[String], addHistory: String => Unit): Res[(String, Seq[String])] = {
      Res.Failure("unexpected call to action")
    }
  })
  setVariable("log", ScalaDeephavenSession.log)
  compilerContext.setParentClassLoader(repl.interp.evalClassloader)

  // Welcome Message =D
  System.out.println(ammonite.main.Defaults.welcomeBanner
    .replace("%SCALA_VERSION%", amm.compilerBuilder.scalaVersion)
  )

  override protected def newQueryScope = new QueryScope.SynchronizedScriptSessionImpl(this)

  private[this] val variables = mutable.Map[String, AnyRef]()

  @throws[QueryScope.MissingVariableException]
  override def getVariable(name: String): Any = synchronized {
    variables.getOrElse(name, throw new QueryScope.MissingVariableException("No binding for: " + name))
  }

  override def getVariable[T](name: String, defaultValue: T): T = synchronized {
    variables.getOrElse(name, defaultValue).asInstanceOf[T]
  }

  private[this] def flushStream(in: ExposedByteArrayOutputStream): Unit = {
    System.out.write(in.peekBuffer, 0, in.size())
    in.reset()
  }
  private[this] def flushStream(in: ExposedByteArrayOutputStream, out: LogEntry): Unit = {
    val buffer = in.peekBuffer
    if (isMoreThanOneLine(buffer, in.size())) {
      // let's only print if there is more than one line -- the first line is a note that it is compiling the source
      out.append("Error ").append(buffer, 0, in.size()).endl()
    }
    in.reset()
  }
  private[this] def isMoreThanOneLine(bytes: Array[Byte], len: Int): Boolean = {
    var offset = 0
    var lineFound = false
    while (offset < len) {
      if (bytes(offset) == '\n') {
        lineFound = true
      } else if (lineFound) {
        return true
      }
      offset += 1
    }

    false
  }

  private[this] var lastException: Throwable = _
  private[this] var evalId = -1
  override protected def evaluate(command: String, scriptName: String): Unit = synchronized {
    try {
      LiveTableMonitor.DEFAULT.exclusiveLock.computeLockedInterruptibly(() => {
        sysOut.reset()
        errOut.reset()

        evalId += 1
        val statements = parser.split(command, ignoreIncomplete = false).get match {
          case Left(error) =>
            ScalaDeephavenSession.log.error().append("Could not parse command: ").append(error).endl()
            return
          case Right(value) => value
        }
        val res = repl.interp.processLine(command, statements, evalId, silent = false, () => ())

        Repl.handleOutput(repl.interp, res)
        Repl.handleRes(
          res,
          repl.printer.info,
          repl.printer.error,
          lastException = _,
          repl.colors()
        ) match {
          case None =>
            if (sysOut.size() > 0 && sysOut.peekBuffer(sysOut.size() - 1) != '\n') {
              repl.printer.outStream.println()
            }
          case Some(_) =>
        }

        if (sysOut.size() > 0) {
          flushStream(sysOut)
        }
        if (errOut.size() > 0) {
          flushStream(errOut, ScalaDeephavenSession.log.error())
        }

        res.map(_.imports.value.foreach(onImportData))
      })
    } catch {
      case e: InterruptedException =>
        val msg = if (e.getMessage != null) e.getMessage else "Query interrupted"
        throw new QueryCancellationException(msg, e)
    }
  }

  private[this] def onImportData(id: ImportData): Unit = {
    id.importType match {
      case ImportData.Type =>
        onImportDataType(id)
      case ImportData.Term =>
        onImportDataTerm(id)
      case ImportData.TermType =>
        onImportDataType(id)
        onImportDataTerm(id)
    }
  }
  private[this] def onImportDataType(id: ImportData): Unit = {
    // TODO: add to query library
  }
  private[this] def onImportDataTerm(id: ImportData): Unit = {
    // using tail to skip `_root_` entry
    val namePrefix = id.prefix.view.tail.map(_.raw).mkString(".")

    val asField = fetchAsField(id, namePrefix)
    val asMethod = fetchAsMethod(id, namePrefix)
    val asObj = fetchAsObject(id, namePrefix)
    val fromPackage = fetchFromPackage(id, namePrefix)

    asField.orElse(asMethod).orElse(asObj).orElse(fromPackage) match {
      case None => ScalaDeephavenSession.log.error().append(s"Could not find instance for term: $id raw: ${id.toName.raw} ticked: ${id.toName.backticked}").endl()
      case Some(value) =>
        variables.put(id.toName.raw, value)
    }
  }

  private[this] def fetchAsField(id: ImportData, prefix: String): Option[AnyRef] = {
    val name = prefix + "$"
    try {
      val cls0 = repl.interp.evalClassloader.loadClass(getJvmName(name))
      val companion = cls0.getDeclaredField("MODULE$").get(null)
      val jvmVarName = getJvmName(id.toName.raw)
      val field = cls0.getDeclaredField(jvmVarName)
      if (!field.isAccessible) {
        // check to see if there is an accessor method
        Some(cls0.getDeclaredMethod(jvmVarName).invoke(companion))
      } else {
        Some(field.get(companion))
      }
    } catch {
      case _: ClassNotFoundException => None
      case _: NoSuchFieldException => None
    }
  }
  private[this] def fetchAsMethod(id: ImportData, prefix: String): Option[AnyRef] = {
    val name = prefix + "$"
    try {
      val cls0 = repl.interp.evalClassloader.loadClass(getJvmName(name))
      val companion = cls0.getDeclaredField("MODULE$").get(null)
      val methods: Array[Method] = cls0.getDeclaredMethods.filter(_.getName.equals(getJvmName(id.toName.raw)))

      if (methods.length == 1 && Modifier.isStatic(methods(0).getModifiers)) {
        // if we find a single static method; export that directly
        Some(methods(0))
      } else {
        // otherwise this is a member and it needs to be wrapped in a callable
        Some(makeImplicitCallable(companion, methods))
      }
    } catch {
      case _: ClassNotFoundException => None
    }
  }
  private[this] def fetchAsObject(id: ImportData, prefix: String): Option[AnyRef] = {
    val name = prefix + "." + id.fromName.raw + "$"
    try {
      val cls0 = repl.interp.evalClassloader.loadClass(getJvmName(name))
      Some(cls0.getDeclaredField("MODULE$").get(null))
    } catch {
      case _: ClassNotFoundException => None
    }
  }
  private[this] def fetchFromPackage(id: ImportData, prefix: String): Option[AnyRef] = {
    val companionClass = prefix + ".package$"
    try {
      val cls0 = repl.interp.evalClassloader.loadClass(getJvmName(companionClass))
      val companion = cls0.getDeclaredField("MODULE$").get(null)
      val methods: Array[Method] = cls0.getDeclaredMethods.filter(_.getName.equals(getJvmName(id.toName.raw)))
      Some(makeImplicitCallable(companion, methods))
    } catch {
      case _: ClassNotFoundException => None
      case _: NoSuchMethodException => None
    }
  }
  private[this] def getJvmName(name: String): String = {
    name.replace("/", "$div")
  }
  private[this] def makeImplicitCallable(instance: AnyRef, methods: Array[Method]): DBLanguageParser.ImplicitCallable = new DBLanguageParser.ImplicitMethod(instance, methods)

  override def getVariables: util.Map[String, AnyRef] = synchronized {
    val variableMap = new util.HashMap[String, AnyRef]()
    for ((name, value) <- variables) {
      variableMap.put(name, value)
    }
    Collections.unmodifiableMap(variableMap)
  }

  override def getVariableNames: util.Set[String] = synchronized {
    val variableNames = new util.HashSet[String]()
    for ((name, _) <- variables) {
      variableNames.add(name)
    }
    Collections.unmodifiableSet(variableNames)
  }

  override def hasVariableName(name: String): Boolean = synchronized {
    variables.contains(name)
  }

  override def setVariable(name: String, value: Any): Unit = synchronized {
    // TODO: how to bind a variable in the repl scope?
    ScalaDeephavenSession.log.error().append(s"skipping set variable $name $value").endl()
  }

  override val scriptType: String = ScalaDeephavenSession.SCRIPT_TYPE

  override def onApplicationInitializationBegin(pathLoader: Supplier[ScriptPathLoader], scriptLoaderState: ScriptPathLoaderState): Unit = {
  }

  override def onApplicationInitializationEnd(): Unit = {
  }

  override def setScriptPathLoader(scriptPathLoader: Supplier[ScriptPathLoader], caching: Boolean): Unit = {
  }

  override def clearScriptPathLoader(): Unit = {
  }

  override def setUseOriginalScriptLoaderState(useOriginal: Boolean) = true
}
