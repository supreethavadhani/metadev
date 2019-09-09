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

/**
 * data structure for meta data for a child form
 * 
 * @author simplity.org
 *
 */
public class DbLink {
	/**
	 * field names from the parent form that form the parent-key for the child
	 * form
	 */
	public String[] childLinkNames;
	/**
	 * column names are from the child table, but the values for the parameter
	 * would come from the parent form
	 * e.g. where childCol1=? and childCll2=?
	 */
	public String linkWhereClause;
	/**
	 * db parameters for the where clause
	 */
	public FormDbParam[] linkParentParams;
	/**
	 * db meta data of the child form
	 */
	public DbMetaData childMeta;
	/**
	 * number of fields in the child form. This is the total of all fields, not
	 * just the fields that are linked to the DB. This is used to create an
	 * array of values for the form data
	 */
	public int nbrChildFields;
}
