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

import org.simplity.fm.core.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * attributes read from application.json are output as generated sources
 *
 * @author simplity.org
 *
 */
public class Application {
	protected static final String ERROR = "ERROR";
	protected static final Logger logger = LoggerFactory.getLogger(Application.class);

	String name;
	String tenantFieldName;
	String tenantDbName;
	DataTypes dataTypes = new DataTypes();
	Map<String, ValueList> valueLists;
	Map<String, KeyedList> keyedLists;
	Map<String, RuntimeList> runtimeLists;

	void fromJson(final JsonObject json) {
		this.name = JsonUtil.getStringMember(json, "name");
		this.tenantFieldName = JsonUtil.getStringMember(json, "tenantFieldName");
		this.tenantDbName = JsonUtil.getStringMember(json, "tenantDbName");
		if (this.name == null) {
			logger.error("name is required");
			this.name = ERROR;
		}

		if (this.tenantFieldName == null) {
			logger.info("No tenant field for this project");
		} else {
			if (this.tenantDbName == null) {
				logger.error("tenantDbName is required when dbFieldName is specified");
				this.tenantDbName = ERROR;
			}
		}

		final JsonObject ele = json.getAsJsonObject("dataTypes");
		if (ele == null) {
			logger.error("No data types defined for the application");
		} else {
			this.dataTypes.fromJson(json);
		}

		this.valueLists = JsonUtil.fromJson(json, "valueLists", ValueList.class, "name");
		this.keyedLists = JsonUtil.fromJson(json, "keyedLists", KeyedList.class, "name");
		this.runtimeLists = JsonUtil.fromJson(json, "runtimeLists", RuntimeList.class, "name");
	}

	void emitJava(final String rootFolder, final String packageName, final String dataTypesFileName) {
		/*
		 * create DataTypes.java in the root folder.
		 */
		final StringBuilder sbf = new StringBuilder();
		this.dataTypes.emitJava(rootFolder, packageName, dataTypesFileName);
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
		if (this.valueLists == null || this.valueLists.size() == 0) {
			logger.warn("No value lists created for this project");
			return;
		}
		final StringBuilder sbf = new StringBuilder();
		for (final ValueList list : this.valueLists.values()) {
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
