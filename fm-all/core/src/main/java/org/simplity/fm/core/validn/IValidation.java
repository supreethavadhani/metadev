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

import org.simplity.fm.core.data.DataRow;
import org.simplity.fm.core.service.IServiceContext;

/**
 * represents a validation at the form level, including inter-field
 * validations.This should not be used for field level validations. (Field level
 * validations are handled at <code>DataElement</code> level
 *
 * @author simplity.org
 *
 */
public interface IValidation {
	/**
	 * execute this validation for a data row
	 *
	 * @param dataRow
	 * @param ctx
	 * @return true if all OK. false if an error message is added to the list
	 */
	boolean isValid(DataRow<?> dataRow, IServiceContext ctx);

	/**
	 *
	 * @return primary/any field that is associated with this validation
	 */
	String getFieldName();
}
