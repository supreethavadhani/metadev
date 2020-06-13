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

import java.sql.SQLException;

/**
 * interface for a class that wants to do db operations in batch. That is, more
 * than one transaction.
 * In this case, the client manages transactions (begin-trans, commit and
 * roll-back)
 *
 * NOTE: This interface is created because the java.util.functions can not
 * declare exceptions. Our function needs to declare a throws clause
 *
 * @author simplity.org
 *
 */
@FunctionalInterface
public interface DbTransacter {

	/**
	 * function that manages its own transactions, like commit and roll-back. It
	 * is also possible to do the read-writes with auto-commits
	 *
	 * @param handle
	 * @throws SQLException
	 */
	void transact(TransactionHandle handle) throws SQLException;
}
