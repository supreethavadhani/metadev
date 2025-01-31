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

import java.util.HashMap;
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
	private static final Map<String, ControlType> controlTypes = createType();
	private static final Map<String, ButtonType> buttonTypes = createButtonType();
	protected static final Logger logger = LoggerFactory.getLogger(Control.class);
	protected static final String C = ", ";

	String controlType;
	String buttonType;
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
		def.append(b).append("controlType: '").append(this.getControlType().name()).append('\'');
		if (this.buttonType != null) {
			def.append(b).append("buttonType: '").append(this.getButtonType().name()).append('\'');
		}
		def.append(b).append("label: ");
		if (this.label == null) {
			def.append(Util.escapeTs(this.name));
		} else {
			def.append(Util.escapeTs(this.label));
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

		if (fields != null && this.name != null) {
			final Field field = fields.get(this.name);
			if (field == null) {
				final String msg = "Control " + this.name
						+ "is not defined as a field. Control generated to result in compilation error";
				def.append(msg);
				logger.error(msg);
			} else {
				field.emitTs(def, controls, dataTypes, b, lists, keyedLists);
			}
		}

		def.append("\n\t};");
	}

	/**
	 * @return
	 */
	private static Map<String, ControlType> createType() {
		final Map<String, ControlType> map = new HashMap<>();
		for (final ControlType ct : ControlType.values()) {
			map.put(ct.name().toLowerCase(), ct);
		}
		return map;
	}

	private static Map<String, ButtonType> createButtonType() {
		final Map<String, ButtonType> map = new HashMap<>();
		for (final ButtonType bt : ButtonType.values()) {
			map.put(bt.name().toLowerCase(), bt);
		}
		return map;
	}

	/**
	 *
	 * @return control type, possibly null
	 */
	public ControlType getControlType() {
		if (this.controlType == null) {
			return null;
		}
		return controlTypes.get(this.controlType.toLowerCase());
	}

	/**
	 *
	 * @return control type, possibly null
	 */
	public ButtonType getButtonType() {
		if (this.buttonType == null) {
			return null;
		}
		return buttonTypes.get(this.buttonType.toLowerCase());
	}
}
