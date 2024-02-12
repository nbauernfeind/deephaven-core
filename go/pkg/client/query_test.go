package client_test

import (
	"context"
	"testing"

	"github.com/deephaven/deephaven-core/go/internal/test_tools"
	"github.com/deephaven/deephaven-core/go/pkg/client"
)

// execBatchOrSerial can be either (*client.Client).ExecBatch or (*client.Client).ExecSerial.
type execBatchOrSerial func(*client.Client, context.Context, ...client.QueryNode) ([]*client.TableHandle, error)

func TestDagQueryBatched(t *testing.T) {
	dagQuery(t, (*client.Client).ExecBatch)
}

func dagQuery(t *testing.T, exec execBatchOrSerial) {
	ctx := context.Background()

	c, err := client.NewClient(ctx, test_tools.GetHost(), test_tools.GetPort(), test_tools.GetAuthType(), test_tools.GetAuthToken())
	test_tools.CheckError(t, "NewClient", err)
	defer c.Close()

	rec := test_tools.ExampleRecord()
	defer rec.Release()

	// Close (float32), Volume (int32), Ticker (string)
	exTable, err := c.ImportTable(ctx, rec)
	test_tools.CheckError(t, "ImportTable", err)
	defer exTable.Release(ctx)

	// Close (float32), Volume (int32), TickerLen (int)
	exLenQuery := exTable.Query().
		Update("TickerLen = Ticker.length()").
		DropColumns("Ticker")

	// Close (float32), TickerLen (int)
	exCloseLenQuery := exLenQuery.
		Update("TickerLen = TickerLen + Volume").
		DropColumns("Volume")

	// Close (float32), TickerLen (int)
	otherQuery := c.EmptyTableQuery(5).
		Update("Close = (float)(ii / 3.0)", "TickerLen = (int)(ii + 1)")

	// Close (float32), TickerLen (int)
	finalQuery := client.MergeQuery("", otherQuery, exCloseLenQuery)

	tables, err := exec(c, ctx, finalQuery, otherQuery, exCloseLenQuery, exLenQuery)
	test_tools.CheckError(t, "ExecBatch", err)
	if len(tables) != 4 {
		t.Errorf("wrong number of tables")
		return
	}

	finalTable, err := tables[0].Snapshot(ctx)
	test_tools.CheckError(t, "Snapshot", err)
	otherTable, err := tables[1].Snapshot(ctx)
	test_tools.CheckError(t, "Snapshot", err)
	exCloseLenTable, err := tables[2].Snapshot(ctx)
	test_tools.CheckError(t, "Snapshot", err)
	exLenTable, err := tables[3].Snapshot(ctx)
	test_tools.CheckError(t, "Snapsnot", err)

	if finalTable.NumRows() != 5+7 || finalTable.NumCols() != 2 {
		t.Errorf("wrong size for finalTable")
		return
	}
	if otherTable.NumRows() != 5 || otherTable.NumCols() != 2 {
		t.Errorf("wrong size for otherTable")
		return
	}
	if exCloseLenTable.NumRows() != 7 || exCloseLenTable.NumCols() != 2 {
		t.Log(exCloseLenTable)
		t.Errorf("wrong size for exCloseLenTable")
		return
	}
	if exLenTable.NumRows() != 7 || exLenTable.NumCols() != 3 {
		t.Log(exLenTable)
		t.Errorf("wrong size for exLenTable")
		return
	}

	for _, tbl := range tables {
		err = tbl.Release(ctx)
		test_tools.CheckError(t, "Release", err)
	}
}
