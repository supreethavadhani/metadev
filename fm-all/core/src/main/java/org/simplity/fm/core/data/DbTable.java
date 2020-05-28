/*
 * Copyright (c) 2020 simplity.org
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.serialize.ISerializer;

/**
 * Represents an array of <code>DbRecord</code>. This wrapper class is created
 * to provide db/persistence related functionalities
 *
 * @author simplity.org
 * @param <T>
 *            DbRecord rows this class is to contain
 *
 */
public class DbTable<T extends DbRecord> implements Iterable<T> {
	private final T record;
	private List<Object[]> rows = new ArrayList<>();

	/**
	 * construct with an instance of the underlying dbRecord
	 *
	 * @param record
	 */
	public DbTable(final T record) {
		this.record = record;
	}

	/**
	 * add a record
	 *
	 * @param rec
	 */
	public void addRecord(final T rec) {
		this.rows.add(rec.fieldValues.clone());
	}

	protected void addRow(final Object[] row) {
		this.rows.add(row);
	}

	/**
	 * clear all existing data
	 */
	public void clear() {
		this.rows.clear();
	}

	/**
	 * @return number of data rows in this data table.
	 */
	public int length() {
		return this.rows.size();
	}

	/**
	 * serialized into an array [{},{}....]
	 *
	 * @param writer
	 * @throws IOException
	 */
	public void serializeRows(final ISerializer writer) throws IOException {
		writer.beginArray();
		for (final T rec : this) {
			writer.beginObject();
			writer.fields(rec);
			writer.endObject();
		}
		writer.endArray();
	}

	/**
	 * To be used by utility programs. End-programmers should not use as this is
	 * not type-safe. ENd-programmers should use FilterSqls instead
	 *
	 * @param whereClauseStartingWithWhere
	 * @param valuesForWhereClause
	 * @param handle
	 * @return true if at least row is filtered. false if no rows.
	 * @throws SQLException
	 */
	public boolean filter(final String whereClauseStartingWithWhere, final Object[] valuesForWhereClause,
			final DbHandle handle) throws SQLException {
		this.rows = this.record.dba.filter(whereClauseStartingWithWhere, valuesForWhereClause, handle);
		return this.rows.size() > 0;
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
		return this.record.dba.insertAll(handle, this.rows.toArray(new Object[0][]));
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
		return this.record.dba.updateAll(handle, this.rows.toArray(new Object[0][]));
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
		return this.record.dba.saveAll(handle, this.rows.toArray(new Object[0][]));
	}

	/**
	 * fetch is used instead of get to avoid clash with getters in generated
	 * classes
	 * 
	 * @param idx
	 * @return record at 0-based index. null if the index is not valid
	 */
	@SuppressWarnings("unchecked")
	public T fetchRecord(final int idx) {
		final Object[] row = this.rows.get(idx);
		if (row == null) {
			return null;
		}
		return (T) this.record.newInstance(row);
	}

	@Override
	public Iterator<T> iterator() {
		final List<Object[]> r = this.rows;
		return new Iterator<T>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return this.idx < r.size();
			}

			@Override
			public T next() {
				return DbTable.this.fetchRecord(this.idx++);
			}
		};
	}

}
