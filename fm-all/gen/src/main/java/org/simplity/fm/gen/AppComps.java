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

import java.io.File;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
class AppComps {
	static final Logger logger = LoggerFactory.getLogger(AppComps.class);
	private static final String C = ", ";

	static final Object TENANT_COLUMN = "tenantKeyColumnName";
	static final Object TENANT_FIELD = "tenantKeyFieldName";

	Map<String, DataTypes> dataTypes;
	Map<String, ValueList> lists;
	Map<String, KeyedList> keyedLists;
	Map<String, RuntimeList> runtimeLists;
	Map<String, Object> params;

	void emitJava(final String rootFolder, final String packageName, final String dataTypesFileName) {
		/*
		 * create DataTypes.java in the root folder.
		 */
		final StringBuilder sbf = new StringBuilder();
		// this.emitJavaTypes(sbf, packageName);
		Util.writeOut(rootFolder + dataTypesFileName + ".java", sbf);
		final String pck = packageName + ".list";
		final String fldr = rootFolder + "list/";

		final File dir = new File(fldr);
		if (dir.exists() == false) {
			dir.mkdirs();
		}
		this.emitJavaLists(pck, fldr);
		this.emitJavaKlists(pck, fldr);
		this.emitJavaRlists(pck, fldr);
	}

	void emitJavaLists(final String pack, final String folder) {
		/**
		 * lists are created under list sub-package
		 */
		if (this.lists == null || this.lists.size() == 0) {
			logger.warn("No lists created for this project");
			return;
		}
		final StringBuilder sbf = new StringBuilder();
		for (final ValueList list : this.lists.values()) {
			sbf.setLength(0);
			list.emitJava(sbf, pack);
			Util.writeOut(folder + Util.toClassName(list.name) + ".java", sbf);
		}
	}

	void emitJavaKlists(final String pack, final String folder) {
		/**
		 * keyed lists
		 */
		if (this.keyedLists == null || this.keyedLists.size() == 0) {
			logger.warn("No keyed lists created for this project");
			return;
		}

		final StringBuilder sbf = new StringBuilder();
		for (final KeyedList list : this.keyedLists.values()) {
			sbf.setLength(0);
			list.emitJava(sbf, pack);
			Util.writeOut(folder + Util.toClassName(list.name) + ".java", sbf);
		}
	}

	void emitJavaRlists(final String pack, final String folder) {
		/**
		 * runtime lists
		 */
		if (this.runtimeLists == null || this.runtimeLists.size() == 0) {
			logger.warn("No runtime lists created for this project");
			return;
		}
		final StringBuilder sbf = new StringBuilder();
		for (final RuntimeList list : this.runtimeLists.values()) {
			sbf.setLength(0);
			list.emitJava(sbf, pack);
			Util.writeOut(folder + Util.toClassName(list.name) + ".java", sbf);
		}
	}
}
