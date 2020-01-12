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

package org.simplity.fm.gen.element;

/**
 * <p>
 * Represents a page, as understood by a web-app. A page is typically used to
 * carry out a specific transaction, say add-a-customer, or show some data, say
 * show-customer-details. This is the top level component in our hierarchy of
 * display elements.
 * </p>
 *
 * <p>
 * page has an array of <code>DisplayElement</code> as its children. A concrete
 * instance may define additional display elements, like header, navigation
 * etc..
 * </p>
 *
 * @author simplity.org
 *
 */
public abstract class Page {
	protected DisplayElement[] childElements;

	/**
	 * emit complete content for the .ts file for this page
	 *
	 * @param sbf
	 * @param importPrefix
	 * @return true if all OK. False in as of any error because of which the
	 *         file should not be written out
	 */
	public abstract boolean emitTs(StringBuilder sbf, String importPrefix);
}
