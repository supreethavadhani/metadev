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

package org.simplity.fm.core.form;

import org.simplity.fm.core.ComponentProvider;

/**
 * represents data structure for field in a form that is designed to hold
 * tabular data
 * 
 * @author simplity.org
 *
 */
public class ChildFormNotUsed {
	/**
	 * name of this field
	 */
	public final String fieldName;
	/**
	 * columns in this table. or fields in this sub-form is described in a
	 * separate form
	 */
	public final FormNotUsed form;

	/**
	 * is this tabular data, or just a sub-form (section)
	 */
	public final boolean isTabular;
	/**
	 * minimum rows of data required. 0 if this is not a grid, or the data is
	 * optional
	 */
	public final int minRows;
	/**
	 * maximum rows of data required. 0 if this is not a grid, or you do not
	 * want to restrict data
	 */
	public final int maxRows;
	/**
	 * message to be used if the grid has less than the min or greater than the
	 * max rows. null if no min/max restrictions
	 */
	public final String errorMessageId;

	/**
	 * 
	 * @param fieldName
	 *            non-null unique across all fields of the form
	 * @param formName
	 *            non-null name of the form that describes fields/columns in the
	 *            child-form
	 * @param isTabular
	 *            is this a tabular form, or just a sub-form
	 * @param minRows
	 *            for validation of data
	 * @param maxRows
	 *            for validation of data. though 0 means unlimited, we strongly
	 *            encourage a reasonable limit
	 * @param errorMessageId
	 *            message id to be used if number of data rows fails validation
	 */
	public ChildFormNotUsed(String fieldName, String formName, boolean isTabular, int minRows, int maxRows,
			String errorMessageId) {
		this.fieldName = fieldName;
		this.form = ComponentProvider.getProvider().getForm(formName);
		this.isTabular = isTabular;
		this.minRows = minRows;
		this.maxRows = maxRows;
		this.errorMessageId = errorMessageId;
	}
}
