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

package org.simplity.fm.core.validn;

import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.service.IServiceContext;

/**
 * a pair of fields that are mutually exclusive. That is, both should
 * not be specified
 *
 * @author simplity.org
 *
 */
public class ExclusiveValidation implements IValidation {
	private final String fieldName;
	private final int index1;
	private final int index2;
	private final boolean oneOfThemIsRequired;
	private final String messageId;

	/**
	 *
	 * @param index1
	 *            index of first field in the form.
	 * @param index2
	 *            index of second field in the form
	 * @param oneOfThemIsRequired
	 *            if true, at least one of them must have value. if false, both
	 *            not having value is OK.
	 * @param fieldName
	 *            with which message is to be added
	 * @param messageId
	 *            error message id
	 */
	public ExclusiveValidation(final int index1, final int index2, final boolean oneOfThemIsRequired,
			final String fieldName, final String messageId) {
		this.index1 = index1;
		this.index2 = index2;
		this.oneOfThemIsRequired = oneOfThemIsRequired;
		this.fieldName = fieldName;
		this.messageId = messageId;
	}

	@Override
	public boolean isValid(final Record record, final IServiceContext ctx) {
		final Object val1 = record.fetchValue(this.index1);
		final Object val2 = record.fetchValue(this.index2);

		if (val1 == null) {
			if (val2 == null && this.oneOfThemIsRequired) {
				ctx.addMessage(Message.newFieldError(this.fieldName, this.messageId));
				return false;
			}
			return true;

		}
		if (val2 == null) {
			return true;
		}
		ctx.addMessage(Message.newFieldError(this.fieldName, this.messageId));
		return false;
	}

	@Override
	public String getFieldName() {
		return this.fieldName;
	}
}
