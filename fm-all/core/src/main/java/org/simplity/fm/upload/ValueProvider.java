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

/**
 * specifies how a field in the form maps to columns in the input row
 * @author simplity.org
 *
 */
public class ValueProvider implements IValueProvider{
	private final String variable;
	private final String constant;
	
	/**
	 * at least one of them should be non-null for this to be useful, though it is not an error
	 * @param variable can be null
	 * @param constant can be null
	 * 
	 */
	public ValueProvider(String variable, String constant) {
		this.variable = variable;
		this.constant = constant;
	}
	
	@Override
	public String getValue(Map<String, String> input, Map<String, Map<String, String>> lookupLists) {
		String result = null;
		if(this.variable != null) {
			result = input.get(this.variable);
		}
		if(result == null && this.constant != null) {
			result = this.constant;
		}
		return result;
	}
}
