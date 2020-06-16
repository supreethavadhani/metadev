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

package org.simplity.fm.core.upload;

import java.util.Map;

import org.simplity.fm.core.service.IServiceContext;

/**
 * client for an upload process
 * 
 * @author simplity.org
 *
 */
@FunctionalInterface
public interface IUploadClient {

	/**
	 * called by the server to get the next row. This is an unusual method that
	 * combines two methods into one.
	 * This is a call-back method called by the uploader to get the next row to
	 * be uploaded. while doing so, it also provides the result of uploading the
	 * last row
	 * 
	 * @param ctx
	 *            the service context in which this process is running
	 * 
	 * @return field/column values for this row. The caller may add some more
	 *         variables to this collection in the upload process for this row.
	 *         It is quite safe for you to clear it and re-use it for next
	 *         call-back though.
	 * 
	 *         null to imply end of data.
	 */
	public Map<String, String> nextRow(IServiceContext ctx);
}
