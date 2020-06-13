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

package org.simplity.fm.core;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Provide a SQL connection for the RDBMS service used by this app
 * 
 * @author simplity.org
 *
 */
public interface IDbConnectionFactory {
	/**
	 *
	 * @return non-null sql connection for default schema or this application.
	 * @throws SQLException
	 *             if no driver is set-up, or there is some problem in getting a
	 *             connection
	 */
	Connection getConnection() throws SQLException;

	/**
	 * to be used to get a connection to a schema that is not the default for
	 * the application
	 *
	 * @param schema
	 *            non-null schema name.
	 * @return non-null sql connection for default schema or this application.
	 * @throws SQLException
	 *             if no driver is set-up, or there is some problem in getting a
	 *             connection
	 */
	Connection getConnection(String schema) throws SQLException;
}
