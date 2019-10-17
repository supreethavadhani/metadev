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

package org.simplity.fm.batch;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.JsonUtil;
import org.simplity.fm.core.service.IserviceContext;
import org.simplity.fm.core.validn.RuntimeList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author simplity.org
 *
 */
public class Uploader {
	protected static final Logger logger = LoggerFactory.getLogger(Uploader.class);

	protected String name;
	protected Map<String, String> params = new HashMap<>();
	protected Map<String, Map<String, String>> valueLists = new HashMap<>();
	protected Map<String, Map<String, String>> keyedLists = new HashMap<>();
	protected Map<String, RuntimeList> runtimeLists = new HashMap<>();
	protected Map<String, Function<String[], String>> functions = new HashMap<>();
	protected FormMapper[] inserts;

	private Uploader() {
		//
	}
	/**
	 * parse from json
	 * @param json
	 * @return parser if all ok. null in case of any error. Error messages are just logged, and not returned to the caller
	 */
	private boolean parse(JsonObject json, IserviceContext ctx) {
		
		JsonElement ele = json.get(Conventions.Upload.TAG_NAME);
		if(ele != null && ele.isJsonPrimitive()) {
			this.name = ele.getAsString();
		}else {
			missingTag(Conventions.Upload.TAG_NAME);
			return false;
		}
		
		ele = json.get(Conventions.Upload.TAG_LOOKUPS);
		if(ele != null ) {
			if(!ele.isJsonObject()) {
				missingTag(Conventions.Upload.TAG_LOOKUPS);
				return false;
			}
			if(!this.parseLookups((JsonObject)ele)) {
				return false;
			}
		}
		
		ele = json.get(Conventions.Upload.TAG_FUNCTIONS);
		if(ele != null ) {
			if(!ele.isJsonObject()) {
				missingTag(Conventions.Upload.TAG_FUNCTIONS);
				return false;
			}
			if(!this.parseFunctions((JsonObject)ele)) {
				return false;
			}
		}
		
		ele = json.get(Conventions.Upload.TAG_INSERTS);
		if(ele == null || !ele.isJsonArray()) {
			missingTag(Conventions.Upload.TAG_FUNCTIONS);
			return false;
		}

		for(JsonElement t : ((JsonArray)ele)) {
			if(t == null || t.isJsonObject() == false) {
				logger.error("{} has an invalid or missing form element", Conventions.Upload.TAG_FUNCTIONS);
				return false;
			}

			if(!this.parseInsert((JsonObject)t)) {
				return false;
			}
		}
		
		return true;
		
	}

	private boolean parseInsert(JsonObject ele) {
		// TODO Auto-generated method stub
		return false;
	}
	private boolean parseFunctions(JsonObject ele) {
		// TODO Auto-generated method stub
		return false;
	}
	private boolean parseLookups(JsonObject json) {
		for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
			String attr = entry.getKey();
			JsonElement ele = entry.getValue();
			boolean ok = true;
			if(ele.isJsonPrimitive()) {
				ok = this.parseSystemList(attr, ele.getAsString());
			}else if(ele.isJsonObject()) {
				ok = this.parseLocalList(attr, (JsonObject)ele);
			}else {
				logger.error("Element {} which is a child of {} is invalid", attr, Conventions.Upload.TAG_LOOKUPS);
				return false;
			}
			if(!ok) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * @param attr
	 * @param ele
	 * @return
	 */
	private boolean parseLocalList(String attr, JsonObject ele) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * @param name2
	 * @param asString
	 * @return
	 */
	private boolean parseSystemList(String name2, String asString) {
		// TODO Auto-generated method stub
		return false;
	}
	/**
	 * @param name2
	 * @param asString
	 * @return
	 */
	private boolean parseRuntimeList(String name2, String asString) {
		// TODO Auto-generated method stub
		return false;
	}
	private static void missingTag(String tagName){
		logger.error("Tag/attribute {} is missing or is not valid", tagName);
	}

}
