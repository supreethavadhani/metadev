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
 * generic functions that can be called by utility functions in a generic way.
 * We strongly recommend that the implementations provide type-specific methods
 * for use by hand-written code.
 *
 * For example averageOf2Of3 function should provide an API double
 * calculate(doubl1 p1, double p2, double p3). While custom code can use this
 * API, generic utility code can continue to use the evaluateAPI.
 *
 *
 * @author simplity.org
 *
 */
public interface IFunction {
	/**
	 * evaluate this function with string as arguments. String arguments are
	 * parsed into the right type. Any parse error is added to the ctx. If the
	 * ctx is null, then an exception is thrown, with the assumption that it is
	 * being called inside a server-only context
	 *
	 * @param ctx
	 *            service context. Can be null in case this is executed outside
	 *            of a service context. implementations must take care of this
	 *
	 * @param params
	 *            must have the right type of values for the function
	 * @return result, possibly null; only primitive value/object are expected.
	 */
	Object parseAndEval(IServiceContext ctx, String... params);

	/**
	 * evaluate this function
	 *
	 * @param params
	 *            must have the right type of values for the function
	 * @return result, possibly null; only primitive value/object are expected.
	 */
	Object eval(Object... params);

	/**
	 * meta data about the parameters. Can be used by the caller before calling
	 * to validate input data
	 *
	 * @return array of value types for each parameter. has only one element if
	 *         variable arguments used. null if no arguments are expected
	 */
	ValueType[] getArgumentTypes();

	/**
	 * meta data about the parameters. Can be used by the caller before calling
	 * to validate input data
	 *
	 * @return array of value types for each parameter. Note that the function
	 *         would receive string and parse them into these types.
	 */
	ValueType getReturnType();

	/**
	 *
	 * @return -1 if it accepts var-args. 0 if no parameters are expected.
	 */
	int getNbrArguments();

	/**
	 *
	 * @return true if this function accepts var-args as its sole parameter.
	 *         false otherwise. Note that the function can not be defined to
	 *         take its last argument as var-args
	 */
	boolean acceptsVarArgs();
}
