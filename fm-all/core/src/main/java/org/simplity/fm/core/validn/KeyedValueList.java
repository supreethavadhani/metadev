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

import java.util.HashMap;
import java.util.Map;

/**
 * Base class to specify an enumeration of valid values for a field. The
 * enumeration are further restricted based on a key field. This class is
 * extended by the generated key value list classes
 * 
 * @author simplity.org
 */
public class KeyedValueList implements IValueList{
	protected String name;
	protected Map<Object, ValueList> values = new HashMap<>();

	@Override
	public boolean isValid(Object fieldValue, Object keyValue) {
		ValueList vl  = this.values.get(keyValue);
		if (vl == null) {
			return false;
		}
		return vl.isValid(fieldValue, null);
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isKeyBased() {
		return true;
	}

	@Override
	public Object[][] getList(Object keyValue) {
		ValueList vl  = this.values.get(keyValue);
		if (vl == null) {
			return null;
		}
		return vl.getList(null);
	}
}