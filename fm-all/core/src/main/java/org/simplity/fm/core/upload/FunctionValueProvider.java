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

package org.simplity.fm.core.upload;

import java.util.Map;

import org.simplity.fm.core.fn.IFunction;
import org.simplity.fm.core.service.IServiceContext;

/**
 * Defines a function that evaluates to give a string
 *
 * @author simplity.org
 *
 */
class FunctionValueProvider implements IValueProvider {
	final IFunction function;
	final IValueProvider[] params;

	/**
	 *
	 * @param function
	 *            non-null function to be executed
	 * @param params
	 *            value providers for the parameters. number must match the
	 *            desired number of params for the function
	 */
	FunctionValueProvider(final IFunction function, final IValueProvider[] params) {
		this.function = function;
		this.params = params;
	}

	@Override
	public String getValue(final Map<String, String> input, final IServiceContext ctx) {
		String[] values = null;
		if (this.params != null) {
			values = new String[this.params.length];
			for (int i = 0; i < values.length; i++) {
				values[i] = this.params[i].getValue(input, ctx);
			}
		}
		final Object obj = this.function.parseAndEval(ctx, values);
		if (obj == null) {
			return "";
		}
		return obj.toString();
	}
}
