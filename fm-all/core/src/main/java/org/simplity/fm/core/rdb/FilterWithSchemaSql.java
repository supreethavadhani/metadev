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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;

/**
 * A Sql that is designed to filter rows from the RDBMS. That is, result may
 * contain more than one rows
 *
 * @author simplity.org
 * @param <T>
 *            SchemaDataTable returned when more than one rows are filtered
 *
 */
public abstract class FilterWithSchemaSql<T extends SchemaDataTable> extends Sql {
	protected Schema schema;

	/**
	 * filter rows into a data table
	 *
	 * @param handle
	 * @return non-null data table that has all the rows filtered. could be
	 *         empty
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public T filter(final DbHandle handle) throws SQLException {
		final T table = (T) this.schema.newSchemaDataTable();
		table.filter(handle, this.sqlText, this.inputData.getRawData());
		return table;
	}

	/**
	 * to be used when at least one row is expected as per our db design, and
	 * hence the caller need not handle the case with no rows
	 *
	 * @param handle
	 * @return non-null non-empty schema data table with all filtered with the
	 *         first filtered row
	 * @throws SQLException
	 *             thrown when any SQL exception, OR when no rows are filtered
	 */
	public T filterOrFail(final DbHandle handle) throws SQLException {
		final T result = this.filter(handle);

		if (result.length() == 0) {
			throw new SQLException("Filter did not return any row. " + this.getState());
		}
		return result;
	}

	/**
	 * iterator on the result of filtering. To be used if we have no need to get
	 * the entire dataTable,
	 *
	 * @param handle
	 * @param fn
	 *            call back function that takes SchemaData as parameter, and
	 *            returns true to continue to read, and false if it is not
	 *            interested in getting any more rows
	 * @throws SQLException
	 */
	public void forEach(final DbHandle handle, final RowProcessor fn) throws SQLException {
		final String sql = this.schema.getDbAssistant().getSelectClause() + this.sqlText;
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return sql;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				FilterWithSchemaSql.this.inputData.setPsParams(ps);
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				final SchemaData data = FilterWithSchemaSql.this.schema.newSchemaData();
				data.readFromRs(rs);
				return fn.process(data);
			}
		});
	}

}
