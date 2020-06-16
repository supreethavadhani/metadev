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

package org.simplity.fm.core.app;

/**
 * Base unchecked exception that represents an exception caused because of some
 * flaw in the internal design/implementation of the pp.
 * for example inconsistent data in the DB, incompatible arguments etc..
 *
 * The framework throws this whenever it detects exceptions that are not
 * supposed to happen by design. It must be caught at the highest level, say
 * service agent.
 *
 * Motivation for this design is to provide a mechanism for the app to centrally
 * handle all such exceptions and plumb it to the org-wide infrastructure.
 *
 * Applications are encouraged to create sub-classes for better error-management
 */
public class ApplicationError extends Error {
	private static final long serialVersionUID = 1L;

	/**
	 *
	 * @param msg
	 */
	public ApplicationError(final String msg) {
		super(msg);
	}

	/**
	 *
	 * @param msg
	 * @param e
	 */
	public ApplicationError(final String msg, final Throwable e) {
		super(msg, e);
	}
}
