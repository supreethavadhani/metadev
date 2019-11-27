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

import java.time.LocalDate;

/**
 * validation parameters for a an integral value
 *
 * @author simplity.org
 *
 */
public class DateType extends DataType {
	private final int maxPastDays;
	private final int maxFutureDays;

	/**
	 * @param name
	 * @param messageId
	 *
	 * @param maxPastDays
	 *            0 means today is OK. 100 means 100 days before today is the
	 *            min, -100 means 100 days after today is the min
	 * @param maxFutureDays
	 *            0 means today is OK. -100 means 100 days before today is the
	 *            max. 100 means 100 days after today is the max
	 */
	public DateType(final String name, final String messageId, final int maxPastDays, final int maxFutureDays) {
		this.valueType = ValueType.Date;
		this.name = name;
		this.messageId = messageId;
		this.maxPastDays = maxPastDays;
		this.maxFutureDays = maxFutureDays;
	}

	@Override
	public LocalDate parse(final String text) {
		try {
			if (text.length() >= 10) {
				return this.validate(LocalDate.parse(text.substring(0, 10)));
			}

			return this.validate(LocalDate.ofEpochDay(Long.parseLong(text)));

		} catch (final Exception e) {
			return null;
		}

	}

	@Override
	public LocalDate parse(final Object object) {
		if (object instanceof LocalDate) {
			return this.validate((LocalDate) object);
		}

		if (object instanceof String) {
			return this.parse((String) object);
		}
		return null;
	}

	private LocalDate validate(final LocalDate date) {
		final LocalDate today = LocalDate.now();
		if (today.plusDays(-this.maxPastDays).isAfter(date)) {
			return null;
		}
		if (today.plusDays(this.maxFutureDays).isBefore(date)) {
			return null;
		}
		return date;
	}
}
