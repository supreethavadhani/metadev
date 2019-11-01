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

package org.simplity.fm.upload;

import java.time.Instant;

import org.simplity.fm.core.Message;

/**
 * result of an upload process
 * 
 * @author simplity.org
 *
 */
public class UploadResult {
	/**
	 * instance a which first row was started(after any set-up)
	 */
	public final Instant startedAt;
	/**
	 * instance at which last row is processed
	 */
	public final Instant doneAt;
	/**
	 * total rows processed
	 */
	public final int nbrRowsProcessed;
	/**
	 * number of rows in error.
	 */
	public final int nbrRowsInError;
	/**
	 * error messages if any. Could be empty, but not null
	 */
	public Message[] errors;

	/**
	 * 
	 * @param startedAt
	 *            instance a which first row was started(after any set-up)
	 * @param doneAt
	 *            instance at which last row is processed
	 * @param nbrRowsProcessed
	 *            total rows processed
	 * @param nbrRowsInError
	 *            number of rows in error
	 * @param errors
	 *            error messages if any. Could be empty, but not null
	 */
	public UploadResult(Instant startedAt, Instant doneAt, int nbrRowsProcessed, int nbrRowsInError,
			Message[] errors) {
		this.startedAt = startedAt;
		this.doneAt = doneAt;
		this.nbrRowsProcessed = nbrRowsProcessed;
		this.nbrRowsInError = nbrRowsInError;
	}
}
