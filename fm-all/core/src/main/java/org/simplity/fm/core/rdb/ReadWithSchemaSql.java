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

/**
 * A Sql that is designed to read just one row from the RDBMS.
 *
 * @author simplity.org
 * @param <T>
 *            SchemaData returned when reading
 *
 */
public abstract class ReadWithSchemaSql<T extends SchemaData> extends Sql {
	protected Schema schema;

	/**
	 * read a row suing this sql
	 * 
	 * @param handle
	 * @return null if read did not succeed.
	 * @throws SQLException
	 */
	@SuppressWarnings("unchecked")
	public T filterFirst(final DbHandle handle) throws SQLException {
		final T data = (T) this.schema.newSchemaData();
		if (data.filterFirstOne(handle, this.sqlText, this.inputData.getRawData())) {
			return data;
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
	public T filterFirstOrFail(final DbHandle handle) throws SQLException {
		final T result = this.filterFirst(handle);

		if (result == null) {
			throw new SQLException("Filter First did not return any row. " + this.getState());
		}
		return result;
	}
}
