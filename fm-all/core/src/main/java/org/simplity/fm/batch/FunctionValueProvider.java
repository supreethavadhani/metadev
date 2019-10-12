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

package org.simplity.fm.batch;

import java.util.Map;

/**
 * Defines a function that evaluates to give a string
 * 
 * @author simplity.org
 *
 */
public class FunctionValueProvider implements IValueProvider {
	IFunction function;
	IValueProvider[] params;

	@Override
	public String getValue(Map<String, String> input) {
		String[] values = null;
		if (this.params != null) {
			values = new String[this.params.length];
			for (int i = 0; i < values.length; i++) {
				values[i] = this.params[i].getValue(input);
			}
		}
		return this.function.evaluate(values);
	}

	/**
	 * a function that takes an array of string as params and returns a string
	 * 
	 * @author simplity.org
	 *
	 */
	@FunctionalInterface
	public interface IFunction {
		/**
		 * 
		 * @param params
		 *            null if this specific instance requires no params. must
		 *            have the desired number of elements.
		 * @return resultant string. can be null.
		 */
		public String evaluate(String[] params);
	}
}
