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

import org.simplity.fm.core.datatypes.ValueType;

/**
 * a sql parameter that is used for input (for setting value to a
 * preparedStatment) or output (extracting value from a result set)
 *
 * This is an immutable class.
 *
 * @author simplity.org
 *
 */
public class SqlParam {
	protected final String name;
	protected final ValueType valueType;
	protected final int idx;

	/**
	 * construct an immutable parameter
	 *
	 * @param name
	 * @param valueType
	 * @param idx
	 */
	public SqlParam(final String name, final ValueType valueType, final int idx) {
		this.name = name;
		this.valueType = valueType;
		this.idx = idx;
	}
}
