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

/**
 * just data structure to collect and pass info fromForm to FormData
 * @author simplity.org
 *
 */
public class FilterSql {
	final String sql;
	final Object[] whereValues;
	final FormDbParam[] whereParams;

	/**
	 * constructor with all attributes
	 *
	 * @param sql
	 *            non-null prepared statement
	 * @param whereParams
	 *            non-null array with valueTypes of parameters in the SQL in the
	 *            right order. Empty array in case the SQL has no parameters.
	 * @param whereValues
	 *            non-null array of values for the parameters in the sql. Each
	 *            element should be of the right type for the corresponding
	 *            parameter.
	 * 
	 */
	public FilterSql(String sql, FormDbParam[] whereParams, Object[] whereValues) {
		this.sql = sql;
		this.whereParams = whereParams;
		this.whereValues = whereValues;
	}
}
