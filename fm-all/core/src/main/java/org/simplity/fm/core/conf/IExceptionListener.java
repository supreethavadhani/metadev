/*
 * Copyright (c) 2020 simplity.org
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
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.simplity.fm.core.conf;

import org.simplity.fm.core.app.ApplicationError;
import org.simplity.fm.core.service.IServiceContext;

/**
 * utility to trigger actions to investigate into this exception
 *
 * @author simplity.org
 */
public interface IExceptionListener {

	/**
	 * typically run-time exception that is raised by the platform/VM and is not
	 * raised explicitly by the frame-work or App code
	 *
	 * @param ctx
	 *            could be null if the exception is detected outside of a
	 *            service context.
	 *
	 * @param e
	 *            exception that is reported when we have no context as in input
	 *            or service data
	 */
	void listen(IServiceContext ctx, Throwable e);

	/**
	 * triggered when an ApplicationError is raised. This is generally raised by
	 * the code when a design error is encountered. That is, this exception is
	 * explicitly raised by the framework or the App code.
	 *
	 * @param ctx
	 *            could be null if the exception is detected outside of a
	 *            service context.
	 *
	 * @param e
	 *            exception that is reported when we have no context as in input
	 *            or service data
	 */
	void listen(IServiceContext ctx, ApplicationError e);
}
