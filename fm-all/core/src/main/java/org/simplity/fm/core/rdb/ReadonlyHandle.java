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

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.datatypes.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Db Handle that allows read access to the underlying RDBMS. No writes are
 * allowed.
 *
 * @author simplity.org
 *
 */
public class ReadonlyHandle {
	private static final Logger logger = LoggerFactory.getLogger(ReadonlyHandle.class);
	protected final Connection con;

	/**
	 * to be created by DbDriver ONLY
	 *
	 * @param con
	 * @param readOnly
	 */
	ReadonlyHandle(final Connection con) {
		this.con = con;
	}

	/**
	 * method to be used to read into a valueObject using a sql component.
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param inputData
	 *            null if the prepared statement has no parameters. must contain
	 *            the right values in the right order
	 * @param outputData
	 *            non-null. must have the right fields in the right order to
	 *            receive data from the result set
	 * @return true if a row was indeed read. false otherwise
	 * @throws SQLException
	 */
	public boolean read(final String sql, final Record inputData, final Record outputData) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (inputData != null) {
				inputData.setPsParams(ps);
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					outputData.readFromRs(rs);
					return true;
				}
				return false;
			}
		}
	}

	/**
	 * method to be used to read into a valueObject using a sql component.
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param paramValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right non-values in the right order for parameters in the
	 *            select sql
	 * @param outputTypes
	 *            non-null. must have the right types in the right order to
	 *            receive data from the result set
	 * @return extracted data as an array of objects. null if no row is read
	 * @throws SQLException
	 */
	public Object[] read(final String sql, final Object[] paramValues, final ValueType[] outputTypes)
			throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (paramValues != null) {
				int posn = 0;
				for (final Object val : paramValues) {
					posn++;
					if (val == null) {
						throw new SQLException(
								"Value at " + posn + " is null. Input parameter values must be non-null");
					}
					ValueType.setObjectAsPsParam(val, ps, posn);
				}
			}
			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				final Object[] result = new Object[outputTypes.length];
				for (int i = 0; i < outputTypes.length; i++) {
					final ValueType vt = outputTypes[i];
					result[i] = vt.getFromRs(rs, i + 1);
				}
				return result;
			}
		}
	}

	/**
	 * method to be used to read into a valueObject using a sql component.
	 *
	 * @param sql
	 *            non-null valid prepared statement to read from the database
	 * @param paramValues
	 *            null if the prepared statement has no parameters. must contain
	 *            the right non-values in the right order for parameters in the
	 *            select sql
	 * @param outputTypes
	 *            non-null. must have the right types in the right order to
	 *            receive data from the result set
	 * @return extracted data as an array of rows. null if no row is read
	 * @throws SQLException
	 */
	public Object[][] filter(final String sql, final Object[] paramValues, final ValueType[] outputTypes)
			throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (paramValues != null) {
				int posn = 0;
				for (final Object val : paramValues) {
					posn++;
					if (val == null) {
						throw new SQLException(
								"Value at " + posn + " is null. Input parameter values must be non-null");
					}
					ValueType.setObjectAsPsParam(val, ps, posn);
				}
			}
			try (ResultSet rs = ps.executeQuery()) {
				final List<Object[]> result = new ArrayList<>();
				while (rs.next()) {
					final Object[] row = new Object[outputTypes.length];
					result.add(row);
					for (int i = 0; i < outputTypes.length; i++) {
						final ValueType vt = outputTypes[i];
						row[i] = vt.getFromRs(rs, i + 1);
					}
				}
				if (result.size() == 0) {
					return null;
				}
				return result.toArray(new Object[0][]);
			}
		}
	}

	/**
	 * method to be used to read possibly more than one rows into a valueTable
	 * using a prepared statement
	 *
	 * @param sql
	 *            non-null valid prepared statement for reading from the
	 *            database
	 * @param inputData
	 *            null if the prepared statement has no parameters. must contain
	 *            the right values in the right order
	 * @param outputInstance
	 *            an instance of the VO for output. This instance is not
	 *            modified, but used to create instances of new VOs
	 * @return list of output Vos. could be empty, but not null
	 * @throws SQLException
	 */
	public <T extends Record> List<T> filter(final String sql, final Record inputData, final T outputInstance)
			throws SQLException {

		logger.info("Filter called with T = {} ", outputInstance.getClass().getName());

		final List<T> list = new ArrayList<>();
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (inputData != null) {
				inputData.setPsParams(ps);
			}
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					@SuppressWarnings("unchecked")
					final T vo = (T) outputInstance.newInstance();
					logger.info("New instance of {} created for filtering", vo.getClass().getName());
					vo.readFromRs(rs);
					list.add(vo);
				}
			}
		}
		return list;
	}

	/**
	 * Most flexible way to read from db. Caller has full control of what
	 * and how to read.
	 *
	 * @param reader
	 *            instance that wants to read from the database
	 * @return number of rows actually read by the reader.
	 * @throws SQLException
	 *
	 */
	public int read(final IDbReader reader) throws SQLException {
		final String sql = reader.getPreparedStatement();
		if (sql == null) {
			return 0;
		}

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			reader.setParams(ps);
			try (ResultSet rs = ps.executeQuery()) {
				int n = 0;
				while (rs.next()) {
					if (reader.readARow(rs) == false) {
						break;
					}
					n++;
				}
				return n;
			}
		}
	}

	/**
	 *
	 * @return blob object
	 * @throws SQLException
	 */
	public Clob createClob() throws SQLException {
		return this.con.createClob();
	}

	/**
	 *
	 * @return blob object
	 * @throws SQLException
	 */
	public Blob createBlob() throws SQLException {
		return this.con.createBlob();
	}

	protected static void warn(final String sql, final ValueType[] types, final Object[] vals) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("RDBMS is not set up. Sql = ").append(sql);
		for (int i = 0; i < types.length; i++) {
			sbf.append('(').append(types[i]).append(", ").append(vals[i]).append(") ");
		}
		logger.warn(sbf.toString());
	}

	protected static void warn(final String sql) {
		logger.error("RDBMS is not set up. Sql = ", sql);
	}
}
