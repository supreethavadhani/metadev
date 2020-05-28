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

import org.simplity.fm.core.data.Record;

/**
 * A Sql that is designed to read just one row from the RDBMS
 *
 * @author simplity.org
 * @param <T>
 *            concrete class of output value object that can be used to access
 *            the out data elements
 *
 */
public abstract class ReadSql<T extends Record> extends Sql {

	protected abstract T newOutputData();

	/**
	 * read a row from the db. must be called ONLY AFTER setting all input
	 * parameters
	 *
	 * @param handle
	 * @return value object with output data. null if data is not read.
	 * @throws SQLException
	 */
	public T read(final DbHandle handle) throws SQLException {
		final T result = this.newOutputData();
		final boolean ok = handle.read(this.sqlText, this.inputData, result);
		if (ok) {
			return result;
		}
		return null;
	}

	/**
	 * read a row from the db. must be called ONLY AFTER setting all input
	 * parameters
	 *
	 * @param handle
	 * @return value object with output data. null if data is not read.
	 * @throws SQLException
	 */
	public T readOrFail(final DbHandle handle) throws SQLException {
		final T result = this.newOutputData();
		final boolean ok = handle.read(this.sqlText, this.inputData, result);
		if (ok) {
			return result;
		}
		logger.error(this.showDetails());
		throw new SQLException("Sql is expected to return one row, but it didn't.");
	}
}
