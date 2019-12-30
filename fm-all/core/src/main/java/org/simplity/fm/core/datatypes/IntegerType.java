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
 * validation parameters for a an integral value
 * 
 * @author simplity.org
 *
 */
public class IntegerType extends DataType {
	private final long minValue;
	private final long maxValue;

	/**
	 * 
	 * @param name
	 * @param messageId
	 * @param minValue
	 * @param maxValue
	 */
	public IntegerType(String name, String messageId, long minValue, long maxValue) {
		this.valueType = ValueType.Integer;
		this.name = name;
		this.messageId = messageId;
		this.minValue = minValue;
		this.maxValue = maxValue;

		this.maxLength = ("" + this.maxValue).length();
		int len = ("" + this.minValue).length();
		if(len > this.maxLength) {
			this.maxLength = len;
		}
	}


	private Long validate(long value) {
		if(value >= this.minValue && value <= this.maxValue) {
			return value;
		}
		return null;
	}


	@Override
	public Long parse(Object object) {
		if(object instanceof Number) {
			return this.validate(((Number)object).longValue());
		}
		if(object instanceof String) {
			return this.parse((String)object);
		}
		return null;
	}


	@Override
	public Long parse(String value) {
		try {
		return this.validate(Long.parseLong(value));
		}catch(Exception e) {
			return null;
		}
	}
}
