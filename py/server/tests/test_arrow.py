#
# Copyright (c) 2016-2022 Deephaven Data Labs and Patent Pending
#
import unittest

import numpy as np
import pyarrow.parquet as papq

from deephaven.column import byte_col, char_col, short_col, bool_col, int_col, long_col, float_col, double_col, \
    string_col, datetime_col, pyobj_col, jobj_col

from deephaven import arrow as dharrow, dtypes, new_table
from tests.testbase import BaseTestCase


class ArrowTestCase(BaseTestCase):

    def test_round_trip(self):
        arrow_table = papq.read_table("tests/data/day_trades.parquet")

        dh_table = dharrow.to_table(arrow_table).tail(1)
        pa_table = dharrow.to_arrow(dh_table)

        dh_table_rt = dharrow.to_table(pa_table)

        self.assert_table_equals(dh_table, dh_table_rt)

    def test_round_trip_types(self):
        cols = [
            # bool_col(name="Boolean", data=[True, False]),
            byte_col(name="Byte", data=(1, -1)),
            char_col(name="Char", data='-1'),
            short_col(name="Short", data=[1, -1]),
            int_col(name="Int", data=[1, -1]),
            long_col(name="Long", data=[1, -1]),
            long_col(name="NPLong", data=np.array([1, -1], dtype=np.int8)),
            float_col(name="Float", data=[1.01, -1.01]),
            double_col(name="Double", data=[1.01, -1.01]),
            string_col(name="String", data=["foo", "bar"]),
            datetime_col(name="Datetime", data=[dtypes.DateTime(1), dtypes.DateTime(-1)]),
        ]
        dh_table = new_table(cols=cols)
        pa_table = dharrow.to_arrow(dh_table)
        dh_table_rt = dharrow.to_table(pa_table)
        self.assert_table_equals(dh_table, dh_table_rt)

    def test_round_trip_empty(self):
        cols = [
            byte_col(name="Byte", data=()),
            char_col(name="Char", data=''),
            short_col(name="Short", data=[]),
            int_col(name="Int", data=[]),
            long_col(name="Long", data=[]),
            long_col(name="NPLong", data=np.array([], dtype=np.int8)),
            float_col(name="Float", data=[]),
            double_col(name="Double", data=[]),
            string_col(name="String", data=[]),
            datetime_col(name="Datetime", data=[]),
        ]
        dh_table = new_table(cols=cols)
        pa_table = dharrow.to_arrow(dh_table)
        dh_table_rt = dharrow.to_table(pa_table)
        self.assert_table_equals(dh_table, dh_table_rt)


if __name__ == '__main__':
    unittest.main()
