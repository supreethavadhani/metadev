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

import org.simplity.fm.core.ApplicationError;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.service.IServiceContext;

/**
 * base class for implementation of different types of functions
 *
 */
public abstract class AbstractFunction implements IFunction {

	protected ValueType returnType;
	protected ValueType[] argTypes;
	protected boolean isVarArgs;

	@Override
	public int getNbrParams() {
		if (this.isVarArgs) {
			return -1;
		}
		if (this.argTypes == null) {
			return 0;
		}
		return this.argTypes.length;
	}

	@Override
	public boolean accepstVarArgs() {
		return this.isVarArgs;
	}

	@Override
	public Object parseAndEval(final IServiceContext ctx, final String... params) {
		return this.execute(ctx, this.parse(params, ctx));
	}

	@Override
	public Object eval(final IServiceContext ctx, final Object... params) {
		this.ensureTypes(params);
		return this.execute(ctx, params);
	}

	@Override
	public ValueType[] getParamTypes() {
		return this.argTypes;
	}

	@Override
	public ValueType getReturnType() {
		return this.returnType;
	}

	/**
	 * method to be implemented by concrete classes.
	 *
	 * @param ctx
	 * @param params
	 *            guaranteed to contain compatible length and values.
	 * @return result
	 */
	protected abstract Object execute(IServiceContext ctx, Object[] params);

	private void ensureNbrParams(final int nbrParams) {
		final int n = this.getNbrParams();
		if (n >= 0 && nbrParams != n) {
			this.throwError(nbrParams + " params received");
		}
	}

	private void ensureTypes(final Object[] params) {
		for (int i = 0; i < params.length; i++) {
			if (!this.argTypes[i].isRighType(params[i])) {
				this.throwError("argument at position " + i + " is " + params[i].getClass().getName());
			}
		}
	}

	private void throwError(final String msg) {
		final StringBuilder sbf = new StringBuilder();
		sbf.append(" Function ").append(this.getClass().getSimpleName()).append(" expects ");
		if (this.isVarArgs) {
			sbf.append("any number of rguments of type " + this.argTypes[0].name());
		} else if (this.argTypes == null || this.argTypes.length == 0) {
			sbf.append("no rguments.");
		} else {
			final int n = this.argTypes.length;
			sbf.append(n).append(" arguments of type(").append(this.argTypes[0].name());
			for (int i = 1; i < n; i++) {
				sbf.append(", ").append(this.argTypes[i].name());
			}
			sbf.append(")");
		}

		throw new ApplicationError(msg + sbf.toString());
	}

	private Object[] parse(final String[] params, final IServiceContext ctx) {
		final int n = params == null ? 0 : params.length;
		this.ensureNbrParams(n);

		if (params == null) {
			return null;
		}

		final Object[] result = new Object[n];

		if (n == 0) {
			return result;
		}

		ValueType vt = null;
		if (this.isVarArgs) {
			vt = this.argTypes[0];
		}

		for (int i = 0; i < n; i++) {
			final String s = params[i];
			if (s == null) {
				continue;
			}

			final Object obj = vt == null ? this.argTypes[i].parse(s) : vt.parse(s);
			if (obj != null) {
				result[i] = obj;
				continue;
			}

			/*
			 * we have a parse error. add it to the ctx, or throw up your
			 * hands!!
			 */
			final String msg = s + " is not a valid value for argument at " + i;
			if (ctx != null) {
				ctx.addMessage(Message.newError(msg));
				return null;
			}

			this.throwError(msg);
		}
		return result;
	}
}
