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

package org.simplity.fm.gen;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a Field row in fields sheet of a forms work book
 *
 * @author simplity.org
 *
 */
class Field {
	protected static final Logger logger = LoggerFactory.getLogger(Field.class);
	protected static final String C = ", ";

	String name;
	String dataType;
	String errorId;
	String defaultValue;
	boolean isRequired;
	String listName;
	String listKey;
	int index;

	void emitJavaCode(final StringBuilder sbf, final String dataTypesName) {
		sbf.append("\n\t\t\tnew ").append(this.getClass().getSimpleName()).append("(\"");
		sbf.append(this.name).append('"');
		sbf.append(C).append(this.index);
		sbf.append(C).append(dataTypesName).append('.').append(this.dataType);
		sbf.append(C).append(Util.escape(this.defaultValue));
		sbf.append(C).append(Util.escape(this.errorId));
		/*
		 * list is handled by inter-field in case key is specified
		 */
		if (this.listKey == null) {
			sbf.append(C).append(Util.escape(this.listName));
		} else {
			sbf.append(C).append("null");
		}
		this.emitJavaSpecific(sbf);
		sbf.append(')');
	}

	protected void emitJavaSpecific(final StringBuilder sbf) {
		sbf.append(C).append(this.isRequired);
	}
}
