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

import org.simplity.fm.core.app.ApplicationError;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.service.IServiceContext;

/**
 * base class for functions that deal only with numbers. we use double as the
 * type, to be doubly sure of future needs !!
 *
 */
public abstract class NumericFunction implements IFunction {

	/**
	 * -1 implies var-args.
	 */
	protected int nbrArgs;

	/**
	 * will have Decimal as type. kept ready for convenience
	 */
	protected ValueType[] argTypes;

	/**
	 * called by sub-classes
	 *
	 * @param n
	 *            -1 if it is var arg
	 */
	protected final void setNbrArgs(final int n) {
		this.nbrArgs = n;
		if (n == 0) {
			return;
		}

		final int nbr = n == -1 ? 1 : n;

		this.argTypes = new ValueType[nbr];
		for (int i = 0; i < nbr; i++) {
			this.argTypes[i] = ValueType.Decimal;
		}
	}

	@Override
	public final int getNbrArguments() {
		return this.nbrArgs;
	}

	@Override
	public final boolean acceptsVarArgs() {
		return this.nbrArgs == -1;
	}

	@Override
	public final Object parseAndEval(final IServiceContext ctx, final String... params) {
		return this.calculate(this.parse(params));
	}

	@Override
	public final Object eval(final Object... args) {

		return this.calculate(this.toDouble(args));
	}

	@Override
	public final ValueType[] getArgumentTypes() {
		return this.argTypes;
	}

	@Override
	public final ValueType getReturnType() {
		return ValueType.Decimal;
	}

	/**
	 * calculate the numeric result based on the numeric arguments.
	 *
	 * @param args
	 *            guaranteed to contain compatible length and values.
	 * @return result
	 */
	public final double calculate(final double[] args) {
		this.ensureNbrArgs(args == null ? 0 : args.length);
		return this.evaluate(args);
	}

	protected abstract double evaluate(double[] args);

	private void ensureNbrArgs(final int nbr) {
		if (this.nbrArgs >= 0 && nbr != this.nbrArgs) {
			this.throwError(nbr + " params received");
		}
	}

	private double[] toDouble(final Object[] args) {
		if (args == null) {
			return null;
		}

		final int nbr = args.length;
		final double[] result = new double[nbr];
		for (int i = 0; i < nbr; i++) {
			try {
				result[i] = ((Number) args[i]).doubleValue();
			} catch (final ClassCastException e) {
				this.throwError("argument at position " + i + " is " + args[i].getClass().getName());
			}
		}
		return result;
	}

	private void throwError(final String msg) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append(" Function ").append(this.getClass().getSimpleName()).append(" expects ");
		if (this.nbrArgs == 0) {
			sbf.append("no");
		} else if (this.nbrArgs == -1) {
			sbf.append("any number of");
		} else {
			sbf.append(this.nbrArgs);
		}
		sbf.append(" arguments. Arguments are always numeric, and the return tyoe is also numeric");
		throw new ApplicationError(msg + sbf.toString());
	}

	private double[] parse(final String[] args) {
		final int n = args == null ? 0 : args.length;
		this.ensureNbrArgs(n);

		if (args == null) {
			return null;
		}

		final double[] result = new double[n];

		if (n == 0) {
			return result;
		}

		for (int i = 0; i < n; i++) {
			final String s = args[i];
			try {
				result[i] = Double.parseDouble(s);
			} catch (final NumberFormatException e) {
				final String msg = "argument at " + i + " is " + s + " which is not a valid number";
				this.throwError(msg);
			}
		}
		return result;
	}
}
