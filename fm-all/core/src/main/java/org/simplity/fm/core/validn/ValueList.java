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

import java.util.HashSet;
import java.util.Set;

/**
 * Base class for defining a set of enumerations as valid values of a field.
 * This class is extended by the generated ValueList classes
 * 
 * @author simplity.org
 */
public class ValueList implements IValueList {
	protected String name;
	protected Set<String> values;
	/*
	 * [object,string][] first element could be either number or text, but the second one always is text
	 */
	protected Object[][] valueList;

	/**
	 * 
	 * @param name non-null unique name
	 * @param valueList non-null non-empty String[][String, String]
	 */
	public ValueList(String name, String[][] valueList) {
		this.name = name;
		this.valueList = valueList;
		this.values = new HashSet<>();
		for(String[] arr: valueList) {
			this.values.add(arr[0]);
		}
	}
	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isKeyBased() {
		return false;
	}
	@Override
	public boolean isValid(Object fieldValue, Object keyValue) {
		return this.values.contains(fieldValue.toString());
	}

	@Override
	public Object[][] getList(String keyValue) {
		return this.valueList;
	}
}