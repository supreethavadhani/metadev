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

package org.simplity.fm.upload;

import java.util.Map;

import org.simplity.fm.core.Conventions;

/**
 * specifies how a field in the form maps to columns in the input row
 * @author simplity.org
 *
 */
public class LookupValueProvider implements IValueProvider{
	private final Map<String, String> lookup;
	private final IValueProvider textValue;
	private final IValueProvider keyValue;

	/**
	 * 
	 * @param lookup must be non-null
	 * @param textValue must be non-null
	 * @param keyValue must be null if this is simple lookup, and non-null if this is keyed lookup
	 */
	public LookupValueProvider(Map<String, String> lookup, IValueProvider textValue, IValueProvider keyValue ) {
		this.lookup = lookup;
		this.textValue = textValue;
		this.keyValue = keyValue;
	}
	
	@Override
	public String getValue(Map<String, String> input) {
		String text = this.textValue.getValue(input);
		if(text == null) {
			return null;
		}
		if(this.keyValue != null) {
			String key = this.keyValue.getValue(input);
			if(key == null) {
				return null;
			}
			text = key +Conventions.Upload.KEY_TEXT_SEPARATOR + text;
		}
		return this.lookup.get(text);
	}
}
