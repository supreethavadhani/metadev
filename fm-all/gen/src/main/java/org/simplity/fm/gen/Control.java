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

import java.util.Map;

import org.simplity.fm.gen.DataTypes.DataType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * represents a Control on a client page
 *
 * @author simplity.org
 *
 */
class Control {
	protected static final Logger logger = LoggerFactory.getLogger(Control.class);
	protected static final String C = ", ";

	ControlType controlType;
	/**
	 * required if the type of control requires data
	 */
	String name;
	String label;
	String placeHolder;
	int width;
	int columnUnits;

	void emitTs(final StringBuilder def, final StringBuilder controls, final Map<String, Field> fields,
			final Map<String, DataType> dataTypes, final Map<String, ValueList> lists,
			final Map<String, KeyedList> keyedLists) {
		def.append("\n\t").append(this.name).append(":Field = {\n\t\tname:'").append(this.name).append("'");

		final String b = "\n\t\t,";
		def.append(b).append("controlType: '").append(this.controlType.name()).append('\'');

		if (this.label != null) {
			def.append(b).append("label: ").append(Util.escapeTs(this.label));
		}

		if (this.placeHolder != null) {
			def.append(b).append("placeHolder: ").append(Util.escapeTs(this.placeHolder));
		}

		if (this.width != 0) {
			def.append(b).append("width: ").append(this.width);
		}

		if (this.columnUnits != 0) {
			def.append(b).append("columnUnits: ").append(this.columnUnits);
		}

		final Field field = fields.get(this.name);
		if (field == null) {
			logger.info("Control {} is not defined as a field. No form control generated", this.name);
		} else {
			field.emitTs(def, controls, dataTypes, b, lists, keyedLists);
		}

		def.append("\n\t};");
	}
}
