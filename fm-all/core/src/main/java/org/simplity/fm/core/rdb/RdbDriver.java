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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver to deal with RDBMS read/write operations. Note that we expose
 * much-higher level APIs that the JDBC driver. And, of course we provide the
 * very basic feature : read/write. That is the whole idea of this class -
 * provide simple API to do the most common operation
 *
 * @author simplity.org
 *
 */
public class RdbDriver {
	protected static final Logger logger = LoggerFactory.getLogger(RdbDriver.class);
	/*
	 * factory..
	 */
	private static IConnectionFactory factory = null;

	/**
	 *
	 * @return db driver
	 * @throws SQLException
	 *             in case of any exception while dealing with the rdbms
	 */
	public static RdbDriver getDriver() throws SQLException {
		return new RdbDriver();
	}

	private RdbDriver() {
		//
	}

	/**
	 * @param batchClient
	 * @throws SQLException
	 *             if update is attempted after setting readOnly=true, or any
	 *             other SqlException
	 *
	 */
	@SuppressWarnings("static-method") // instance has no attributes, but have
										// made it instance for future sake
	public void transactBatch(final IDbBatchClient batchClient) throws SQLException {
		if (factory == null) {
			final String msg = "A dummy handle is returned as RDBMS is not set up";
			logger.error(msg);
			throw new SQLException(msg);
		}
		try (Connection con = factory.getConnection()) {
			doBatch(con, batchClient);
		}
	}

	/**
	 * @param transactor
	 * @param readOnly
	 *            true if the caller is not going to modify any data.
	 * @throws SQLException
	 *             if update is attempted after setting readOnly=true, or any
	 *             other SqlException
	 *
	 */
	@SuppressWarnings("static-method") // instance has no attributes, but have
										// made it instance for future sake
	public void transact(final IDbClient transactor, final boolean readOnly) throws SQLException {
		if (factory == null) {
			final String msg = "A dummy handle is returned as RDBMS is not set up";
			logger.error(msg);
			throw new SQLException(msg);
		}
		try (Connection con = factory.getConnection()) {
			doTransact(con, transactor, readOnly);
		}
	}

	private static void doTransact(final Connection con, final IDbClient transactor, final boolean readOnly)
			throws SQLException {
		final DbHandle handle = new DbHandle(con);
		try {
			if (readOnly) {
				con.setReadOnly(true);
				transactor.transact(handle);
			} else {
				con.setAutoCommit(false);
				if (transactor.transact(handle)) {
					con.commit();
				} else {
					con.rollback();
				}
			}
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred in the middle of a transaction: {}, {}", e, e.getMessage());
			if (!readOnly) {
				try {
					con.rollback();
				} catch (final Exception ignore) {
					//
				}
			}
			throw new SQLException(e.getMessage());
		}

	}

	private static void doBatch(final Connection con, final IDbBatchClient batchClient) throws SQLException {
		final DbBatchHandle handle = new DbBatchHandle(con);
		try {
			batchClient.doBatch(handle);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Exception thrown by a batch processor. {}, {}", e, e.getMessage());
			final SQLException se = new SQLException(e.getMessage());
			try {
				con.rollback();
			} catch (final Exception ignore) {
				//
			}
			throw se;
		}

	}

	/**
	 * do transaction on a schema that is not the default schema used by this
	 * application. Use this ONLY id the schema is different from the default
	 *
	 * @param transactor
	 * @param readOnly
	 *            true if the caller is not going to modify any data.
	 * @param schemaName
	 *            non-null schema name that is different from the default schema
	 * @throws SQLException
	 *             if update is attempted after setting readOnly=true, or any
	 *             other SqlException
	 *
	 */
	public void transactUsingSchema(final IDbClient transactor, final boolean readOnly, final String schemaName)
			throws SQLException {
		if (factory == null) {
			final String msg = "A dummy handle is returned as RDBMS is not set up";
			logger.error(msg);
			throw new SQLException(msg);
		}
		try (Connection con = factory.getConnection(schemaName)) {
			doTransact(con, transactor, readOnly);
		}
	}

	/**
	 * @param conFactory
	 *            non-null factory to be used to get db-connection
	 */
	public static void setFactory(final IConnectionFactory conFactory) {
		factory = conFactory;
	}
}