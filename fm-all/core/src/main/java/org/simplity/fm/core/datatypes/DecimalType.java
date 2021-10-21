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

/**
 * @author simplity.org
 *
 */
public class DecimalType extends DataType {
	private final long minValue;
	private final long maxValue;
	/**
	 * calculated based nbr decimals as a factor to round-off the value to the
	 * right decimal places
	 */
	private final long factor;

	/**
	 *
	 * @param name
	 * @param messageId
	 * @param minValue
	 * @param maxValue
	 * @param nbrDecimals
	 */
	public DecimalType(final String name, final String messageId, final long minValue, final long maxValue,
			final int nbrDecimals) {
		this.valueType = ValueType.Decimal;
		this.name = name;
		this.messageId = messageId;
		this.minValue = minValue;
		this.maxValue = maxValue;

		this.maxLength = ("" + this.maxValue).length();
		final int len = ("" + this.minValue).length();
		if (len > this.maxLength) {
			this.maxLength = len;
		}
		this.maxLength += nbrDecimals + 1;
		long f = 10;
		for (int i = 0; i < nbrDecimals; i++) {
			f *= 10;
		}
		this.factor = f;
	}

	@Override
	public Double parse(final String value) {
		try {
			return this.validate(Double.parseDouble(value));
		} catch (final Exception e) {
			return null;
		}
	}

	@Override
	public Double parse(final Object value) {
		if (value instanceof Number) {
			return this.validate(((Number) value).doubleValue());
		}
		return this.parse(value.toString());
	}

	private Double validate(final double d) {
		final long f = Math.round(d);
		if (f > this.maxValue || f < this.minValue) {
			return null;
		}
		return (double) ((d * this.factor) / this.factor);
	}
}
