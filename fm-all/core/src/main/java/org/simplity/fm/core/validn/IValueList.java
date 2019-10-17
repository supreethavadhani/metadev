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

import java.util.Map;

import org.simplity.fm.core.service.IserviceContext;

/**
 * design-time or run-time list that can be used for validating a field value
 * and supplying possible list of values for that field
 * 
 * @author simplity.org
 *
 */
public interface IValueList {
	/**
	 * 
	 * @return unique name of this list. This is normally string,but it can be
	 *         long as well.
	 */
	public Object getName();

	/**
	 * is this list key-based?
	 * 
	 * @return true if the list depends on a key. false if the list is fixed
	 */
	public boolean isKeyBased();

	/**
	 * get a list of valid values
	 * 
	 * @param keyValue
	 *            null if this list is not key-based.
	 * @param ctx
	 *            non-null for run-time list. can be null for generated lists
	 *            (simple and keyed)
	 * @return array of [internalValue, displayValue]. internal value could be
	 *         string or number. null if no such list
	 */
	public Object[][] getList(Object keyValue, IserviceContext ctx);

	/**
	 * is the field value valid as per this list?
	 * 
	 * @param fieldVale
	 *            non-null value of the right type. Typically either String or
	 *            Long
	 * @param keyValue
	 *            null if this list is not key-based. value of the right type if
	 *            it is key-based
	 * @return true of the field value is valid. false if it is invalid, or
	 *         these is any error in the validation process
	 */
	public boolean isValid(Object fieldVale, Object keyValue);

	/**
	 * Reverse look-up. get internal id for a display name
	 * 
	 * 
	 * @param ctx
	 *            null if a static list is to be used.must be non-null for
	 *            runtime lists
	 * @return map with key = keyName|displayText and value = internal value.
	 *         e.g if keyId=91. keyName=India, internalValue=KA
	 *         displyaText=Karnataka, then we will have an entry with
	 *         key="India|Karnataka" and value="KA"
	 */
	public Map<String, String> getAll(IserviceContext ctx);
}
