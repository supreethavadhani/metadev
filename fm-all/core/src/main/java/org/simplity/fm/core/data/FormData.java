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
 * <p>
 * Abstract representation of data associated with a form. Each Concrete class
 * of Form will has an associated concrete class of FormData.
 * </p>
 *
 * <p>
 * This is a wrapper on DataObject to accommodate values for local fields if
 * any. While this base class defines an array of objects to hold all local
 * field values, generated concrete classes define setters and getters for
 * specific fields
 * </p>
 *
 * @author simplity.org
 *
 */
public abstract class FormData {

	/**
	 * data for the schema that this form is based on
	 */
	protected final DataObject dataObject;
	/**
	 * data for local fields that this form may have. null if there are no local
	 * fields.
	 */
	protected final Object[] fieldValues;

	/**
	 * form data for all linked forms
	 */
	protected final FormDataTable[] linkedData;

	/**
	 * delegated back by the concrete class to ensure that only the data
	 * components are right
	 *
	 */
	protected FormData(final DataObject dataObject, final Object[] fieldValues, final FormDataTable[] linkedData) {
		this.dataObject = dataObject;
		this.fieldValues = fieldValues;
		this.linkedData = linkedData;
	}

	/*
	 * called by generated concrete class, and never by end-programmers.
	 */
	protected DataObject getDataObject() {
		return this.dataObject;
	}

	protected Object getFieldValue(final int index) {
		return this.fieldValues[index];
	}

	protected void setFieldValue(final int index, final Object value) {
		this.fieldValues[index] = value;
	}

	protected Object[] getFieldValues() {
		return this.fieldValues;
	}

	protected FormDataTable[] getLinkedData() {
		return this.linkedData;
	}

	protected FormDataTable getLinkedData(final int idx) {
		return this.linkedData[idx];
	}
}
