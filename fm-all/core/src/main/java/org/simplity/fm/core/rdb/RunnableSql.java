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
 * represents a sql with required data input/output to be used at run time for
 * an interaction with the RDBMS for a given sql.
 *
 * a concrete class is generated for each concrete generated Sql class.
 *
 * @author simplity.org
 *
 */
public class RunnableSql {
	protected final Sql sql;
	protected final Object[] inputData;
	protected final Object[] outputData;

	private boolean isExecuted;
	private int nbrAffetedRows;

	protected RunnableSql(final Sql sql) {
		this.sql = sql;
		if (sql.isDml) {
			this.outputData = null;
		} else {
			this.outputData = new Object[sql.outputParams.length];
		}
		if (sql.inputParams == null) {
			this.inputData = null;
		} else {
			this.inputData = new Object[sql.inputParams.length];
		}
	}

	protected void assignInput(final int idx, final Object value) {
		this.inputData[idx] = value;
	}

	protected Object extractOutput(final int idx) {
		return this.outputData[idx];
	}

}
