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
import java.time.Instant;
import java.time.LocalDate;

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
		public String parse(String value) {
			return value;
		}

		@Override
		public void setPsParam(PreparedStatement ps, int position, Object value) throws SQLException {
			ps.setString(position, (String) value);
		}

		@Override
		public String getFromRs(ResultSet rs, int position) throws SQLException {
			return rs.getString(position);
		}
	},
	/**
	 * whole number
	 */
	INTEGER {
		@Override
		public Long parse(String value) {
			/*
			 * we are okay with decimals but we take the long value of that
			 */
			try {
				return ((Number) Double.parseDouble(value)).longValue();
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public void setPsParam(PreparedStatement ps, int position, Object value) throws SQLException {
			ps.setLong(position, (long) value);
		}

		@Override
		public Long getFromRs(ResultSet rs, int position) throws SQLException {
			long result = rs.getLong(position);
			if(rs.wasNull()) {
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
		public Double parse(String value) {
			try {
				return Double.parseDouble(value);
			} catch (Exception e) {
				return null;
			}
		}

		@Override
		public void setPsParam(PreparedStatement ps, int position, Object value) throws SQLException {
			ps.setDouble(position, (Double) value);
		}

		@Override
		public Double getFromRs(ResultSet rs, int position) throws SQLException {
			double result = rs.getDouble(position);
			if(rs.wasNull()) {
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
		public Boolean parse(String value) {
			if ("1".equals(value)) {
				return true;
			}
			if ("0".equals(value)) {
				return false;
			}
			String v = value.toUpperCase();
			if ("TRUE".equals(v)) {
				return true;
			}
			if ("FALSE".equals(v)) {
				return true;
			}
			return null;
		}

		@Override
		public void setPsParam(PreparedStatement ps, int position, Object value) throws SQLException {
			boolean val = false;
			if(value != null) {
				val = (boolean)value;
			}
			ps.setBoolean(position, val);
		}

		@Override
		public Boolean getFromRs(ResultSet rs, int position) throws SQLException {
			boolean result = rs.getBoolean(position);
			if(rs.wasNull()) {
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
		public LocalDate parse(String value) {
			try {
				return LocalDate.parse(value);
			} catch (Exception e) {
				//
			}
			return null;
		}

		@Override
		public void setPsParam(PreparedStatement ps, int position, Object value) throws SQLException {
			ps.setDate(position, Date.valueOf((LocalDate) value));
		}

		@Override
		public LocalDate getFromRs(ResultSet rs, int position) throws SQLException {
			Date date = rs.getDate(position);
			if(rs.wasNull()) {
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
		public Instant parse(String value) {
			try {
				return Instant.parse(value);
			} catch (Exception e) {
				//
			}
			return null;
		}

		@Override
		public void setPsParam(PreparedStatement ps, int position, Object value) throws SQLException {
			ps.setTimestamp(position, Timestamp.from((Instant) value));
		}

		@Override
		public Instant getFromRs(ResultSet rs, int position) throws SQLException {
			Timestamp stamp = rs.getTimestamp(position);
			if(rs.wasNull()) {
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
