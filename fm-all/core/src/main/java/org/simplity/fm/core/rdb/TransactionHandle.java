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

import java.sql.Connection;
import java.sql.SQLException;

/**
 * db handle that allows multiple transactions.
 *
 * @author simplity.org
 *
 */
public class TransactionHandle extends ReadWriteHandle {

	/**
	 * @param con
	 */
	TransactionHandle(final Connection con) {
		super(con);
	}

	/**
	 * turn on/off auto commit mode. If it is on, commit/roll-backs are not
	 * valid
	 *
	 * @param mode
	 * @throws SQLException
	 */
	public void setAutoCommitMode(final boolean mode) throws SQLException {
		this.con.setAutoCommit(mode);
	}

	/**
	 * commit all write operations after the last commit/roll-back
	 *
	 * @throws SQLException
	 */
	public void commit() throws SQLException {
		this.con.commit();
	}

	/**
	 * roll back any writes. This is to be used only to handle any exception. We
	 * strongly suggest that this should never be called by design.
	 *
	 * @throws SQLException
	 */
	public void rollback() throws SQLException {
		this.con.rollback();
	}

}
