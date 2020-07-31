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
 * data structure with field attributes that are to be overridden
 *
 * @author simplity.org
 *
 */
public class FieldOverride {
	/**
	 * name is unique within a record/form
	 */
	public String name;

	/**
	 * data type describes the type of value and restrictions (validations) on
	 * the value
	 */
	public String dataType;
	/**
	 * default value is used only if this is optional and the value is missing.
	 * not used if the field is mandatory
	 */
	public String defaultValue;
	/**
	 * refers to the message id/code that is used for i18n of messages
	 */
	public String messageId;
	/**
	 * required/mandatory. If set to true, text value of empty string and 0 for
	 * integral are assumed to be not valid. Relevant only for editable fields.
	 */
	public boolean isRequired;

	/**
	 * list that provides drop-down values for this field
	 */
	public String listName;
}
