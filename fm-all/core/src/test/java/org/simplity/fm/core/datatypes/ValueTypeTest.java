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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.simplity.fm.core.Conventions;

/**
 * @author simplity.org
 *
 */

public class ValueTypeTest {
	protected ResultSet rs = Mockito.mock(ResultSet.class);
	protected PreparedStatement ps = Mockito.mock(PreparedStatement.class);

	@Nested
	@DisplayName("Test ValueType.Boolean")
	class BooleanTest {
		@ParameterizedTest
		@ValueSource(strings = { "1", " true", "TRUE ", "  True ", "tRuE" })
		void shouldParseAsTrue(final String value) {
			assertEquals(true, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "  0 ", "false", "FALSE  ", "False", "FaLSe" })
		void shouldParseAsFalse(final String value) {
			assertEquals(false, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "a", "3", "Yes", "t", "f", "true1", "true true" })
		void shouldParseAsNull(final String value) {
			assertEquals(null, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(booleans = { true, false })
		void shouldSetBoolValueToPs(final boolean value) throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Boolean.setPsParam(ValueTypeTest.this.ps, 1, value);
			Mockito.verify(ValueTypeTest.this.ps).setBoolean(1, value);
		}

		@Test
		void shouldSetNullToPs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Boolean.setPsParam(ValueTypeTest.this.ps, 1, null);
			Mockito.verify(ValueTypeTest.this.ps).setNull(1, Types.BOOLEAN);
		}

		@Test
		void shouldThrowException() {
			assertThrows(ClassCastException.class, () -> {
				ValueType.Boolean.setPsParam(ValueTypeTest.this.ps, 1, "someString");
			});
		}

		@Test
		void shouldGetBooleanValueFromRes() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(false);
			when(ValueTypeTest.this.rs.getBoolean(1)).thenReturn(true);
			when(ValueTypeTest.this.rs.getBoolean(2)).thenReturn(false);
			assertEquals(true, ValueType.Boolean.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(false, ValueType.Boolean.getFromRs(ValueTypeTest.this.rs, 2));
		}

		@Test
		void shouldGetNullFromRes() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(true);
			when(ValueTypeTest.this.rs.getBoolean(1)).thenReturn(true);
			when(ValueTypeTest.this.rs.getBoolean(2)).thenReturn(false);
			assertEquals(null, ValueType.Boolean.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(null, ValueType.Boolean.getFromRs(ValueTypeTest.this.rs, 2));
		}
	}

	@Nested
	@DisplayName("Test ValueType.Text")
	class TextTest {
		@ParameterizedTest
		@ValueSource(strings = { " 1", "true ", "TRUE ", "" })
		void shouldTrimStrings(final String value) {
			assertEquals(value.trim(), ValueType.Text.parse(value));
		}

		@Test
		void shouldSetTextValueToPs() throws SQLException {
			final Object[] objects = { 1, "abcd", true, LocalDate.now() };
			for (final Object obj : objects) {
				Mockito.reset(ValueTypeTest.this.ps);
				ValueType.Text.setPsParam(ValueTypeTest.this.ps, 1, obj);
				Mockito.verify(ValueTypeTest.this.ps).setString(1, obj.toString());
			}
		}

		@Test
		void shouldSetNullToPsAsPerConvention() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Text.setPsParam(ValueTypeTest.this.ps, 1, null);
			Mockito.verify(ValueTypeTest.this.ps).setString(1, Conventions.Db.TEXT_VALUE_OF_NULL);
		}

		@Test
		void shouldGetTextValueFromRes() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(false);
			when(ValueTypeTest.this.rs.getString(1)).thenReturn("one");
			when(ValueTypeTest.this.rs.getString(2)).thenReturn("two");
			when(ValueTypeTest.this.rs.getString(3)).thenReturn(null);
			assertEquals("one", ValueType.Text.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals("two", ValueType.Text.getFromRs(ValueTypeTest.this.rs, 2));
			assertEquals(Conventions.Db.TEXT_VALUE_OF_NULL, ValueType.Text.getFromRs(ValueTypeTest.this.rs, 3));
		}
	}

	@Nested
	@DisplayName("Test ValueType.Integer")
	class IntegerTest {
		@ParameterizedTest
		@ValueSource(strings = { "1a", "a1", "1+1", "1 1", "tRuE", " ", "1  a" })
		void shouldParseNonNumbersAsNull(final String value) {
			assertNull(ValueType.Integer.parse(value));
		}

		@Test
		void shouldParseUpTo19Digits() {
			assertEquals(1234567890123456789L, ValueType.Integer.parse("1234567890123456789"));
		}

		@ParameterizedTest
		@ValueSource(strings = { "12345678901234567890", "12345678901234567890123.345" })
		void shouldFailIfMoreThan19Digits(final String value) {
			assertNull(ValueType.Integer.parse(value));
		}

		@Test
		void shouldParseNumberStartingWith0() {
			assertEquals(1L, ValueType.Integer.parse("01"));
		}

		@Test
		void shouldParseNumbersStartingWithPlus() {
			assertEquals(1L, ValueType.Integer.parse("+1"));
		}

		@Test
		void shouldParseNumbersStartingWithMinus() {
			assertEquals(-1L, ValueType.Integer.parse("-1"));
		}

		@ParameterizedTest
		@ValueSource(strings = { "1.0", "1.49", "0.5" })
		void shouldParseDecimalsAsRounded(final String value) {
			assertEquals(1L, ValueType.Integer.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "-1.0", "-1.5", "-0.6", "-.56" })
		void shouldParseNegativeDecimalsAsRounded(final String value) {
			assertEquals(-1L, ValueType.Integer.parse(value));
		}

		@ParameterizedTest
		@ValueSource(longs = { 0L, 1L, -1L })
		void shouldSetLongValueToPs(final long value) throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Integer.setPsParam(ValueTypeTest.this.ps, 1, value);
			Mockito.verify(ValueTypeTest.this.ps).setLong(1, value);
		}

		@Test
		void shouldSet0WhenNull() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Integer.setPsParam(ValueTypeTest.this.ps, 1, null);
			Mockito.verify(ValueTypeTest.this.ps).setLong(1, 0L);
		}

		@Test
		void shouldThrowException() {
			assertThrows(ClassCastException.class, () -> {
				ValueType.Integer.setPsParam(ValueTypeTest.this.ps, 1, "some String");
			});
		}

		@Test
		void shouldGetLongValueFromRs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(false);
			when(ValueTypeTest.this.rs.getLong(1)).thenReturn(0L);
			when(ValueTypeTest.this.rs.getLong(2)).thenReturn(99L);
			assertEquals(0L, ValueType.Integer.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(99L, ValueType.Integer.getFromRs(ValueTypeTest.this.rs, 2));
		}

		@Test
		void shouldTreatNullFromRsAs0() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(true);
			when(ValueTypeTest.this.rs.getLong(1)).thenReturn(1L);
			when(ValueTypeTest.this.rs.getLong(2)).thenReturn(2L);
			assertEquals(0L, ValueType.Integer.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(0L, ValueType.Integer.getFromRs(ValueTypeTest.this.rs, 2));
		}
	}

	@Nested
	@DisplayName("Test ValueType.Integer")
	class DecimlTest {
		@ParameterizedTest
		@ValueSource(strings = { "1a", "a1", "1+1", "1 1", "tRuE", " ", "1  a" })
		void shouldParseNonNumbersAsNull(final String value) {
			assertNull(ValueType.Decimal.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "1", "1.1", "0.001", ".011", "12345678901234567890.1234" })
		void shouldParseValidDecimals(final String value) {
			final double d = Double.parseDouble(value);
			assertEquals(d, ValueType.Decimal.parse(value));
		}

		@ParameterizedTest
		@ValueSource(doubles = { 0d, 1d, -1d })
		void shouldSetDoubleValueToPs(final double value) throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Decimal.setPsParam(ValueTypeTest.this.ps, 1, value);
			Mockito.verify(ValueTypeTest.this.ps).setDouble(1, value);
		}

		@Test
		void shouldSet0WhenNull() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Decimal.setPsParam(ValueTypeTest.this.ps, 1, null);
			Mockito.verify(ValueTypeTest.this.ps).setDouble(1, 0d);
		}

		@Test
		void shouldThrowException() {
			assertThrows(ClassCastException.class, () -> {
				ValueType.Decimal.setPsParam(ValueTypeTest.this.ps, 1, "some String");
			});
		}

		@Test
		void shouldGetDoubleValueFromRs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(false);
			when(ValueTypeTest.this.rs.getDouble(1)).thenReturn(0d);
			when(ValueTypeTest.this.rs.getDouble(2)).thenReturn(99d);
			assertEquals(0d, ValueType.Decimal.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(99d, ValueType.Decimal.getFromRs(ValueTypeTest.this.rs, 2));
		}

		@Test
		void shouldTreatNullFromRsAs0() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(true);
			when(ValueTypeTest.this.rs.getDouble(1)).thenReturn(1d);
			when(ValueTypeTest.this.rs.getDouble(2)).thenReturn(2d);
			assertEquals(0d, ValueType.Decimal.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(0d, ValueType.Decimal.getFromRs(ValueTypeTest.this.rs, 2));
		}
	}

	@Nested
	@DisplayName("Test ValueType.Date")
	class DateTest {
		@ParameterizedTest
		@ValueSource(strings = { " 2011-11-12 ", " 2999-12-31" })
		void shouldParseVlidDates(final String value) {
			assertEquals(LocalDate.parse(value.trim()), ValueType.Date.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "abcd", "20111-11-12", "2020-02-30", "2019-02-29" })
		void shouldReturnNullForInvalidDates(final String value) {
			assertNull(ValueType.Date.parse(value));
		}

		@Test
		void shouldSetDateValueToPs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			final LocalDate d = LocalDate.now();
			final Date date = Date.valueOf(d);
			ValueType.Date.setPsParam(ValueTypeTest.this.ps, 1, d);
			Mockito.verify(ValueTypeTest.this.ps).setDate(1, date);
		}

		@Test
		void shouldSetNullToPs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Date.setPsParam(ValueTypeTest.this.ps, 1, null);
			Mockito.verify(ValueTypeTest.this.ps).setDate(1, null);
		}

		@Test
		void shouldThrowExceptionOnNonDates() {
			assertThrows(ClassCastException.class, () -> {
				ValueType.Date.setPsParam(ValueTypeTest.this.ps, 1, "some String");
			});
		}

		@Test
		void shouldGetDateValueFromRs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			final LocalDate d = LocalDate.parse("2011-12-31");
			when(ValueTypeTest.this.rs.getDate(1)).thenReturn(null);
			when(ValueTypeTest.this.rs.getDate(2)).thenReturn(Date.valueOf(d));
			assertNull(ValueType.Date.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(d, ValueType.Date.getFromRs(ValueTypeTest.this.rs, 2));
		}
	}

	@Nested
	@DisplayName("Test ValueType.Timestamp")
	class TimestampTest {
		@ParameterizedTest
		@ValueSource(strings = { " 2011-11-12T12:23:59Z", " 2020-02-29T12:23:59Z  " })
		void shouldParseValidStamps(final String value) {
			assertEquals(Instant.parse(value.trim()), ValueType.Timestamp.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "abcd", "2011-11-12 12:23:59.12Z", "2011-11-12T12:23:59.12" })
		void shouldReturnNullForInvalidStamps(final String value) {
			assertNull(ValueType.Timestamp.parse(value));
		}

		@Test
		void shouldSetStampValueToPs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			final Instant t = Instant.ofEpochMilli(123456789l);
			final Timestamp stamp = Timestamp.from(t);
			ValueType.Timestamp.setPsParam(ValueTypeTest.this.ps, 1, t);
			Mockito.verify(ValueTypeTest.this.ps).setTimestamp(1, stamp);
		}

		@Test
		void shouldSetNullToPs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Timestamp.setPsParam(ValueTypeTest.this.ps, 1, null);
			Mockito.verify(ValueTypeTest.this.ps).setTimestamp(1, null);
		}

		@Test
		void shouldThrowExceptionOnNonDates() {
			assertThrows(ClassCastException.class, () -> {
				ValueType.Timestamp.setPsParam(ValueTypeTest.this.ps, 1, "some String");
			});
		}

		@Test
		void shouldGetTimestampValueFromRs() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			final Instant t = Instant.ofEpochMilli(123456789l);
			when(ValueTypeTest.this.rs.getTimestamp(1)).thenReturn(null);
			when(ValueTypeTest.this.rs.getTimestamp(2)).thenReturn(Timestamp.from(t));
			assertNull(ValueType.Timestamp.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(t, ValueType.Timestamp.getFromRs(ValueTypeTest.this.rs, 2));
		}
	}

	@Nested
	@DisplayName("Test ValueType.setObjectAsParam()")
	class SetObjectTest {
		@Test
		void shouldThrowExceptionOnWrongTypes() {
			final Object[] values = { 1, 1.2f, Date.valueOf(LocalDate.now()), new StringBuilder(),
					Timestamp.from(Instant.ofEpochMilli(123456l)), null };
			for (final Object value : values) {
				assertThrows(SQLException.class, () -> {
					ValueType.setObjectAsPsParam(value, ValueTypeTest.this.ps, 1);
				});
			}
		}

		@Test
		void shouldSetString() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			final String value = "abcd";
			ValueType.setObjectAsPsParam(value, ValueTypeTest.this.ps, 1);
			Mockito.verify(ValueTypeTest.this.ps).setString(1, value);
		}

		@Test
		void shouldSetLong() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			final long value = 34215;
			ValueType.setObjectAsPsParam(value, ValueTypeTest.this.ps, 1);
			Mockito.verify(ValueTypeTest.this.ps).setLong(1, value);
		}

		@Test
		void shouldSetDouble() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			final double value = 12.34d;
			ValueType.setObjectAsPsParam(value, ValueTypeTest.this.ps, 1);
			Mockito.verify(ValueTypeTest.this.ps).setDouble(1, value);
		}

		@Test
		void shouldSetBoolean() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			final boolean value = true;
			ValueType.setObjectAsPsParam(value, ValueTypeTest.this.ps, 1);
			Mockito.verify(ValueTypeTest.this.ps).setBoolean(1, value);
		}

		@Test
		void shouldSetDate() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			final LocalDate t = LocalDate.ofEpochDay(12345);
			final Date value = Date.valueOf(t);
			ValueType.setObjectAsPsParam(t, ValueTypeTest.this.ps, 1);
			Mockito.verify(ValueTypeTest.this.ps).setDate(1, value);
		}

		@Test
		void shouldSetTimestamp() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			final Instant t = Instant.ofEpochMilli(342156l);
			final Timestamp value = Timestamp.from(t);
			ValueType.setObjectAsPsParam(t, ValueTypeTest.this.ps, 1);
			Mockito.verify(ValueTypeTest.this.ps).setTimestamp(1, value);
		}
	}
}
