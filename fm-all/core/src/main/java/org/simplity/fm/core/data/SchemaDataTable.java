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
public abstract class SchemaDataTable implements Iterable<SchemaData> {
	protected static final Logger logger = LoggerFactory.getLogger(SchemaDataTable.class);

	protected final Schema schema;
	protected Object[][] fieldValues = new Object[0][];

	/**
	 *
	 * @param schema
	 * @param values
	 */
	protected SchemaDataTable(final Schema schema, final Object[][] values) {
		this.schema = schema;
		if (values != null) {
			this.fieldValues = values;
		}
	}

	/**
	 *
	 * @param idx
	 * @return data row. null if the index is out of range
	 */
	protected SchemaData getSchemaData(final int idx) {
		return this.schema.newSchemaData(this.fieldValues[idx]);
	}

	/**
	 *
	 * @return data row. null if the index is out of range
	 */
	public Object[][] getRawData() {
		return this.fieldValues;
	}

	/**
	 * iterator for data rows
	 */
	@Override
	public Iterator<SchemaData> iterator() {
		return new Iterator<SchemaData>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < SchemaDataTable.this.fieldValues.length;
			}

			@Override
			public SchemaData next() {
				return SchemaDataTable.this.getSchemaData(this.idx++);
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
		for (final Object[] row : this.fieldValues) {
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
	 * @param whereClauseStartingWithWhere
	 *            e.g. "WHERE a=? and b=?" null if all rows are to be read. Best
	 *            practice is to use parameters rather than dynamic sql. That is
	 *            you should use a=? rather than a = 32
	 * @param values
	 *            null or empty if where-clause is null or has no parameters.
	 *            every element MUST be non-null and must be one of the standard
	 *            objects we use String, Long, Double, Boolean, LocalDate,
	 *            Instant
	 * @return true if at least one row is read
	 * @throws SQLException
	 */
	public boolean filter(final DbHandle handle, final String whereClauseStartingWithWhere, final Object[] values)
			throws SQLException {
		this.fieldValues = this.schema.getDbAssistant().filter(whereClauseStartingWithWhere, values, false, handle);
		return (this.fieldValues.length > 0);
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
		return this.schema.getDbAssistant().updateAll(handle, this.fieldValues);
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
		return this.schema.getDbAssistant().insertAll(handle, this.fieldValues);
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
		return this.schema.getDbAssistant().saveAll(handle, this.fieldValues);
	}

	/**
	 * @return number of data rows in this data table.
	 */
	public int length() {
		return this.fieldValues.length;
	}
}
