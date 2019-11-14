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

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.JsonUtil;
import org.simplity.fm.core.data.IoType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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
	ChildForm[] childForms;
	Field[] tempFields;
	Control[] controls;
	boolean[] dbOperations;

	boolean fromJson(final JsonObject json, final String fileName) {
		boolean allOk = true;
		this.name = JsonUtil.getStringMember(json, "name");
		this.schemaName = JsonUtil.getStringMember(json, "schemaName");
		if (this.name == null) {
			logger.error("form has no name");
			allOk = false;
		} else if (this.name.equals(fileName) == false) {
			logger.error("File {} has a form named {}. This is not allowed. file name must matchthe form name",
					fileName, this.name);
			allOk = false;
		}
		if (this.schemaName == null) {
			logger.warn("form has no schemaName. Will be useful for client specific activity only");
		}

		this.parseOps(json.getAsJsonObject("operations"));

		return allOk;
	}

	private void parseOps(final JsonObject json) {
		this.dbOperations = new boolean[IoType.values().length];
		if (json == null) {
			logger.warn("No bb operations are enabled. Form will not be processed on its own");
			return;
		}
		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {
			try {
				if (entry.getValue().getAsBoolean()) {
					this.dbOperations[IoType.valueOf(entry.getKey()).ordinal()] = true;
				}
			} catch (final Exception e) {
				logger.error(e.getMessage());
			}
		}
	}

	void emitJavaClass(final StringBuilder sbf, final String packageName) {
		String pck = packageName;
		final String prefix = Util.getClassQualifier(this.name);
		if (prefix != null) {
			pck += '.' + prefix;
		}
		sbf.append("package ").append(pck).append(";\n");
		Util.emitImport(sbf, ComponentProvider.class);
		Util.emitImport(sbf, org.simplity.fm.core.data.Form.class);
		/*
		 * classdeclaration
		 */
		final String cls = Util.toClassName(this.name);
		sbf.append("\n/**\n *\n */\npublic class ").append(cls).append(" extends Form {");

		/*
		 * static fields
		 */
		final String p = "\n\tprotected static final ";
		sbf.append(p).append("String NAME = ").append(Util.escape(this.name));
		sbf.append(p).append("String SCHEMA = ").append(Util.escape(this.schemaName));
		sbf.append(p).append("boolean[] OPS = {");
		boolean firstOne = true;
		for (final boolean b : this.dbOperations) {
			if (firstOne) {
				firstOne = false;
			} else {
				sbf.append(", ");
			}
			sbf.append(b);
		}
		sbf.append("};");

		/*
		 * constructor
		 */
		sbf.append("\n\t/**\n * constructor\n */\npublic ").append(cls).append(" {");
		sbf.append("\n\t\tthis.name = NAME;");
		sbf.append("\n\t\tthis.schema = ComponentProvider.getProvider().getSchema(SCHEMA);");
		sbf.append("\n\t\tthis.operations = OPS;");

		sbf.append("\n\t}\n}\n");
	}

	/**
	 * @param sbf
	 * @param typesMap
	 * @param lists
	 * @param keyedLists
	 * @param tsImportPrefix
	 */
	public void emitTs(final StringBuilder sbf, final Map<String, DataTypes> typesMap,
			final Map<String, ValueList> lists, final Map<String, KeyedList> keyedLists, final String tsImportPrefix) {
		// TODO Auto-generated method stub

	}

}
