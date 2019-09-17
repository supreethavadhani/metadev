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

import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.form.FormDbParam;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class DbHandle {
	private static final Logger logger = LoggerFactory.getLogger(DbHandle.class);
	private final Connection con;

	/**
	 * to be created by DbDriver ONLY
	 * 
	 * @param con
	 * @param readOnly
	 */
	DbHandle(Connection con) {
		this.con = con;
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
	public int read(IDbReader reader) throws SQLException {
		String  sql = reader.getPreparedStatement();
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
				logger.info("{} rows read using read()", n);
				return n;
			}
		}
	}

	/**
	 * lower level API that is very close to the JDBC API for reading one
	 * row from the result of a select query
	 * 
	 * @param sql
	 *            a prepared statement
	 * @param whereTypes
	 *            parameter types of where clause. These are the parameters
	 *            set to the prepared statement before getting the result
	 *            set from the prepared statement
	 * @param whereData
	 *            must have the same number of elements as in whereTypes.
	 *            Values to be set to the prepared statement
	 * @param resultTypes
	 *            this array has one element for each parameter expected in
	 *            the output result set. Values are extracted from the
	 *            result set based on these types
	 * @return an array of object values extracted from the result set. null
	 *         if result-set had no rows
	 * @throws SQLException
	 */
	public Object[] read(String sql, ValueType[] whereTypes, Object[] whereData, ValueType[] resultTypes)
			throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			int posn = 0;
			for (ValueType vt : whereTypes) {
				Object val = whereData[posn];
				posn++;
				vt.setPsParam(ps, posn, val);
			}

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				Object[] result = new Object[resultTypes.length];
				for (int i = 0; i < resultTypes.length; i++) {
					result[i] = resultTypes[i].getFromRs(rs, i + 1);
				}
				return result;
			}
		}
	}


	/**
	 * lower level API that is very close to the JDBC API for reading all
	 * rows from the result of a select query
	 * 
	 * @param sql
	 *            a prepared statement
	 * @param whereTypes
	 *            parameter types of where clause. These are the parameters
	 *            set to the prepared statement before getting the result
	 *            set from the prepared statement
	 * @param whereData
	 *            must have the same number of elements as in whereTypes.
	 *            Values to be set to the prepared statement
	 * @param resultTypes
	 *            this array has one element for each parameter expected in
	 *            the output result set. Values are extracted from the
	 *            result set based on these types
	 * @return an array of rows from the result set. Each row is an array of
	 *         object values from the result set row.
	 * @throws SQLException
	 */
	public Object[][] readRows(String sql, ValueType[] whereTypes, Object[] whereData, ValueType[] resultTypes)
			throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			int posn = 0;
			for (ValueType vt : whereTypes) {
				Object val = whereData[posn];
				posn++;
				vt.setPsParam(ps, posn, val);
			}

			List<Object[]> result = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Object[] row = new Object[resultTypes.length];
					result.add(row);
					for (int i = 0; i < resultTypes.length; i++) {
						row[i] = resultTypes[i].getFromRs(rs, i + 1);
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
	 * @param writer
	 * @return number of affected rows.
	 * @throws SQLException
	 */
	public int write(IDbWriter writer) throws SQLException {
		String sql = writer.getPreparedStatement();
		if (sql == null) {
			logger.warn(
					"writer {} returned null as prepared statement, indicatiing taht it does not want to write.. Opertion skipped.",
					writer.getClass().getName());
			return 0;
		}

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			writer.setParams(ps);
			return ps.executeUpdate();
		}
	}

	/**
	 * write to the rdbms using a writer
	 * @param writer
	 * @return number of rows affected
	 * @throws SQLException
	 */
	public int write(IDbBatchWriter writer) throws SQLException {
		
		String sql = writer.getPreparedStatement();
		if (sql == null) {
			logger.warn(
					"writer {} returned null as prepared statement, indicatiing taht it does not want to write.. Opertion skipped.",
					writer.getClass().getName());
			return 0;
		}

		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			do {
				ps.addBatch();
			} while (writer.setParams(ps));
			int[] nbrs = ps.executeBatch();
			int n = 0;
			for (int i : nbrs) {
				/*
				 * some drivers return -1 indicating inability to get nbr rows
				 * affected
				 */
				if (i < 0) {
					n = 1;
					break;
				}
				n += i;
			}
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
	public int insertAndGenerteKeys(IDbWriter writer, String keyColumnName, long[] generatedKeys) throws SQLException {
		String[] keys = { keyColumnName };

		try (PreparedStatement ps = this.con.prepareStatement(writer.getPreparedStatement(), keys)) {
			writer.setParams(ps);
			int result = ps.executeUpdate();
			if (result > 0) {
				generatedKeys[0] = getGeneratedKey(ps);
			}
			return result;
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
	 *            values to be set to the prepared statement
	 * @return number of affected rows. -1 if the driver was unable to
	 *         determine it
	 * @throws SQLException
	 */
	public int write(String sql, ValueType[] paramTypes, Object[] paramValues) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {

			for (int i = 0; i < paramValues.length; i++) {
				paramTypes[i].setPsParam(ps, i + 1, paramValues[i]);
			}
			return ps.executeUpdate();
		}
	}

	/**
	 * API that is close to the JDBC API for insert operation that requires
	 * generated key to be retrieved
	 * 
	 * @param sql
	 *            a prepared statement that manipulates data.
	 * @param paramTypes
	 *            type of parameters to be set the prepared statement
	 * @param params
	 *            values to be set to the prepared statement
	 * @param generatedKeys
	 *            non-null array. generated key is returned in the first element
	 * @param keyName
	 *            non-null name of the key-column that is generated
	 * @return number of affected rows. -1 if the driver was unable to
	 *         determine it
	 * @throws SQLException
	 */
	public int insert(String sql, ValueType[] paramTypes, Object[] params, long[] generatedKeys, String keyName)
			throws SQLException {
		return doWrite(this.con, sql, paramTypes, params, generatedKeys, keyName);
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
	public int[] writeBatch(String sql, ValueType[] paramTypes, Object[][] paramValues) throws SQLException {
		try (PreparedStatement ps = this.con.prepareStatement(sql)) {
			for (Object[] row : paramValues) {
				ps.addBatch();
				for (int i = 0; i < paramValues.length; i++) {
					paramTypes[i].setPsParam(ps, i + 1, row[i]);
				}
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

	
	/*
	 * core worker methods are static. They are called either directly from
	 * driver (for one-off operation) or from transHandler as part of
	 * transactions
	 */

	protected static int doRead(IDbReader reader, Connection con) throws SQLException {
		String pps = reader.getPreparedStatement();
		if (pps == null) {
			return 0;
		}

		try (PreparedStatement ps = con.prepareStatement(pps)) {
			reader.setParams(ps);
			try (ResultSet rs = ps.executeQuery()) {
				int n = 0;
				while (rs.next()) {
					if (reader.readARow(rs) == false) {
						break;
					}
					n++;
				}
				logger.info("{} rows read using read()", n);
				return n;
			}
		}
	}


	/**
	 * This is a specialized one for form-based i/o where output row may have
	 * more fields than the fields in the result set, and the order may not be
	 * the same.
	 * 
	 * @param con
	 * @param sql
	 * @param whereParams
	 * @param whereValues
	 * @param selectParams
	 * @param nbrFields
	 * @return
	 * @throws SQLException
	 */
	protected static Object[][] doReadFormRows(Connection con, String sql, FormDbParam[] whereParams,
			Object[] whereValues, FormDbParam[] selectParams, int nbrFields) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			int posn = 0;
			for (FormDbParam p : whereParams) {
				posn++;
				p.valueType.setPsParam(ps, posn, whereValues[p.idx]);
			}

			List<Object[]> result = new ArrayList<>();
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Object[] row = new Object[nbrFields];
					result.add(row);
					posn = 0;
					for (FormDbParam p : selectParams) {
						posn++;
						row[p.idx] = p.valueType.getFromRs(rs, posn);
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
	 * This is a specialized one for form-based i/o where output row may have
	 * more fields than the fields in the result set, and the order may not be
	 * the same.
	 * 
	 * @param con
	 * @param sql
	 * @param whereParams
	 * @param whereValues
	 * @param selectParams
	 * @param nbrFields
	 * @return
	 * @throws SQLException
	 */
	protected static Object[] doReadFormRow(Connection con, String sql, FormDbParam[] whereParams, Object[] whereValues,
			FormDbParam[] selectParams, int nbrFields) throws SQLException {
		try (PreparedStatement ps = con.prepareStatement(sql)) {
			int posn = 0;
			for (FormDbParam p : whereParams) {
				posn++;
				p.valueType.setPsParam(ps, posn, whereValues[p.idx]);
			}

			try (ResultSet rs = ps.executeQuery()) {
				if (!rs.next()) {
					return null;
				}

				Object[] result = new Object[nbrFields];
				posn = 0;
				for (FormDbParam p : selectParams) {
					posn++;
					result[p.idx] = p.valueType.getFromRs(rs, posn);
				}
				return result;
			}
		}
	}

	protected static int doWrite(Connection con, String sql, ValueType[] paramTypes, Object[] paramValues,
			long[] generatedKeys, String keyColumnName) throws SQLException {
		String[] keys = { keyColumnName };
		try (PreparedStatement ps = con.prepareStatement(sql, keys)) {

			for (int i = 0; i < paramValues.length; i++) {
				paramTypes[i].setPsParam(ps, i + 1, paramValues[i]);
			}
			int result = ps.executeUpdate();
			if (result > 0) {
				generatedKeys[0] = getGeneratedKey(ps);
			}
			return result;
		}
	}


	private static long getGeneratedKey(PreparedStatement ps) throws SQLException {
		try (ResultSet rs = ps.getGeneratedKeys()) {
			if (rs.next()) {
				return rs.getLong(1);
			}
			throw new SQLException("Driver failed to return a generated key ");
		}
	}

	static void warn(String sql, ValueType[] types, Object[] vals) {
		StringBuilder sbf = new StringBuilder();
		sbf.append("RDBMS is not set up. Sql = ").append(sql);
		for (int i = 0; i < types.length; i++) {
			sbf.append('(').append(types[i]).append(", ").append(vals[i]).append(") ");
		}
		logger.warn(sbf.toString());
	}

	static void warn(String sql) {
		logger.error("RDBMS is not set up. Sql = ", sql);
	}

	
}
