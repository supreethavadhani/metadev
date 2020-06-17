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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
	Text {
		@Override
		public String doParse(final String value) {
			return value;
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			String val;
			if (value == null) {
				val = Conventions.Db.TEXT_VALUE_OF_NULL;
			} else {
				val = value.toString();
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

		@Override
		protected boolean typeOk(final Object value) {
			return true;
		}
	},
	/**
	 * whole number
	 */
	Integer {
		@Override
		public Long doParse(final String value) {
<<<<<<< HEAD
			try {
				return (Long.parseLong(value));
			} catch (final Exception e) {
				try {
					/*
					 * not long. is it decimal?
					 */
					final double d = Double.parseDouble(value);
					/*
					 * why was it not valid lng but a valid decimal?
					 */
					final int idx = value.indexOf('.');
					if (idx == -1) {
						/*
						 * it was a long that was longer than long!
						 */
						return null;
					}
					if (idx > 19) {
						/*
						 * valid decimal, but has more than 19 digits. cannot be
						 * accepted as long
						 */
						return null;
					}
					return Math.round(d);

				} catch (final Exception e1) {
					return null;
				}
=======
			/*
			 * we are okay with decimals but we take the long value of that
			 */
			try {
				return ((Number) Double.parseDouble(value)).longValue();
			} catch (final Exception e) {
				return null;
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
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

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof Number;
		}
	},
	/**
	 * whole number
	 */
	Decimal {
		@Override
		public Double doParse(final String value) {
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

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof Number;
		}
	},
	/**
	 * boolean
	 */
	Boolean {
		@Override
		public Boolean doParse(final String value) {
			switch (value.toLowerCase()) {
			case "1":
			case "true":
				return true;
			case "0":
			case "false":
				return false;
			default:
				return null;
			}
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			if (value == null) {
				ps.setNull(position, Types.BOOLEAN);
			} else {
				ps.setBoolean(position, (boolean) value);
			}
		}

		@Override
		public Boolean getFromRs(final ResultSet rs, final int position) throws SQLException {
			final boolean result = rs.getBoolean(position);
			if (rs.wasNull()) {
				return null;
			}
			return result;
		}

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof Boolean;
		}
	},
	/**
	 * Date as in calendar. No time, no time-zone. like a date-of-birth. Most
	 * commonly used value-type amongst the three types
	 */
	Date {
		@Override
		public LocalDate doParse(final String value) {
			try {
				return LocalDate.parse(value);
			} catch (final Exception e) {
				//
			}
			return null;
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			java.sql.Date date = null;
			if (value != null) {
				date = java.sql.Date.valueOf((LocalDate) value);
			}
			ps.setDate(position, date);
		}

		@Override
		public LocalDate getFromRs(final ResultSet rs, final int position) throws SQLException {
			final java.sql.Date date = rs.getDate(position);
			if (date == null) {
				return null;
			}
			return date.toLocalDate();
		}

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof LocalDate;
		}
	},

	/**
	 * an instant of time. will show up as different date/time .based on the
	 * locale. Likely candidate to represent most "date-time" fields
	 */
	Timestamp {
		@Override
		public Instant doParse(final String value) {
			try {
				return Instant.parse(value);
			} catch (final Exception e) {
<<<<<<< HEAD
				System.err.println(value + " is not a vlid instant");
=======
				//
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
			}
			return null;
		}

		@Override
		public void setPsParam(final PreparedStatement ps, final int position, final Object value) throws SQLException {
			java.sql.Timestamp stamp = null;
			if (value != null) {
				stamp = java.sql.Timestamp.from((Instant) value);
			}
			ps.setTimestamp(position, stamp);
		}

		@Override
		public Instant getFromRs(final ResultSet rs, final int position) throws SQLException {
			final java.sql.Timestamp stamp = rs.getTimestamp(position);
			if (stamp == null) {
				return null;
			}
			return stamp.toInstant();
		}

		@Override
		protected boolean typeOk(final Object value) {
			return value instanceof Instant;
		}
	};

	/**
	 * parse this value type from a string
	 *
	 * @param value
	 *            non-null to ensure that the caller can figure out whether the
	 *            parse failed or not.
	 *
	 * @return parsed value of this type. null if value the value could not be
	 *         parsed to the desired type
	 */
	public Object parse(final String value) {
		return this.doParse(value.trim());
	}

	protected abstract Object doParse(String value);

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

	/**
	 * can be used when the parameter is non-null and is one of String, Long,
	 * Double, Boolean, LocalDate or Instant. This method has a slight
	 * performance over-head to checkfor teh object instance. So, if the value
	 * type is known, it is better to use setPsParam()If the valeType is knowm,
	 * it is better to use the
	 *
	 * @param value
	 *            non-null. one of the standard classes we use,String, Long,
	 *            Double, Boolean, LocalDate or Instant
	 * @param ps
	 *            non-null prepared statement
<<<<<<< HEAD
	 * @param oneBasedPosition
=======
	 * @param oneBaedPosition
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
	 *            position of the parameter. note that the first position starts
	 *            at 1 and not 0
	 * @throws SQLException
	 */
<<<<<<< HEAD
	public static void setObjectAsPsParam(final Object value, final PreparedStatement ps, final int oneBasedPosition)
=======
	public static void setObjectAsPsParam(final Object value, final PreparedStatement ps, final int oneBaedPosition)
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
			throws SQLException {
		if (value == null) {
			throw new SQLException("Null value can not be set to a psparameter using this method");
		}

		if (value instanceof String) {
<<<<<<< HEAD
			ps.setString(oneBasedPosition, (String) value);
=======
			ps.setString(oneBaedPosition, (String) value);
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
			return;
		}

		if (value instanceof Long) {
<<<<<<< HEAD
			ps.setLong(oneBasedPosition, (Long) value);
=======
			ps.setLong(oneBaedPosition, (Long) value);
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
			return;
		}

		if (value instanceof Double) {
<<<<<<< HEAD
			ps.setDouble(oneBasedPosition, (Double) value);
=======
			ps.setDouble(oneBaedPosition, (Double) value);
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
			return;
		}

		if (value instanceof Boolean) {
<<<<<<< HEAD
			ps.setBoolean(oneBasedPosition, (Boolean) value);
=======
			ps.setBoolean(oneBaedPosition, (Boolean) value);
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
			return;
		}

		if (value instanceof LocalDate) {
<<<<<<< HEAD
			ps.setDate(oneBasedPosition, java.sql.Date.valueOf((LocalDate) value));
=======
			ps.setDate(oneBaedPosition, java.sql.Date.valueOf((LocalDate) value));
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
			return;
		}

		if (value instanceof Instant) {
<<<<<<< HEAD
			ps.setTimestamp(oneBasedPosition, java.sql.Timestamp.from((Instant) value));
			return;
		}
		throw new SQLException(
				"Value set to a psparameter must be a non-null instance of String, Long, Souble, Boolean, LocalDate or Instance. Supplied value is "
						+ value.getClass().getName());
	}

	/**
	 * @param value
	 *            can be null.
	 * @return true if the value is null, or is an instance of the right type.
	 *         false otherwise.
	 */
	public boolean isRighType(final Object value) {
		if (value == null) {
			return true;
		}
		return this.typeOk(value);
	}

	protected abstract boolean typeOk(Object value);
=======
			ps.setTimestamp(oneBaedPosition, java.sql.Timestamp.from((Instant) value));
			return;
		}
	}
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
}
