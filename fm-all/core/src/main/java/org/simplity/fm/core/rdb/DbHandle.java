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

import org.simplity.fm.core.data.PreparedStatementParam;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.datatypes.ValueType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class DbHandle {
	private static final Logger logger = LoggerFactory.getLogger(DbHandle.class);
	protected final Connection con;

	/**
	 * to be created by DbDriver ONLY
	 *
	 * @param con
	 * @param readOnly
	 */
	DbHandle(final Connection con) {
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
	 * @param writer
	 * @return number of affected rows.
	 * @throws SQLException
	 */
	public int write(final IDbWriter writer) throws SQLException {
		final String sql = writer.getPreparedStatement();
		if (sql == null) {
			logger.warn(
					"writer {} returned null as prepared statement, indicating taht it does not want to write.. Opertion skipped.",
					writer.getClass().getName());
			return 0;
		}
		logger.info("SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			if (writer.setParams(ps) == false) {
				logger.warn("call back function returned false and hence the write operaiton is abandoned");
				return 0;
			}

			final int n = ps.executeUpdate();
			logger.info("{} rows affected ", n);
			return n;
		}
	}

	/**
	 * @param writer
	 * @param keyColumnName
	 * @param generatedKeys
	 * @return number of affected rows.
	 * @throws SQLException
	 */
	public int insertAndGenerateKey(final IDbWriter writer, final String keyColumnName, final long[] generatedKeys)
			throws SQLException {
		final String[] keys = { keyColumnName };

		final String sql = writer.getPreparedStatement();
		if (sql == null) {
			logger.warn("Writer returned a null SQL, indicating no action.");
			return 0;
		}
		logger.info("Insert With Key SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql, keys)) {
			int result = 0;
			if (writer.setParams(ps)) {
				result = ps.executeUpdate();
				if (result > 0) {
					final long id = getGeneratedKey(ps);
					logger.info("Row iinserted with generated key = {}", id);
					generatedKeys[0] = id;
				} else {
					logger.info("{} rows inserted ", result);
				}
			} else {
				logger.warn("Call back function returned false, and hence insert operation is abandoned");
			}
			return result;
		}
	}

	/**
	 * API that is close to the JDBC API for updating/inserting/deleting
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param params
	 *            parameters to be set the prepared statement
	 * @return number of affected rows. -1 if the driver was unable to
	 *         determine it
	 * @throws SQLException
	 */
	public int write(final String sql, final PreparedStatementParam[] params) throws SQLException {
		logger.info("Generic Write SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			final int posn = 0;
			for (final PreparedStatementParam p : params) {
				p.setPsParam(ps, posn);
			}
			final int n = ps.executeUpdate();
			logger.info("{} rows affected ", n);
			return n;
		}
	}

	/**
	 * execute a prepared statement using Vo as source of parameter values
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param values
	 *            Vo that has the values for the prepared statement
	 * @return number of affected rows. -1 if the driver was unable to
	 *         determine it
	 * @throws SQLException
	 */
	public int write(final String sql, final Record values) throws SQLException {
		logger.info("Generic Write SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			values.setPsParams(ps);
			final int n = ps.executeUpdate();
			logger.info("{} rows affected ", n);
			return n;
		}
	}

	/**
	 * API that is close to the JDBC API for updating/inserting/deleting. You
	 * should consider using the Can be
	 * used if and only if every value is non-null and the objects follow strict
	 * conventions.
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param nonNullvalues
	 *            can be null if sql requires no parameters. Every element in
	 *            the array MUST be non-null. Also, every value MUST be an
	 *            instance of one of our standard classes : String, Long,
	 *            Double, Boolean, LocalDate and Instant
	 * @return number of affected rows. -1 if the driver was unable to
	 *         determine it
	 * @throws SQLException
	 */
	public int write(final String sql, final Object[] nonNullvalues) throws SQLException {
		logger.info("Generic Write SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			int posn = 0;
			for (final Object val : nonNullvalues) {
				posn++;
				ValueType.setObjectAsPsParam(val, ps, posn);
			}
			final int n = ps.executeUpdate();
			logger.info("{} rows affected ", n);
			return n;
		}
	}

	/**
	 * write to the rdbms using a writer
	 *
	 * @param writer
	 * @return number of rows affected
	 * @throws SQLException
	 */
	public int writeMany(final IDbMultipleWriter writer) throws SQLException {

		final String sql = writer.getPreparedStatement();
		if (sql == null) {
			logger.warn(
					"writer {} returned null as prepared statement, indicatiing taht it does not want to write.. Opertion skipped.",
					writer.getClass().getName());
			return 0;
		}
		logger.info("Batch SQL:{}", sql);

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			boolean hasMore = true;
			while (hasMore) {
				hasMore = writer.setParams(ps);
				ps.addBatch();
			}

			return accumulate(ps.executeBatch());
		}
	}

	private static int accumulate(final int[] counts) {
		int n = 0;
		for (final int i : counts) {
			/*
			 * some drivers return -1 indicating inability to get nbr rows
			 * affected
			 */
			if (i < 0) {
				logger.warn("Driver returned -1 as number of rows affected for a batch. assumed to be 1");
				n++;
			} else {
				n += i;
			}
		}
		logger.info("{} rows affected ", n);
		return n;
	}

	/**
	 * use a prepared statement, and values for the parameters to run it
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramValues
	 *            Each element is a non-null array that contains non-null values
	 *            for each parameter in the prepared statement. to be set to the
	 *            prepared statement.
	 * @return number of affected rows. Not reliable. If driver returns -1, we
	 *         assume it to be 1
	 * @throws SQLException
	 */
	public int writeMany(final String sql, final Object[][] paramValues) throws SQLException {
		logger.info("Generic Batch SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (final Object[] row : paramValues) {
				for (int i = 0; i < row.length; i++) {
					ValueType.setObjectAsPsParam(row[i], ps, i + 1);
				}
				ps.addBatch();
			}
			return accumulate(ps.executeBatch());
		}
	}

	/**
	 * use a prepared statement, and values for the parameters to run it
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramValues
	 *            Each element is a non-null array that contains non-null values
	 *            for each parameter in the prepared statement. to be set to the
	 *            prepared statement.
	 * @return number of affected rows. Not reliable. If driver returns -1, we
	 *         assume it to be 1
	 * @throws SQLException
	 */
	public int writeMany(final String sql, final Record[] paramValues) throws SQLException {
		logger.info("Generic Batch SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (final Record row : paramValues) {
				row.setPsParams(ps);
				ps.addBatch();
			}
			return accumulate(ps.executeBatch());
		}
	}

	/**
	 * use a prepared statement, and values for the parameters to run it
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramValues
	 *            Each element is a non-null array that contains non-null values
	 *            for each parameter in the prepared statement. to be set to the
	 *            prepared statement.
	 * @return number of affected rows. Not reliable. If driver returns -1, we
	 *         assume it to be 1
	 * @throws SQLException
	 */
	public int writeMany(final String sql, final List<Record> paramValues) throws SQLException {
		logger.info("Generic Batch SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (final Record row : paramValues) {
				row.setPsParams(ps);
				ps.addBatch();
			}
			return accumulate(ps.executeBatch());
		}
	}

	/**
	 * API that is close to the JDBC API for updating/inserting/deleting
	 *
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramTypes
	 *            type of parameters to be set the prepared statement
	 * @param paramValues
	 *            values to be set to the prepared statement. these will be
	 *            executed in a batch
	 * @return number of affected rows, on element per batch. -1 implies
	 *         that the driver was unable to determine it
	 * @throws SQLException
	 */
	public int[] writeMany(final String sql, final ValueType[] paramTypes, final Object[][] paramValues)
			throws SQLException {
		logger.info("Generic Batch SQL:{}", sql);
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (final Object[] row : paramValues) {
				for (int i = 0; i < paramTypes.length; i++) {
					paramTypes[i].setPsParam(ps, i + 1, row[i]);
				}
				ps.addBatch();
			}
			return ps.executeBatch();
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

	private static long getGeneratedKey(final PreparedStatement ps) throws SQLException {
		try (ResultSet rs = ps.getGeneratedKeys()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			throw new SQLException("Driver failed to return a generated key ");
		}
	}

	static void warn(final String sql, final ValueType[] types, final Object[] vals) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append("RDBMS is not set up. Sql = ").append(sql);
		for (int i = 0; i < types.length; i++) {
			sbf.append('(').append(types[i]).append(", ").append(vals[i]).append(") ");
		}
		logger.warn(sbf.toString());
	}

	static void warn(final String sql) {
		logger.error("RDBMS is not set up. Sql = ", sql);
	}
}
