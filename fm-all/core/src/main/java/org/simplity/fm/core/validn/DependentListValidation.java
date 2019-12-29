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

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Message;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.service.IServiceContext;

/**
 * when the valid values for a field depends on the vale of its key-field. like
 * list of valid districts depends on stateCode
 *
 * @author simplity.org
 */
public class DependentListValidation implements IValidation {
	private final String listName;
	private final int fieldIndex;
	private final int parentFieldIndex;
	private final String fieldName;
	private final String messaageId;

	/**
	 * create the list with valid keys and values
	 *
	 * @param fieldIndex
	 * @param parentFieldIndex
	 * @param listName
	 * @param fieldName
	 * @param messageId
	 *
	 */
	public DependentListValidation(final int fieldIndex, final int parentFieldIndex, final String listName,
			final String fieldName, final String messageId) {
		this.fieldIndex = fieldIndex;
		this.parentFieldIndex = parentFieldIndex;
		this.listName = listName;
		this.fieldName = fieldName;
		this.messaageId = messageId;
	}

	@Override
	public boolean isValid(final SchemaData dataRow, final IServiceContext ctx) {
		final Object fieldValue = dataRow.getObject(this.fieldIndex);
		if (fieldValue == null) {
			return true;
		}
		final Object keyValue = dataRow.getObject(this.parentFieldIndex);
		if (keyValue == null) {
			return true;
		}

		final IValueList vl = ComponentProvider.getProvider().getValueList(this.listName);
		if (vl == null) {
			return true;
		}
		if (vl.isValid(fieldValue, keyValue)) {
			return true;
		}
		ctx.addMessage(Message.newFieldError(this.fieldName, this.messaageId));
		return false;
	}

	@Override
	public String getFieldName() {
		return this.fieldName;
	}
}