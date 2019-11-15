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

import org.apache.poi.hpsf.Section;
import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.data.IoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Form {
	protected static final Logger logger = LoggerFactory.getLogger(Form.class);

	String name;
	String schemaName;
	String[] dbOperations;
	Field[] localFields;
	LinkedForm[] childForms;
	Field[] tempFields;
	Control[] controls;
	Section[] sections;

	void emitJavaForm(final StringBuilder sbf, final String packageName) {
		final boolean isComposite = this.childForms != null && this.childForms.length > 0;
		/*
		 * our package name is rootPAckage + any prefix/qualifier in our name
		 *
		 * e.g. if name a.b.schema1 then prefix is a.b and className is Schema1
		 */
		String pck = packageName + ".form";
		final String qual = Util.getClassQualifier(this.name);
		if (qual != null) {
			pck += '.' + qual;
		}
		sbf.append("package ").append(pck).append(";\n");
		Util.emitImport(sbf, ComponentProvider.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Form.class);
		if (isComposite) {
			Util.emitImport(sbf, org.simplity.fm.core.data.LinkedForm.class);
			Util.emitImport(sbf, org.simplity.fm.core.data.CompositeForm.class);
		}
		/*
		 * class declaration
		 */
		final String cls = Util.toClassName(this.name);
		sbf.append("\n/**\n *\n */\npublic class ").append(cls).append(" extends ");
		if (isComposite) {
			sbf.append("Composite");
		}
		sbf.append("Form {");

		/*
		 * static fields
		 */
		String p = "\n\tprotected static final ";
		sbf.append(p).append("String NAME = ").append(Util.escape(this.name)).append(';');
		sbf.append(p).append("String SCHEMA = ").append(Util.escape(this.schemaName)).append(';');
		sbf.append(p);
		this.getOps(sbf);

		if (isComposite) {
			this.emitChildStatics(sbf, p);
		}
		/*
		 * constructor
		 */
		p = "\n\t\tthis.";
		sbf.append("\n\t/**\n * constructor\n */\npublic ").append(cls).append("() {");
		sbf.append(p).append("name = NAME;");
		sbf.append(p).append("schema = ComponentProvider.getProvider().getSchema(SCHEMA);");
		sbf.append(p).append("operations = OPS;");
		if (isComposite) {
			sbf.append(p).append("linkedForms = CHILDREN;");
			sbf.append(p).append("initialize();");
		}

		sbf.append("\n\t}\n}\n");
	}

	private void emitChildStatics(final StringBuilder sbf, final String p) {
		sbf.append(p).append("LinkedForm[] CHILDREN = {");
		for (int i = 0; i < this.childForms.length; i++) {
			if (i != 0) {
				sbf.append(',');
			}
			sbf.append("\n\t\t\t");
			this.childForms[i].emitJavaCode(sbf);
		}
		sbf.append(p).append("};");
	}

	private void getOps(final StringBuilder sbf) {
		final IoType[] types = IoType.values();
		final boolean[] ops = new boolean[types.length];
		if (this.dbOperations != null) {
			/*
			 * we want to use a case-insensitive parsing enum names into a map
			 * in lower case
			 */
			final Map<String, Integer> indexes = new HashMap<>();
			for (final IoType iot : types) {
				indexes.put(iot.name().toLowerCase(), iot.ordinal());
			}

			/*
			 * now parse each of the operations and set the corresponding
			 * boolean to
			 * true
			 */
			for (final String op : this.dbOperations) {
				final Integer idx = indexes.get(op.toLowerCase());
				if (idx == null) {
					logger.error("{} is not a valid db operation (IoType). Ignored.");
				} else {
					ops[idx] = true;
				}
			}
		}
		sbf.append("boolean[] OPS = {");
		boolean firstOne = true;
		for (final boolean b : ops) {
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(", ");
			}
			sbf.append(b);
		}
		sbf.append("};");
	}

	/**
	 * @param sbf
	 * @param typesMap
	 * @param lists
	 * @param keyedLists
	 * @param tsImportPrefix
	 */
	void emitTs(final StringBuilder sbf, final Map<String, DataTypes> typesMap, final Map<String, ValueList> lists,
			final Map<String, KeyedList> keyedLists, final String tsImportPrefix) {
		// TODO Auto-generated method stub

	}

}
