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

package org.simplity.fm.core.fn;

import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.service.IServiceContext;

/**
 * Concatenate strings
 *
 */
public class Sum extends AbstractFunction {
	private static final ValueType[] TYPES = { ValueType.Integer };

	/**
	 * default constructor
	 */
	public Sum() {
		this.argTypes = TYPES;
		this.isVarArgs = true;
		this.returnType = ValueType.Integer;
	}

	@Override
	protected Long execute(final IServiceContext ctx, final Object[] args) {
		if (args == null || args.length == 0) {
			return 0L;
		}

		long result = 0;
		for (final Object n : args) {
			result += ((Number) n).longValue();
		}

		return result;
	}

	/**
	 *
	 * @param args
	 * @return sum of all the arguments
	 */
	public long sum(final long... args) {
		if (args == null || args.length == 0) {
			return 0;
		}

		long result = 0;
		for (final long n : args) {
			result += n;
		}

		return result;
	}

}
