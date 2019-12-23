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
import org.simplity.fm.core.data.DataRow;
import org.simplity.fm.core.service.IServiceContext;

/**
 * a pair of fields that are mutually inclusive. Either both re present, or both
 * are absent.
 * There is also flexibility to use a specific value for field1 for this rule to
 * be used.
 *
 * for e.g. machineName and description.In this case, if machine is specified,
 * it must be described. And you should not describe an unspecified machine
 *
 * if country-code is 91, then pin code must be specified, otherwise it should
 * not be specified
 *
 * @author simplity.org
 *
 */
public class InclusiveValidation implements IValidation {
	/**
	 * primary/main field
	 */
	private final int mainIndex;
	/**
	 * field to be validated
	 */
	private final int dependentIndex;
	/**
	 * null if this rule is always applicable. Non-null if this rule is
	 * applicable only when the main field has this value
	 */
	private final String mainValue;
	/**
	 * field name to be pushed to the error message
	 */
	private final String fieldName;
	private final String messageId;

	/**
	 *
	 * @param mainIndex
	 * @param dependentIndex
	 * @param mainValue
	 * @param fieldName
	 * @param messageId
	 */
	public InclusiveValidation(final int mainIndex, final int dependentIndex, final String mainValue,
			final String fieldName, final String messageId) {
		this.mainIndex = mainIndex;
		this.dependentIndex = dependentIndex;
		this.mainValue = mainValue;
		this.fieldName = fieldName;
		this.messageId = messageId;
	}

	@Override
	public boolean isValid(final DataRow<?> dataRow, final IServiceContext ctx) {
		final Object main = dataRow.getObject(this.mainIndex);
		final Object dep = dataRow.getObject(this.dependentIndex);

		boolean mainSpecified = false;
		if (main != null) {
			if (this.mainValue == null) {
				mainSpecified = true;
			} else {
				mainSpecified = this.mainValue.equals(main.toString());
			}
		}

		if (mainSpecified) {
			if (dep != null) {
				return true;
			}
		} else {
			if (dep == null) {
				return true;
			}
		}
		ctx.addMessage(Message.newFieldError(this.fieldName, this.messageId));
		return false;
	}

	@Override
	public String getFieldName() {
		return this.fieldName;
	}
}
