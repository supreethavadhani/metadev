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
import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.data.ValueObject;

/**
 * @author simplity.org
 *
 */
public abstract class WriteSql extends Sql {
	private List<ValueObject> batchData;

	/**
	 *
	 * @param handle
	 * @return number of affected rows. could be 0.
	 * @throws SQLException
	 */
	public int write(final DbHandle handle) throws SQLException {
		if (this.batchData != null) {
			throw new SQLException("Sql is prepared for batch, but write is issued.");
		}
		return handle.write(this.sqlText, this.inputData);
	}

	/**
	 *
	 * @param handle
	 * @return number of affected rows. could be 0.
	 * @throws SQLException
	 */
	public int writeBatch(final DbHandle handle) throws SQLException {
		if (this.batchData == null) {
			throw new SQLException("Sql is not prepared for batch, but writeBatch is issued.");
		}
		final int n = handle.writeMany(this.sqlText, this.batchData.toArray(new ValueObject[0]));
		this.batchData = null;
		return n;
	}

	/**
	 * add a batch after setting value to all the fields. Note that this MUST be
	 * called before invoking writeBatch();
	 *
	 * @throws SQLException
	 *             if any field in the Vo is null
	 */
	public void addBatch() throws SQLException {
		if (this.batchData == null) {
			this.batchData = new ArrayList<>();
		}
		this.batchData.add(this.inputData);
		this.inputData = this.newValueObject();
	}

	protected abstract ValueObject newValueObject();
}
