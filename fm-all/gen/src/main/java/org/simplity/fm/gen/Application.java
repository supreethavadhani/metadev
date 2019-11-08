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

import org.simplity.fm.core.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * attributes read from application.json
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
}
