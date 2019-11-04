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

import org.simplity.fm.core.validn.IValidation;

/**
 * 
 * @author simplity.org
 *
 */
public abstract class FormValidation implements IValidation {

	protected final String name1;
	protected final String name2;
	protected final boolean boolValue;
	protected final String errorMessageId;

	/**
	 * 
	 * @param fieldNam1
	 * @param fieldName2
	 * @param boolValue
	 * @param errorMessageId
	 */
	public FormValidation(String fieldNam1, String fieldName2, boolean boolValue, String errorMessageId) {
		this.name1 = fieldNam1;
		this.name2 = fieldName2;
		this.boolValue = boolValue;
		this.errorMessageId = errorMessageId;
	}
}
