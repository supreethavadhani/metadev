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

package org.simplity.fm.core.validn;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.simplity.fm.core.rdb.DbHandle;
import org.simplity.fm.core.rdb.IDbClient;
import org.simplity.fm.core.rdb.IDbReader;
import org.simplity.fm.core.rdb.RdbDriver;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents meta data for a value list to be fetched at run time
 * 
 * @author simplity.org
 *
 */
public class RuntimeList implements IValueList {
	protected static final Logger logger = LoggerFactory.getLogger(RuntimeList.class);
	protected String name;
	protected String listSql;
	protected String allSql;
	protected String checkSql;
	protected boolean hasKey;
	protected boolean keyIsNumeric;
	protected boolean valueIsNumeric;
	protected boolean isTenantSpecific;

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isKeyBased() {
		return this.hasKey;
	}

	@Override
	public Object[][] getList(final Object key, IServiceContext ctx) {
		if (this.hasKey) {
			if (key == null) {
				logger.error("Key should have value for list {}", this.name);
				return null;
			}
		}
		long l = 0;
		if (this.keyIsNumeric) {
			try {
				l = Long.parseLong(key.toString());
			} catch (Exception e) {
				logger.error("Key should be numeric value for list {} but we got {}", this.name, key);
				return null;
			}
		}
		final long numericValue = l;
		final List<Object[]> result = new ArrayList<>();
		final RuntimeList that = this;
		try {
			RdbDriver.getDriver().transact(new IDbClient() {

				@Override
				public boolean transact(DbHandle handle) throws SQLException {
					handle.read(new IDbReader() {
						@Override
						public String getPreparedStatement() {
							return that.listSql;
						}

						@Override
						public void setParams(PreparedStatement ps) throws SQLException {
							int posn = 1;
							if (that.hasKey) {
								if (that.keyIsNumeric) {
									ps.setLong(posn, numericValue);
								} else {
									ps.setString(posn, key.toString());
								}
								posn++;
							}
							if (that.isTenantSpecific) {
								ps.setLong(posn, (long) ctx.getTenantId());
								posn++;
							}
						}

						@Override
						public boolean readARow(ResultSet rs) throws SQLException {
							Object[] row = new Object[2];
							if (RuntimeList.this.valueIsNumeric) {
								row[0] = rs.getLong(1);
							} else {
								row[0] = rs.getString(1);
							}
							row[1] = rs.getString(2);
							result.add(row);
							return true;
						}

					});
					return true;
				}
			}, true);
		} catch (SQLException e) {
			String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ", this.name, msg);
			return null;
		}
		if (result.size() == 0) {
			logger.error("No data found for list {} with key {}", this.name, key);
			return null;
		}
		return result.toArray(new Object[0][]);
	}

	@Override
	public boolean isValid(final Object fieldValue, final Object keyValue) {
		if (this.hasKey) {
			if (keyValue == null) {
				logger.error("Key should have value for list {}", this.name);
				return false;
			}
		}

		final boolean[] result = new boolean[1];

		try {
			RdbDriver.getDriver().transact(new IDbClient() {

				@Override
				public boolean transact(DbHandle handle) throws SQLException {
					handle.read(new IDbReader() {
						@Override
						public String getPreparedStatement() {
							return RuntimeList.this.checkSql;
						}

						@Override
						public void setParams(PreparedStatement ps) throws SQLException {
							if (RuntimeList.this.valueIsNumeric) {
								ps.setLong(1, (Long) fieldValue);
							} else {
								ps.setString(1, (String) fieldValue);
							}
							if (RuntimeList.this.hasKey) {
								if (RuntimeList.this.keyIsNumeric) {
									ps.setLong(2, (Long) keyValue);
								} else {
									ps.setString(2, (String) keyValue);
								}
							}
						}

						@Override
						public boolean readARow(ResultSet rs) throws SQLException {
							result[0] = true;
							return false;
						}

					});
					return true;
				}
			}, true);
		} catch (SQLException e) {
			String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ", this.name, msg);
			return false;
		}
		return result[0];
	}

	/**
	 * this is specifically for batch operations where id is to be inserted in
	 * place of name.
	 * 
	 * @param ctx
	 * @return map to get id from name
	 */
	@Override
	public Map<String, String> getAll(IServiceContext ctx) {
		Map<String, String> result = new HashMap<>();
		if (this.hasKey == false) {
			logger.error("List {} is not keyed. getAll is not pplicable", this.name);
			return result;
		}

		try {
			RdbDriver.getDriver().transact(new IDbClient() {

				@Override
				public boolean transact(DbHandle handle) throws SQLException {
					handle.read(new IDbReader() {

						@Override
						public String getPreparedStatement() {
							return RuntimeList.this.allSql;
						}

						@Override
						public void setParams(PreparedStatement ps) throws SQLException {
							if (RuntimeList.this.isTenantSpecific) {
								ps.setLong(1, (long) ctx.getTenantId());
							}
						}

						@Override
						public boolean readARow(ResultSet rs) throws SQLException {
							String id = rs.getString(1);
							String nam = rs.getString(2);
							String key = rs.getString(3);
							result.put(key + '|' + nam, id);
							return true;
						}
					});
					return true;
				}
			}, true);
		} catch (SQLException e) {
			String msg = e.getMessage();
			logger.error("Error while getting values for list {}. ERROR: {} ", this.name, msg);
		}
		return result;
	}
}
