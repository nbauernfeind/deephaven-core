import io.deephaven.appmode.ApplicationContext
import io.deephaven.db.tables.utils.TableTools

app = ApplicationContext.get()
hello = TableTools.emptyTable(42)
app.setField("hello", hello)
app.setField("world", TableTools.timeTable("00:00:01"))
