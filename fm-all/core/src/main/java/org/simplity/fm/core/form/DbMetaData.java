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

package org.simplity.fm.core.form;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.rdb.IDbReader;
import org.simplity.fm.core.rdb.IDbWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class DbMetaData {
	protected static final Logger logger = LoggerFactory.getLogger(DbMetaData.class);
	/**
	 * e.g. where a=? and b=?
	 */
	public String whereClause;
	/**
	 * db parameters to be used for the where clause
	 */
	public FormDbParam[] whereParams;
	/**
	 * e.g. where a=? and b=?
	 */
	public String uniqueClause;
	/**
	 * db parameters to be used for the where clause
	 */
	public FormDbParam[] uniqueParams;
	/**
	 * e.g. select a,b,c from t
	 */
	public String selectClause;
	/**
	 * db parameters to be used to receive data from the result set of the
	 * select query
	 */
	public FormDbParam[] selectParams;
	/**
	 * e.g insert a,b,c,d into table1 values(?,?,?,?)
	 */
	public String insertClause;
	/**
	 * db parameters for the insert sql
	 */
	public FormDbParam[] insertParams;

	/**
	 * e.g. update table1 set a=?, b=?, c=?
	 */
	public String updateClause;
	/**
	 * db parameters for the update sql
	 */
	public FormDbParam[] updateParams;

	/**
	 * e.g. delete from table1. Note that where is not part of this.
	 */
	public String deleteClause;

	/**
	 * db column name that is generated as internal key. null if this is not
	 * relevant
	 */
	public String generatedColumnName;

	/**
	 *
	 */
	public int generatedKeyIdx = -1;
	/**
	 * meta data for the child form. null if
	 */
	public DbLink[] dbLinks;

	/**
	 * array index corresponds to DbOperation.orinal(). true if that operation
	 * is allowed
	 */
	public boolean[] dbOperationOk = new boolean[IoType.values().length];

	/**
	 * if this APP is designed for multi-tenant deployment, and this table has
	 * data across tenants..
	 */
	public Field tenantField;

	/**
	 * if this table allows update, and needs to use time-stamp-match technique
	 * to avoid concurrent updates..
	 */
	public Field timestampField;

	/**
	 * insert/create this form data into the db.
	 *
	 * @param handle
	 * @param values
	 *
	 * @return true if it is created. false in case it failed because of an an
	 *         existing form with the same id/key
	 * @throws SQLException
	 */
	public boolean insert(final DbHandle handle, final Object[] values) throws SQLException {
		int n = 0;
		if (this.generatedColumnName != null) {
			try {
				final long[] generatedKeys = new long[1];
				n = handle.insertAndGenerteKeys(new IDbWriter() {

					@Override
					public String getPreparedStatement() {
						return DbMetaData.this.insertClause;
					}

					@Override
					public void setParams(final PreparedStatement ps) throws SQLException {
						int posn = 1;
						final StringBuilder sbf = new StringBuilder("Parameter Values");
						for (final FormDbParam p : DbMetaData.this.insertParams) {
							final Object value = values[p.idx];
							p.valueType.setPsParam(ps, posn, value);
							sbf.append('\n').append(posn).append('=').append(value);
							posn++;
						}
						logger.info(sbf.toString());
					}

				}, this.generatedColumnName, generatedKeys);
				final long id = generatedKeys[0];
				if (id == 0) {
					logger.error("DB handler did not return generated key");
				} else {
					values[this.generatedKeyIdx] = generatedKeys[0];
					logger.info("Generated key {] assigned back to form data", id);
				}
			} catch (final SQLException e) {
				final String msg = toMessage(e, this.insertClause, this.insertParams, values);
				logger.error(msg);
				throw new SQLException(msg, e);
			}
		} else {
			n = writeWorker(handle, this.insertClause, this.insertParams, values);
		}
		if (n == 0) {
			return false;
		}
		return true;
	}

	/**
	 * update this form data back into the db.
	 *
	 * @param handle
	 * @param values
	 *
	 * @return true if it is indeed updated. false in case there was no row to
	 *         update
	 * @throws SQLException
	 */
	public boolean update(final DbHandle handle, final Object[] values) throws SQLException {
		final int nbr = writeWorker(handle, this.updateClause, this.updateParams, values);
		return nbr > 0;
	}

	/**
	 * remove this form data from the db
	 *
	 * @param handle
	 * @param values
	 *
	 * @return true if it is indeed deleted happened. false otherwise
	 * @throws SQLException
	 */
	public boolean deleteFromDb(final DbHandle handle, final Object[] values) throws SQLException {
		final String sql = this.deleteClause + this.whereClause;
		final int nbr = writeWorker(handle, sql, this.whereParams, values);
		return nbr > 0;
	}

	private static int writeWorker(final DbHandle handle, final String sql, final FormDbParam[] params,
			final Object[] values) throws SQLException {
		try {
			final int n = handle.write(new IDbWriter() {

				@Override
				public String getPreparedStatement() {
					return sql;
				}

				@Override
				public void setParams(final PreparedStatement ps) throws SQLException {
					int posn = 1;
					final StringBuilder sbf = new StringBuilder("Parameter Values");
					for (final FormDbParam p : params) {
						final Object value = values[p.idx];
						p.valueType.setPsParam(ps, posn, value);
						sbf.append('\n').append(posn).append('=').append(value);
						posn++;
					}
					logger.info(sbf.toString());
				}

			});
			return n;
		} catch (final SQLException e) {
			final String msg = toMessage(e, sql, params, values);
			logger.error(msg);
			throw new SQLException(msg, e);
		}
	}

	private static String toMessage(final SQLException e, final String sql, final FormDbParam[] params,
			final Object[] values) {
		final StringBuilder buf = new StringBuilder();
		buf.append("Sql Exception : ").append(e.getMessage());
		buf.append("SQL:").append(sql).append("\nParameters");
		final SQLException e1 = e.getNextException();
		if (e1 != null) {
			buf.append("\nLinked to the SqlExcpetion: ").append(e1.getMessage());
		}
		int idx = 1;
		for (final FormDbParam p : params) {
			buf.append('\n').append(idx).append(". type=").append(p.valueType);
			buf.append(" value=").append(values[p.idx]);
			idx++;
		}
		return buf.toString();
	}

	/**
	 * fetch data for this form from a db
	 *
	 * @param handle
	 * @param values
	 *
	 * @return true if it is read.false if no data found for this form (key not
	 *         found...)
	 * @throws SQLException
	 */
	public boolean fetch(final DbHandle handle, final Object[] values) throws SQLException {
		return fetchWorker(handle, this.selectClause + this.whereClause, values, this.whereParams, this.selectParams);
	}

	/**
	 * @param handle
	 * @param values
	 * @return true if read is successful. false otherwise
	 * @throws SQLException
	 */
	public boolean fetchUsingUniqueKeys(final DbHandle handle, final Object[] values) throws SQLException {
		if (this.uniqueClause == null) {
			logger.error("No unique keys defined. can not be fetched with unique keys.");
			return false;
		}
		return fetchWorker(handle, this.selectClause + this.uniqueClause, values, this.uniqueParams, this.selectParams);
	}

	private static boolean fetchWorker(final DbHandle driver, final String sql, final Object[] values,
			final FormDbParam[] setters, final FormDbParam[] getters) throws SQLException {
		final boolean[] result = new boolean[1];
		driver.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return sql;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				if (setters == null || setters.length == 0) {
					return;
				}
				int posn = 1;
				final StringBuilder sbf = new StringBuilder("Parameter Values");
				for (final FormDbParam p : setters) {
					final Object value = values[p.idx];
					p.valueType.setPsParam(ps, posn, value);
					sbf.append('\n').append(posn).append('=').append(value);
					posn++;
				}
				logger.info(sbf.toString());
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				int posn = 1;
				for (final FormDbParam p : getters) {
					values[p.idx] = p.valueType.getFromRs(rs, posn);
					posn++;
				}
				result[0] = true;
				return false;
			}
		});
		return result[0];
	}

	static FormData[] fetchDataWorker(final DbHandle handle, final Form form, final String sql, final Object[] values,
			final FormDbParam[] setters, final FormDbParam[] getters) throws SQLException {
		final List<FormData> result = new ArrayList<>();
		handle.read(new IDbReader() {

			@Override
			public String getPreparedStatement() {
				return sql;
			}

			@Override
			public void setParams(final PreparedStatement ps) throws SQLException {
				int posn = 1;
				final StringBuilder sbf = new StringBuilder("Parameter Values");
				for (final FormDbParam p : setters) {
					final Object value = values[p.idx];
					p.valueType.setPsParam(ps, posn, value);
					sbf.append('\n').append(posn).append('=').append(value);
					posn++;
				}
				logger.info(sbf.toString());
			}

			@Override
			public boolean readARow(final ResultSet rs) throws SQLException {
				final FormData fd = form.newFormData();
				result.add(fd);
				int posn = 1;
				for (final FormDbParam p : getters) {
					fd.fieldValues[p.idx] = p.valueType.getFromRs(rs, posn);
					posn++;
				}
				return true;
			}
		});
		return result.toArray(new FormData[0]);
	}

}
