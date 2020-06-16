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

import org.simplity.fm.core.conf.IDbConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Driver to deal with RDBMS read/write operations. Note that we expose
 * much-higher level APIs that the JDBC driver. And, of course we provide the
 * very basic feature : read/write. That is the whole idea of this class -
 * provide simple API to do the most common operation
 *
 * This is an immutable class, and hence can be used as a singleton. This is
 * designed to be accessed through App
 *
 * @author simplity.org
 *
 */
public class RdbDriver {
	protected static final Logger logger = LoggerFactory.getLogger(RdbDriver.class);

	private final IDbConnectionFactory factory;

	/**
	 * to be used by APP, and no one else..
	 *
	 * @param factory
	 */
	public RdbDriver(final IDbConnectionFactory factory) {
		this.factory = factory;
		//
	}

	/**
	 * do read-only operation on the rdbms
	 *
	 * @param reader
	 *            function that reds from the db
	 * @throws SQLException
	 *
	 */
	public void read(final DbReader reader) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			doReadOnly(con, reader);
		}
	}

	/**
	 * do read-only operations using a specific schema name
	 *
	 * @param reader
	 *            function that reds from the db
	 * @param schemaName
	 *            non-null schema name that is different from the default schema
	 * @throws SQLException
	 *
	 */
	public void read(final String schemaName, final DbReader reader) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			doReadOnly(con, reader);
		}
	}

	/**
	 * do read-write operations on the rdbms within a transaction boundary. The
	 * transaction is managed by the driver.
	 *
	 * @param updater
	 *            function that reads from db and writes to it within a
	 *            transaction boundary. returns true to commit the transaction,
	 *            or false to signal a roll-back. The transaction is rolled back
	 *            on exceptions as well.
	 * @throws SQLException
	 */
	public void readWrite(final DbWriter updater) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			doReadWrite(con, updater);
		}
	}

	/**
	 * do read-write-operations in a transaction using a specific schema name
	 *
	 * @param schemaName
	 *            non-null schema name that is different from the default schema
	 *            do read-write operations on the rdbms within a transaction
	 *            boundary. The
	 *            transaction is managed by the driver.
	 *
	 * @param updater
	 *            function that reads from db and writes to it within a
	 *            transaction boundary. returns true to commit the transaction,
	 *            or false to signal a roll-back. The transaction is rolled back
	 *            on exceptions as well.
	 * @throws SQLException
	 *
	 */
	public void readWrite(final String schemaName, final DbWriter updater) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			doReadWrite(con, updater);
		}
	}

	/**
	 * Meant for db operations that are to be committed/rolled-back possibly
	 * more than once. Of course, it is rolled-back if the caller throws any
	 * exception
	 *
	 * @param transacter
	 *            function that accesses the db with transactions managed with
	 *            commit/roll-back or with auto-commit mode. Driver does not do
	 *            any transaction management.
	 * @throws SQLException
	 *             if update is attempted after setting readOnly=true, or any
	 *             other SqlException
	 *
	 */
	public void transact(final DbTransacter transacter) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection()) {
			doBatch(con, transacter);
		}
	}

	/**
	 * Meant for db operations that are to be committed/rolled-back possibly
	 * more than once. Of course, it is rolled-back if the caller throws any
	 * exception
	 *
	 * @param schemaName
	 *            name of the non-default schema to be used in the db
	 * @param transacter
	 *            function that accesses the db with transactions managed with
	 *            commit/roll-back or with auto-commit mode. Driver does not do
	 *            any transaction management.
	 * @throws SQLException
	 *             if update is attempted after setting readOnly=true, or any
	 *             other SqlException
	 *
	 */
	public void transact(final String schemaName, final DbTransacter transacter) throws SQLException {
		this.checkFactory();
		try (Connection con = this.factory.getConnection(schemaName)) {
			doBatch(con, transacter);
		}
	}

	private void checkFactory() throws SQLException {
		if (this.factory == null) {
			final String msg = "Db driver is not set up for this application. No db operations are possible";
			logger.error(msg);
			throw new SQLException(msg);
		}
	}

	private static void doReadOnly(final Connection con, final DbReader reader) throws SQLException {
		final ReadonlyHandle handle = new ReadonlyHandle(con);
		try {
			con.setReadOnly(true);
			reader.read(handle);
		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred in the middle of a transaction: {}, {}", e, e.getMessage());
			throw new SQLException(e.getMessage());
		}
	}

	private static void doReadWrite(final Connection con, final DbWriter updater) throws SQLException {
		final ReadWriteHandle handle = new ReadWriteHandle(con);
		try {
			con.setAutoCommit(false);
			if (updater.readWrite(handle)) {
				con.commit();
			} else {
				con.rollback();
			}

		} catch (final Exception e) {
			e.printStackTrace();
			logger.error("Exception occurred in the middle of a transaction: {}, {}", e, e.getMessage());
			try {
				con.rollback();
			} catch (final Exception ignore) {
				//
			}
			throw new SQLException(e.getMessage());
		}
	}

	private static void doBatch(final Connection con, final DbTransacter transacter) throws SQLException {
		final TransactionHandle handle = new TransactionHandle(con);
		try {
			transacter.transact(handle);
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
}