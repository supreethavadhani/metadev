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

package org.simplity.fm.core;

import org.simplity.fm.core.datatypes.ValueType;
import org.simplity.fm.core.service.IServiceContext;

/**
 * generic function that takes number of strings as parameters and returns a
 * string.
 * 
 * 
 * @author simplity.org
 *
 */
public interface IFunction {
	/**
	 * evaluate this function
	 * 
	 * @param ctx
	 *            service context. Can be null in case this is executed outside
	 *            of a service context. implementations must take care of this
	 * 
	 * @param params
	 *            must have the right type of values for the function
	 * @return result, possibly null;
	 */
	public String eval(IServiceContext ctx, String... params);

	/**
	 * meta data about the parameters. Can be used by the caller before calling
	 * to validate input data
	 * 
	 * @return array of value types for each parameter. Note that the function
	 *         would receive string and parse them into these types.
	 */
	public ValueType[] getParamTypes();
}
