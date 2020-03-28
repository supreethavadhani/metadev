/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.core.rdb;

import java.sql.SQLException;

import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;

/**
 * A Sql that is designed to filter rows from the RDBMS. That is, result may
 * contain more than one rows
 *
 * @author simplity.org
 * @param <T1>
 *            SchemaData returned when filtering just one row
 * @param <T2>
 *            SchemaDataTable returned when more than one rows are filtered
 *
 */
public abstract class FilterSql<T1 extends SchemaData, T2 extends SchemaDataTable> extends Sql {
	protected Schema schema;

	/**
	 * this is to be used if the filter condition is expected to get only ine
	 * row.Typically when the filter condition is for unique-key or conceptual
	 * primary key.
	 * It may also be used when you are looking to get the first one in an
	 * ordered result,or you are okay with any one of the possible results.
	 *
	 * @param handle
	 * @return null if filter did not get even one row. Schema data for the
	 *         first filtered row otherwise.
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public T1 filterFirst(final DbHandle handle) throws SQLException {
		final SchemaData data = this.schema.newSchemaData();
		if (data.filterFirstOne(handle, this.sqlText, this.inputData.getRawData())) {
			return (T1) data;
		}
		return null;
	}

	/**
	 * to be used when a row is expected as per our db design, and hence the
	 * caller need not handle the case with no rows
	 *
	 * @param handle
	 * @return non-null schema data with the first filtered row
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are filtered
	 */
	public T1 filterFirstOrFail(final DbHandle handle) throws SQLException {
		final T1 result = this.filterFirst(handle);

		if (result == null) {
			throw new SQLException("Filter First did not return any row. " + this.getState());
		}
		return result;
	}

	/**
	 * filter rows into a data table
	 *
	 * @param handle
	 * @return schema data table that has all the rows filtered. null if no rows
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public T2 filter(final DbHandle handle) throws SQLException {
		final SchemaDataTable table = this.schema.newSchemaDataTable();
		if (table.filter(handle, this.sqlText, this.inputData.getRawData())) {
			return (T2) table;
		}
		return null;

	}

	/**
	 * to be used when at least one row is expected as per our db design, and
	 * hence the
	 * caller need not handle the case with no rows
	 *
	 * @param handle
	 * @return non-null schema data table with allfiltered with the first
	 *         filtered row
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are filtered
	 */
	public T2 filterOrFail(final DbHandle handle) throws SQLException {
		final T2 result = this.filter(handle);

		if (result == null) {
			throw new SQLException("Filter did not return any row. " + this.getState());
		}
		return result;
	}

}
