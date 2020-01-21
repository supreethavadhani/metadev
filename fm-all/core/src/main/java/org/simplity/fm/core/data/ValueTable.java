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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.simplity.fm.core.JsonUtil;

import com.google.gson.stream.JsonWriter;

/**
 * Represents an array of generic Value object
 *
 * @author simplity.org
 * @param <T>
 *
 */
public class ValueTable<T extends ValueObject> implements Iterable<T> {
	protected final T vo;
	protected Object[][] dataTable = new Object[0][];

	/**
	 * create a Vo with known set of values. Caller MUST ensure that the field
	 * values are appropriate for the fields.
	 *
	 * @param vo
	 *            non-null value object
	 */
	public ValueTable(final T vo) {
		this.vo = vo;
	}

	/**
	 *
	 * @return number of rows in this table
	 */
	public int length() {
		return this.dataTable.length;
	}

	/**
	 * @return field values
	 */
	public Object[][] getRawData() {
		return this.dataTable;
	}

	/**
	 *
	 * @param idx
	 * @return Vo at this index. null if the index@SuppressWarnings("unchecked")
	 *         is not within range
	 */
	public T getVo(final int idx) {
		if (idx >= 0 && idx < this.dataTable.length) {
			@SuppressWarnings("unchecked")
			final T result = (T) this.vo.copy();
			result.fieldValues = this.dataTable[idx];
			return result;
		}
		return null;
	}

	/**
	 * to be used as value for a name. It writes an array of objects. Caller
	 * MUST not start/end array
	 *
	 * @param writer
	 * @throws IOException
	 */
	public void serializeRows(final JsonWriter writer) throws IOException {
		writer.beginArray();
		for (final Object[] row : this.dataTable) {
			writer.beginObject();
			JsonUtil.writeFields(this.vo.fields, row, writer);
			writer.endObject();
		}
		writer.endArray();
	}

	/**
	 * set parameter values to a prepared statement that uses this Vo as input
	 * source.
	 *
	 * @param ps
	 * @throws SQLException
	 */
	public void setPsBatchParams(final PreparedStatement ps) throws SQLException {
		for (final Object[] row : this.dataTable) {
			for (int i = 0; i < row.length; i++) {
				new ValueObject(this.vo.fields, row).setPsParams(ps);
			}
			ps.addBatch();
		}
	}

	/**
	 * read values from a result set for which this VO is designed as output
	 * data structure
	 *
	 * @param rs
	 * @return number of rows read
	 * @throws SQLException
	 */
	public int readFromRs(final ResultSet rs) throws SQLException {
		final List<Object[]> rows = new ArrayList<>();
		final int nbr = this.vo.fields.length;
		while (rs.next()) {
			final Object[] row = new Object[nbr];
			rows.add(row);
			int idx = 0;
			for (final Field field : this.vo.fields) {
				row[idx] = field.getValueType().getFromRs(rs, idx + 1);
				idx++;
			}
		}
		this.dataTable = rows.toArray(new Object[0][]);
		return this.dataTable.length;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int idx = 0;
			private final int limit = ValueTable.this.dataTable.length;

			@Override
			public boolean hasNext() {
				return this.idx < this.limit;
			}

			@Override
			public T next() {
				return ValueTable.this.getVo(this.idx++);
			}
		};
	}
}
