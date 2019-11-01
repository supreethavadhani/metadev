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

/**
 * @author simplity.org
 *
 */
public class LinkedForm {
	/**
	 * non-null unique across all fields of the form
	 */
	protected final String linkName;
	/**
	 * form being linked
	 */
	protected final Form form;
	/**
	 * does this form cotaan multipl rows of data?
	 */
	protected final boolean isTabular;
	/**
	 * if this is tabular, min rows expected from client
	 */
	protected final int minRows;
	/**
	 * if this is tabular, max rows expected from client.
	 */
	protected final int maxRows;
	/**
	 * field names from the parent form that are used for linking
	 */
	protected final String[] parentLinkNames;
	/**
	 * field names from the child form that form the parent-key for the child
	 * form
	 */
	public String[] childLinkNames;
	/**
	 * in case min/max rows violated, what is the error message to be used to
	 * report this problem
	 */
	protected final String errorMessageId;

	/**
	 * column names are from the child table, but the values for the parameter
	 * would come from the parent form
	 * e.g. where childCol1=? and childCll2=?
	 */
	public String linkWhereClause;

	/**
	 *
	 * @param linkName
	 *            non-null unique across all fields of the form
	 * @param form
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
	public LinkedForm(final String linkName, final Form form, final boolean isTabular, final int minRows,
			final int maxRows, final String errorMessageId) {
		this.linkName = linkName;
		this.form = form;
		this.isTabular = isTabular;
		this.minRows = minRows;
		this.maxRows = maxRows;
		this.errorMessageId = errorMessageId;
	}

}
