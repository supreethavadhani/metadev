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

package org.simplity.fm.core.data;

import org.simplity.fm.core.service.IService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * a wrapper on schema to offer this as client-facing service
 *
 * @author simplity.org
 *
 */
public class Form {
	static protected final Logger logger = LoggerFactory.getLogger(Form.class);

	protected String name;
	protected Schema schema;
	/*
	 * db operations that are to be exposed thru this form. array corresponds to
	 * the ordinals of IoType
	 */
	protected boolean[] operations;

	/**
	 * get the service instance for the desired operation on this form
	 *
	 * @param opern
	 * @return service, or null if this form is not designed for this operation
	 */
	public IService getService(final IoType opern) {
		if (this.operations == null || this.operations[opern.ordinal()] == false) {
			return null;
		}
		return this.schema.getService(opern);
	}
}
