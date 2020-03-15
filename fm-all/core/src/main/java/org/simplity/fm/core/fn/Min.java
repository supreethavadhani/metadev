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
 * Minimum of the numbers
 *
 */
public class Min extends AbstractFunction {
	private static final ValueType[] TYPES = { ValueType.Decimal };

	/**
	 * default constructor
	 */
	public Min() {
		this.argTypes = TYPES;
		this.isVarArgs = true;
		this.returnType = ValueType.Decimal;
	}

	@Override
	protected Double execute(final IServiceContext ctx, final Object[] args) {
		if (args == null || args.length == 0) {
			return 0.0;
		}

		double min = Double.MAX_VALUE;
		for (final Object n : args) {
			final double m = (double) n;
			if (m < min) {
				min = m;
			}
		}

		return min;
	}

	/**
	 *
	 * @param args
	 * @return sum of all the arguments
	 */
	public double min(final double... args) {
		if (args == null || args.length == 0) {
			return 0;
		}

		double min = Double.MAX_VALUE;
		for (final double n : args) {
			if (n < min) {
				min = n;
			}
		}

		return min;
	}
}
