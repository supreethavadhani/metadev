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

package org.simplity.fm.core.datatypes;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;

import org.simplity.fm.core.Conventions;

/**
 * text, number etc..
 *
 * @author simplity.org
 *
 */
public enum ValueType {
	/**
	 * text
	 */
	TEXT {
		@Override
		public String parse(final String value) {
			return value;
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			String val = (String) value;
			if (value == null) {
				val = Conventions.Db.TEXT_VALUE_OF_NULL;
			}
			ps.setString(position, val);
		}

		@Override
		public String getFromRs(final ResultSet rs, final int position) throws SQLException {
			String val = rs.getString(position);
			if (val == null) {
				val = Conventions.Db.TEXT_VALUE_OF_NULL;
			}
			return val;
		}
	},
	/**
	 * whole number
	 */
	INTEGER {
		@Override
		public Long parse(final String value) {
			/*
			 * we are okay with decimals but we take the long value of that
			 */
			try {
				return ((Number) Double.parseDouble(value)).longValue();
			} catch (final Exception e) {
				return null;
			}
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			if (value == null) {
				if (Conventions.Db.TREAT_NULL_AS_ZERO) {
					ps.setLong(position, 0L);
				} else {
					ps.setNull(position, Types.INTEGER);
				}
			} else {
				ps.setLong(position, (long) value);
			}
		}

		@Override
		public Long getFromRs(final ResultSet rs, final int position) throws SQLException {
			final long result = rs.getLong(position);
			if (rs.wasNull()) {
				if (Conventions.Db.TREAT_NULL_AS_ZERO) {
					return 0L;
				}
				return null;
			}
			return result;
		}
	},
	/**
	 * whole number
	 */
	DECIMAL {
		@Override
		public Double parse(final String value) {
			try {
				return Double.parseDouble(value);
			} catch (final Exception e) {
				return null;
			}
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			if (value == null) {
				if (Conventions.Db.TREAT_NULL_AS_ZERO) {
					ps.setDouble(position, 0.0);
				} else {
					ps.setNull(position, Types.DECIMAL);
				}
			} else {
				ps.setDouble(position, (Double) value);
			}
		}

		@Override
		public Double getFromRs(final ResultSet rs, final int position) throws SQLException {
			final double result = rs.getDouble(position);
			if (rs.wasNull()) {
				if (Conventions.Db.TREAT_NULL_AS_ZERO) {
					return 0.0;
				}
				return null;
			}
			return result;
		}
	},
	/**
	 * boolean
	 */
	BOOLEAN {
		@Override
		public Boolean parse(final String value) {
			if ("1".equals(value)) {
				return true;
			}
			if ("0".equals(value)) {
				return false;
			}
			final String v = value.toUpperCase();
			if ("TRUE".equals(v)) {
				return true;
			}
			if ("FALSE".equals(v)) {
				return true;
			}
			return null;
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			boolean val = false;
			if (value != null) {
				val = (boolean) value;
			}
			ps.setBoolean(position, val);
		}

		@Override
		public Boolean getFromRs(final ResultSet rs, final int position) throws SQLException {
			final boolean result = rs.getBoolean(position);
			if (rs.wasNull()) {
				return null;
			}
			return result;
		}
	},
	/**
	 * Date as in calendar. No time, no time-zone. like a date-of-birth. Most
	 * commonly used value-type amongst the three types
	 */
	DATE {
		@Override
		public LocalDate parse(final String value) {
			try {
				return LocalDate.parse(value);
			} catch (final Exception e) {
				//
			}
			return null;
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			Date date = null;
			if (value != null) {
				date = Date.valueOf((LocalDate) value);
			}
			ps.setDate(position, date);
		}

		@Override
		public LocalDate getFromRs(final ResultSet rs, final int position) throws SQLException {
			final Date date = rs.getDate(position);
			if (date == null) {
				return null;
			}
			return date.toLocalDate();
		}
	},

	/**
	 * an instant of time. will show up as different date/time .based on the
	 * locale. Likely candidate to represent most "date-time" fields
	 */
	TIMESTAMP {
		@Override
		public Instant parse(final String value) {
			try {
				return Instant.parse(value);
			} catch (final Exception e) {
				//
			}
			return null;
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			Timestamp stamp = null;
			if (value != null) {
				stamp = Timestamp.from((Instant) value);
			}
			ps.setTimestamp(position, stamp);
		}

		@Override
		public Instant getFromRs(final ResultSet rs, final int position) throws SQLException {
			final Timestamp stamp = rs.getTimestamp(position);
			if (stamp == null) {
				return null;
			}
			return stamp.toInstant();
		}
	};

	/**
	 * parse this value type from a string
	 *
	 * @param value
	 *            non-null
	 * @return parsed value of this type. null if value is null or the value can
	 *         not be parsed to the desired type
	 */
	public abstract Object parse(String value);

	/**
	 * @param ps
	 * @param position
	 * @param value
	 * @throws SQLException
	 */
	public abstract void setPsParam(PreparedStatement ps, int position, Object value) throws SQLException;

	/**
	 *
	 * @param rs
	 * @param position
	 * @return object returned in the result set
	 * @throws SQLException
	 */
	public abstract Object getFromRs(ResultSet rs, int position) throws SQLException;
}
