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

import java.util.regex.Pattern;

/**
 * validation parameters for a text value
 *
 * @author simplity.org
 *
 */
public class TextType extends DataType {
	private final String regex;

	/**
	 *
	 * @param name
	 * @param messageId
	 * @param minLength
	 * @param maxLength
	 * @param regex
	 */
<<<<<<< HEAD
	public TextType(final String name, final String messageId, final int minLength, final int maxLength,
			final String regex) {
		this.name = name;
=======
	public TextType(String name, String messageId, int minLength, int maxLength, String regex) {
>>>>>>> fbeaf366db5b468d2b6d9478cc8f1c7e697e915c
		this.valueType = ValueType.Text;
		this.minLength = minLength;
		this.maxLength = maxLength;
		this.messageId = messageId;
		if (regex == null || regex.isEmpty()) {
			this.regex = null;
		} else {
			this.regex = regex;
		}
	}

	@Override
	public String parse(final Object object) {
		return this.parse(object.toString());
	}

	@Override
	public String parse(final String value) {
		final int len = value.length();
		if (len < this.minLength || (this.maxLength > 0 && len > this.maxLength)) {
			return null;
		}
		if (this.regex == null || Pattern.matches(this.regex, value)) {
			return value;
		}
		return null;
	}
}
