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
import java.util.List;

import org.simplity.fm.core.data.ValueObject;

/**
 * A Sql that is designed to read just one row from the RDBMS
 *
 * @author simplity.org
 * @param <T>
 *            concrete class of output value object that can be used to access
 *            the out data elements
 *
 */
public abstract class FilterSql<T extends ValueObject> extends Sql {

	protected abstract T newOutputData();

	/**
	 * read a row from the db. must be called ONLY AFTER setting all input
	 * parameters
	 *
	 * @param handle
	 * @return array of value object with output data. empty, but not null if
	 *         there are no rows.
	 * @throws SQLException
	 */
	public List<T> filter(final DbHandle handle) throws SQLException {
		final T instance = this.newOutputData();
		return handle.filter(this.sqlText, this.inputData, instance);
	}

	/**
	 * read a row from the db. must be called ONLY AFTER setting all input
	 * parameters
	 *
	 * @param handle
	 * @return array of value object with output data. empty, but not null if
	 *         there are no rows.
	 * @throws SQLException
	 */
	public List<T> filterOrFail(final DbHandle handle) throws SQLException {
		final T instance = this.newOutputData();
		final List<T> list = handle.filter(this.sqlText, this.inputData, instance);
		if (list.size() > 0) {
			return list;
		}
		logger.error(this.getState());
		throw new SQLException("Sql is expected to return at least one row, but it didn't.");
	}

	/**
	 * iterator on the result of filtering. To be used if we have no need to get
	 * the entire dataTable,
	 *
	 * @param handle
	 * @param fn
	 *            call back function that takes Vo as parameter, and
	 *            returns true to continue to read, and false if it is not
	 *            interested in getting any more rows
	 * @throws SQLException
	 */
	public void forEach(final DbHandle handle, final RowProcessor fn) throws SQLException {
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return FilterSql.this.sqlText;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				FilterSql.this.inputData.setPsParams(ps);
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				final ValueObject vo = FilterSql.this.newOutputData();
				vo.readFromRs(rs);
				return fn.process(vo);
			}
		});
	}
}
