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

package org.simplity.fm.upload;

import java.util.Map;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.form.Field;
import org.simplity.fm.core.form.Form;
import org.simplity.fm.core.form.FormData;
import org.simplity.fm.core.service.IServiceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * provides mapping of form fields with columns in the input row
 * 
 * @author simplity.org
 *
 */
public class FormMapper {
	
	private static final Logger logger = LoggerFactory.getLogger(FormMapper.class);
	
	static FormMapper parseMapper(JsonObject json, Uploader loader, IServiceContext ctx) {
		FormMapper mapper = new FormMapper();
		mapper.loader = loader;
		if(mapper.parse(json, ctx)) {
			return mapper;
		}
		return null;
	}
	
	ComponentProvider compProvider = ComponentProvider.getProvider();
	//parent loader. Used for validations
	Uploader loader;
	Form form;
	String generatedKeyOutputName;
	IValueProvider[] valueProviders;

	private FormMapper() {
		//
	}


	private boolean parse(JsonObject json, IServiceContext ctx) {
		
		JsonElement ele = json.get(Conventions.Upload.TAG_FORM);
		if(ele == null || !ele.isJsonPrimitive()) {
			logger.error("{} is required for an form mapper", Conventions.Upload.TAG_FORM);
			return false;
		}
		
		String text = ele.getAsString();
		this.form = this.compProvider.getForm(text);
		if(this.form == null) {
			logger.error("{} is not a valid form name", text);
			return false;
		}
		
		ele = json.get(Conventions.Upload.TAG_GENERATED_KEY);
		if(ele != null) {
			if(ele.isJsonPrimitive()) {
				this.generatedKeyOutputName = ele.getAsString();
			}else {
				logger.error("{} has an invalid value", Conventions.Upload.TAG_GENERATED_KEY);
				return false;
			}
		}
		
		ele = json.get(Conventions.Upload.TAG_FIELDS);
		if(ele == null || !ele.isJsonObject()) {
			logger.error("form mapper has {} attribute missing or invalid", Conventions.Upload.TAG_FIELDS);
			return false;
		}

		return this.parseFields((JsonObject)ele, ctx);
	}

	private boolean parseFields(JsonObject json, IServiceContext ctx) {
		
		this.valueProviders = new IValueProvider[this.form.getNbrFields()];
		for(Map.Entry<String, JsonElement> entry : json.entrySet()) {
			
			String fieldName = entry.getKey();
			Field field = this.form.getField(fieldName);
			
			if(field == null) {
				logger.error("{} is not a valid field name in the form {}", fieldName, this.form.getFormId());
				return false;
			}
			
			JsonElement ele = entry.getValue();
			if(ele.isJsonPrimitive() == false) {
				logger.error("Field {} in the form {} has an invalid value", fieldName, this.form.getFormId());
				return false;
			}
			String val = ele.getAsString();
			char c = ' ';
			if(!val.isEmpty()) {
				c = val.charAt(0);
			}
		}
		return true;
	}


	private IValueProvider parseVp(String value, IServiceContext ctx, boolean fnOk) {
		if(value.isEmpty()) {
			return new ValueProvider(null, "");
		}
		
		char c = value.charAt(0);
		String text = value.substring(1);
		if(c == Conventions.Upload.TYPE_VAR) {
			return new ValueProvider(text, null);
		}
		
		if(c == Conventions.Upload.TYPE_CONST) {
			return new ValueProvider(null, text);
		}
		
		if(c == Conventions.Upload.TYPE_PARAM) {
			String t = this.loader.params.get(text);
			if(t == null) {
				logger.error("{} is used as a paremeter for a field, but it is not defined as a parameter in the parameters list", text);
				return null;
			}
			return new ValueProvider(null, t);
		}
	
		boolean isFn = true;
		if(c == Conventions.Upload.TYPE_LOOKUP) {
			isFn = false;
		}else if(c != Conventions.Upload.TYPE_FN) {
			logger.error("{} is an invalid value. It is starting with an invalid char '{}'", value, c);
			return null;
		}
		if(!fnOk) {
			logger.error("{} is an invalid paramater. Parameter of a function can not be function again.", value);
			return null;
		}
		
		String fn = parseName(text);
		IValueProvider[] pars = parseParams(text);
		if(fn == null || pars == null || pars.length == 0) {
			return null;
		}
		
		if(isFn) {
			logger.error("Sorry, we are not reay yet with functions. Unable to process value ", value);
			return null;
		}

		if(this.loader.valueLists.containsKey(fn) == false) {
			logger.error("{} is not a valid lookup name", fn);
			return null;
		}
		int nbr = pars.length;
		if(nbr > 2) {
			logger.error("look up {} is used with more than 2 parameters", fn);
			return null;
		}
		if(nbr == 2 && this.loader.listsRequiringKey.containsKey(fn) == false) {
			logger.error("look up {} is used with two parameters, but it is not a keyed-list", fn);
			return null;
		}
		
		return null;
	}
	
	private static String parseName(String text) {
		int idx = text.indexOf('(');
		if(idx == -1) {
			invalidFn(text);
			return null;
		}
		
		return text.substring(0,idx).trim();
	}

	private static void invalidFn(String fn) {
		logger.error("{} is not a valid function/lookup. expect text of the form fname(p1,p2,..)", fn);
	}

	private static IValueProvider[] parseParams(String text) {
		//look for (
		int idx = text.indexOf('(');
		if(idx == -1) {
			invalidFn(text);
			return null;
		}

		//there has to be something more
		idx++;
		if(text.length() == idx) {
			invalidFn(text);
			return null;
		}
		
		// find ) after at least one char.
		String s = text.substring(idx).trim();
		idx = s.length() -1;
		if(idx <= 0 || s.charAt(idx) != ')') {
			invalidFn(text);
			return null;
		}
		
		String[] arr =  s.substring(0, idx).split(",");
		for(int i = 0; i < arr.length; i++) {
			arr[i] = arr[i].trim();
		}
		 return  null;
	}
	/**
	 * @param values
	 * @param lookupLists 
	 * @param ctx
	 *            that must have user and tenantKey if the insert operation
	 *            require these
	 * @return loaded form data. null in case of any error in loading. Actual
	 *         error messages are put into the context
	 */
	public FormData loadData(Map<String, String> values, Map<String, Map<String, String>>lookupLists, IServiceContext ctx) {
		String[] data = new String[this.valueProviders.length];
		int idx = 0;
		for (IValueProvider vp : this.valueProviders) {
			if (vp != null) {
				data[idx] = vp.getValue(values, lookupLists);
			}
			idx++;
		}
		
		FormData fd = this.form.newFormData();
		ctx.resetMessages();
		fd.validateAndLoadForInsert(data, ctx);
		if (ctx.allOk()) {
			return fd;
		}

		return null;
	}

	/**
	 * @return the generatedKeyOutputName
	 */
	public String getGeneratedKeyOutputName() {
		return this.generatedKeyOutputName;
	}
}
