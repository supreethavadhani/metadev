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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

import org.eclipse.jdt.annotation.NonNull;
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
		void shouldParseAsTrue(final @NonNull String value) {
			assertEquals(true, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "  0 ", "false", "FALSE  ", "False", "FaLSe" })
		void shouldParseAsFalse(final @NonNull String value) {
			assertEquals(false, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "a", "3", "Yes", "t", "f", "true1", "true true" })
		void shouldParseAsNull(final @NonNull String value) {
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
				ValueType.Boolean.setPsParam(ValueTypeTest.this.ps, 1, "some String");
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
		void shouldTrimStrings(final @NonNull String value) {
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
	class INtegerTest {
		@ParameterizedTest
		@ValueSource(strings = { "1", " true", "TRUE ", "  True ", "tRuE" })
		void shouldParseAsTrue(final @NonNull String value) {
			assertEquals(true, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "  0 ", "false", "FALSE  ", "False", "FaLSe" })
		void shouldParseAsFalse(final @NonNull String value) {
			assertEquals(false, ValueType.Boolean.parse(value));
		}

		@ParameterizedTest
		@ValueSource(strings = { "", "a", "3", "Yes", "t", "f", "true1", "true true" })
		void shouldParseAsNull(final @NonNull String value) {
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
		void shouldSetNullToPsAsPerConvention() throws SQLException {
			Mockito.reset(ValueTypeTest.this.ps);
			ValueType.Integer.setPsParam(ValueTypeTest.this.ps, 1, null);
			if (Conventions.Db.TREAT_NULL_AS_ZERO) {
				Mockito.verify(ValueTypeTest.this.ps).setLong(1, 0);
			} else {
				Mockito.verify(ValueTypeTest.this.ps).setNull(1, Types.INTEGER);
			}
		}

		@Test
		void shouldThrowException() {
			assertThrows(ClassCastException.class, () -> {
				ValueType.Integer.setPsParam(ValueTypeTest.this.ps, 1, "some String");
			});
		}

		@Test
		void shouldGetLongValueFromRes() throws SQLException {
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(false);
			when(ValueTypeTest.this.rs.getLong(1)).thenReturn(0L);
			when(ValueTypeTest.this.rs.getLong(2)).thenReturn(99L);
			assertEquals(0L, ValueType.Integer.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(99L, ValueType.Integer.getFromRs(ValueTypeTest.this.rs, 2));
		}

		@Test
		void shouldTreatNullFromRsAsPerConvention() throws SQLException {
			Object expectedValue = null;
			if (Conventions.Db.TREAT_NULL_AS_ZERO) {
				expectedValue = 0;
			}
			Mockito.reset(ValueTypeTest.this.rs);
			when(ValueTypeTest.this.rs.wasNull()).thenReturn(true);
			when(ValueTypeTest.this.rs.getLong(1)).thenReturn(1L);
			when(ValueTypeTest.this.rs.getLong(2)).thenReturn(2L);
			assertEquals(expectedValue, ValueType.Integer.getFromRs(ValueTypeTest.this.rs, 1));
			assertEquals(expectedValue, ValueType.Integer.getFromRs(ValueTypeTest.this.rs, 2));
		}
	}
}
