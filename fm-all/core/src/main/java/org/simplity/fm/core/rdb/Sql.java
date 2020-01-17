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

/**
 * A component that is used to interact with the data base either for data
 * retrieval or data manipulation.
 *
 * We insist that the programmers use prepared statement, but not dynamic sql.
 * This abstract class is extended to generate sqls in a project
 *
 * @author simplity.org
 *
 */
public abstract class Sql {
	protected final String sql;
	protected final SqlParam[] inputParams;
	protected final SqlParam[] outputParams;
	protected final boolean isDml;

	/**
	 *
	 * @param sql
	 *            non-null and valid prepared statement. It is used to create a
	 *            result-set with extracted data if the output parameters are
	 *            specified. Used as a DMl if outputParamaters is set to null
	 * @param inputParams
	 *            null if the prepared statement has no parameters. Contains
	 *            parameters corresponding to the params defined the prepared
	 *            statement, in that order
	 * @param outputParams
	 *            null if the sql is a DML. Otherwise params in the right order
	 *            of parameters in the result set.
	 */
	public Sql(final String sql, final SqlParam[] inputParams, final SqlParam[] outputParams) {
		this.sql = sql;
		this.inputParams = inputParams;
		if (outputParams == null || outputParams.length == 0) {
			this.outputParams = null;
			this.isDml = true;
		} else {
			this.outputParams = outputParams;
			this.isDml = false;
		}
	}
}
