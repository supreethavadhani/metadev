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
public class Max extends AbstractFunction {
	private static final ValueType[] TYPES = { ValueType.Decimal };

	/**
	 * default constructor
	 */
	public Max() {
		this.argTypes = TYPES;
		this.isVarArgs = true;
		this.returnType = ValueType.Decimal;
	}

	@Override
	protected Double execute(final IServiceContext ctx, final Object[] args) {
		if (args == null || args.length == 0) {
			return 0.0;
		}

		double max = Double.MIN_VALUE;
		for (final Object n : args) {
			final double m = (double) n;
			if (m > max) {
				max = m;
			}
		}

		return max;
	}

	/**
	 *
	 * @param args
	 * @return sum of all the arguments
	 */
	public long max(final long... args) {
		if (args == null || args.length == 0) {
			return 0;
		}

		long max = Long.MIN_VALUE;
		for (final long n : args) {
			if (n > max) {
				max = n;
			}
		}

		return max;
	}
}
