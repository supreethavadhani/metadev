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

package org.simplity.fm.core.data;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;

import org.simplity.fm.core.rdb.DbHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonWriter;

/**
 * @author simplity.org
 *
 */
public class DataTable implements Iterable<DataRow> {
	protected static final Logger logger = LoggerFactory.getLogger(DataTable.class);
	protected final Schema schema;
	protected Object[][] dataTable = new Object[0][];

	/**
	 *
	 * @param schema
	 */
	public DataTable(final Schema schema) {
		this.schema = schema;
	}

	/**
	 *
	 * @param schema
	 * @param data
	 */
	public DataTable(final Schema schema, final Object[][] data) {
		this.schema = schema;
		final int nbr = this.schema.getNbrFields();
		if (data == null || data.length == 0) {
			this.dataTable = new Object[0][];
		} else {
			for (final Object[] row : data) {
				if (row == null || row.length != nbr) {
					throw new RuntimeException("Data table needs " + nbr
							+ " fields in each of its row but is supplied woth invalid number of elements in one of its rows");
				}
			}
			this.dataTable = data;
		}
	}

	/**
	 *
	 * @param idx
	 * @return data row. null if the index is out of range
	 */
	public DataRow getRow(final int idx) {
		try {
			return new DataRow(this.schema, this.dataTable[idx]);
		} catch (final ArrayIndexOutOfBoundsException e) {
			logger.error("Data table has {} rows but row {} is requested. null returned", this.dataTable.length, idx);
			return null;
		}
	}

	/**
	 *
	 * @param idx
	 * @return data row. null if the index is out of range
	 */
	public Object[] getRawData(final int idx) {
		try {
			return this.dataTable[idx];
		} catch (final ArrayIndexOutOfBoundsException e) {
			logger.error("Data table has {} rows but row {} is requested. null returned", this.dataTable.length, idx);
			return null;
		}
	}

	/**
	 * iterator for data rows
	 */
	@Override
	public Iterator<DataRow> iterator() {
		return new Iterator<DataRow>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < DataTable.this.dataTable.length;
			}

			@Override
			public DataRow next() {
				return DataTable.this.getRow(this.idx++);
			}
		};
	}

	/**
	 * @return iterator over raw data
	 */
	public Iterable<Object[]> rowDataSet() {
		return () -> new Iterator<Object[]>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < DataTable.this.dataTable.length;
			}

			@Override
			public Object[] next() {
				return DataTable.this.getRawData(this.idx++);
			}
		};
	}

	/**
	 * serialized into an array [{},{}....]
	 *
	 * @param writer
	 * @throws IOException
	 */
	public void serializeAsJson(final JsonWriter writer) throws IOException {
		writer.beginArray();
		for (final Object[] row : this.dataTable) {
			writer.beginObject();
			this.schema.serializeToJson(row, writer);
			writer.endObject();
		}
		writer.endArray();
	}

	/**
	 * fetch data as per the sql
	 *
	 * @param handle
	 * @param reader
	 * @return true if at least one row is read
	 * @throws SQLException
	 */
	public boolean fetch(final DbHandle handle, final FilterSql reader) throws SQLException {
		this.dataTable = this.schema.getDbAssistant().filter(handle, reader.getSql(), reader.getWhereParams());
		return (this.dataTable.length > 0);
	}

	/**
	 * update all the rows into the data base
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error,
	 *         in which case the caller better roll-back the transaction rows
	 *         saved
	 * @throws SQLException
	 */
	public boolean update(final DbHandle handle) throws SQLException {
		return this.schema.getDbAssistant().updateAll(handle, this.dataTable);
	}

	/**
	 * insert all rows into the db
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error,
	 *         in which case the caller better roll-back the transaction rows
	 *         saved
	 * @throws SQLException
	 */
	public boolean insert(final DbHandle handle) throws SQLException {
		return this.schema.getDbAssistant().insertAll(handle, this.dataTable);
	}

	/**
	 * save the row into database. if the key is present, it is updated else it
	 * is inserted
	 *
	 * @param handle
	 * @return number true if all rows were saved. false in case of any error,
	 *         in which case the caller better roll-back the transaction rows
	 *         saved
	 * @throws SQLException
	 */
	public boolean save(final DbHandle handle) throws SQLException {
		return this.schema.getDbAssistant().saveAll(handle, this.dataTable);
	}

	/**
	 * @return number of data rows in this data table.
	 */
	public int length() {
		return this.dataTable.length;
	}

}
