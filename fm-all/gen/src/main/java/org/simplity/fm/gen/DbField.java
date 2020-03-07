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

package org.simplity.fm.gen;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.data.ColumnType;

/**
 * represents a Field row in fields sheet of a forms work book
 *
 * @author simplity.org
 *
 */
class DbField extends Field {
	private static final Map<String, ColumnType> columnTypes = createMap();
	String dbColumnName;
	private String columnType;

	@Override
	protected void emitJavaSpecific(final StringBuilder sbf) {
		sbf.append(C).append(Util.escape(this.dbColumnName));
		sbf.append(C);
		final ColumnType ct = this.getColumnType();
		if (ct == null) {
			sbf.append("null");
		} else {
			sbf.append("ColumnType.").append(ct.name());
		}
	}

	/**
	 * @return column type, or null.
	 */
	public ColumnType getColumnType() {
		if (this.columnType == null) {
			return ColumnType.OptionalData;
		}
		final ColumnType ct = columnTypes.get(this.columnType.toLowerCase());
		if (ct != null) {
			return ct;
		}
		logger.error("{} is an invalid columnType for field {}. optional data is  assumed", this.columnType, this.name);
		return ColumnType.OptionalData;
	}

	/**
	 * @return
	 */
	private static Map<String, ColumnType> createMap() {
		final Map<String, ColumnType> map = new HashMap<>();
		for (final ColumnType vt : ColumnType.values()) {
			map.put(vt.name().toLowerCase(), vt);
		}
		return map;
	}
}
