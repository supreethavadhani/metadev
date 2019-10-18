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
		if (mapper.parse(json, ctx)) {
			return mapper;
		}
		return null;
	}

	ComponentProvider compProvider = ComponentProvider.getProvider();
	// parent loader. Used for validations
	Uploader loader;
	Form form;
	String generatedKeyOutputName;
	IValueProvider[] valueProviders;

	private FormMapper() {
		//
	}

	private boolean parse(JsonObject json, IServiceContext ctx) {

		JsonElement ele = json.get(Conventions.Upload.TAG_FORM);
		if (ele == null || !ele.isJsonPrimitive()) {
			logger.error("{} is required for an form mapper", Conventions.Upload.TAG_FORM);
			return false;
		}

		String text = ele.getAsString();
		this.form = this.compProvider.getForm(text);
		if (this.form == null) {
			logger.error("{} is not a valid form name", text);
			return false;
		}

		ele = json.get(Conventions.Upload.TAG_GENERATED_KEY);
		if (ele != null) {
			if (ele.isJsonPrimitive()) {
				this.generatedKeyOutputName = ele.getAsString();
			} else {
				logger.error("{} has an invalid value", Conventions.Upload.TAG_GENERATED_KEY);
				return false;
			}
		}

		ele = json.get(Conventions.Upload.TAG_FIELDS);
		if (ele == null || !ele.isJsonObject()) {
			logger.error("form mapper has {} attribute missing or invalid", Conventions.Upload.TAG_FIELDS);
			return false;
		}

		return this.parseFields((JsonObject) ele, ctx);
	}

	private boolean parseFields(JsonObject json, IServiceContext ctx) {

		this.valueProviders = new IValueProvider[this.form.getNbrFields()];
		for (Map.Entry<String, JsonElement> entry : json.entrySet()) {

			String fieldName = entry.getKey();
			Field field = this.form.getField(fieldName);

			if (field == null) {
				logger.error("{} is not a valid field name in the form {}", fieldName, this.form.getFormId());
				return false;
			}

			JsonElement ele = entry.getValue();
			if (ele.isJsonPrimitive() == false) {
				logger.error("Field {} in the form {} has an invalid value", fieldName, this.form.getFormId());
				return false;
			}
			IValueProvider vp = this.parseVp(ele.getAsString(), ctx, true);
			if(vp == null) {
				return false;
			}
			this.valueProviders[field.getIndex()] = vp;
		}
		return true;
	}

	private IValueProvider parseVp(String value, IServiceContext ctx, boolean fnOk) {
		if (value.isEmpty()) {
			return new ValueProvider(null, "");
		}

		char c = value.charAt(0);
		String text = value.substring(1);
		if (c == Conventions.Upload.TYPE_VAR) {
			return new ValueProvider(text, null);
		}

		if (c == Conventions.Upload.TYPE_CONST) {
			return new ValueProvider(null, text);
		}

		if (c == Conventions.Upload.TYPE_PARAM) {
			String t = this.loader.params.get(text);
			if (t == null) {
				logger.error(
						"{} is used as a paremeter for a field, but it is not defined as a parameter in the parameters list",
						text);
				return null;
			}
			return new ValueProvider(null, t);
		}

		boolean isFn = true;
		if (c == Conventions.Upload.TYPE_LOOKUP) {
			isFn = false;
		} else if (c != Conventions.Upload.TYPE_FN) {
			logger.error("{} is an invalid value. It is starting with an invalid char '{}'", value, c);
			return null;
		}
		if (!fnOk) {
			logger.error("{} is an invalid paramater. Parameter of a function can not be function again.", value);
			return null;
		}

		String fn = parseName(text);
		IValueProvider[] vps = this.parseParams(text, ctx);
		if (fn == null || vps == null || vps.length == 0) {
			return null;
		}

		if (isFn) {
			logger.error("Sorry, we are not ready yet with functions. Unable to process value ", value);
			return null;
		}

		Map<String, String> valueList = this.loader.valueLists.get(fn);
		if (valueList == null) {
			logger.error("{} is not a valid lookup name", fn);
			return null;
		}

		int nbr = vps.length;
		IValueProvider vp1 = vps[0];
		IValueProvider vp2 = null;
		boolean isKeyed = this.loader.listsRequiringKey.contains(fn);
		if (isKeyed) {
			if (nbr == 2) {
				vp2 = vps[1];
			} else {
				logger.error("look up {} is used with {} params. It should use 2 parameters as it is a keyed-list", fn,
						nbr);
				return null;
			}
		} else if (nbr > 1) {
			logger.error("look up {} is used with {} params. It should use only one param as it is not a keyd list", fn,
					nbr);
			return null;
		}

		return new LookupValueProvider(valueList, vp1, vp2);
	}

	/*
	 * text is possibly of the form " abcd ( a, b, c)  " we have to return
	 * "abcd"
	 */
	private static String parseName(String text) {
		int idx = text.indexOf('(');
		if (idx == -1) {
			logger.error("'(' not found for aa fn/lookup");
			invalidFn(text);
			return null;
		}

		return text.substring(0, idx).trim();
	}

	private static void invalidFn(String fn) {
		logger.error("{} is not a valid function/lookup. expect text of the form fname(p1,p2,..)", fn);
	}

	/*
	 * text is possibly of the form " abcd ( =a, $b, #c)  " we have to return
	 * array of three IvalueProviders
	 */
	private IValueProvider[] parseParams(String text, IServiceContext ctx) {

		String s = text.trim(); // s="abcd ( =a, $b, #c)"
		// look for (
		int idx = text.indexOf('(');
		if (idx == -1) {
			logger.error("'(' not found for fn/lookup ");
			invalidFn(text);
			return null;
		}

		s = s.substring(idx);// s="( =a, $b, #c)"
		idx = s.indexOf(')');
		if (idx == -1) {
			logger.error("')' not found for fn/lookup ");
			invalidFn(text);
			return null;
		}

		if (s.length() != (idx + 1)) {
			logger.error("not ending with ')'");
			invalidFn(text);
			return null;
		}

		s = s.substring(1, idx).trim(); // s="=a, $b, #c"

		if (s.isEmpty()) {
			logger.error("Fb/lookup has no parameters. We expect at least one parameter");
			invalidFn(text);
			return null;
		}

		String[] arr = s.split(",");
		IValueProvider[] result = new IValueProvider[arr.length];
		for (int i = 0; i < arr.length; i++) {
			IValueProvider vp = this.parseVp(arr[i].trim(), ctx, false);
			if (vp == null) {
				return null;
			}
			result[i] = vp;
		}
		return result;
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
	public FormData loadData(Map<String, String> values, Map<String, Map<String, String>> lookupLists,
			IServiceContext ctx) {
		String[] data = new String[this.valueProviders.length];
		int idx = 0;
		for (IValueProvider vp : this.valueProviders) {
			if (vp != null) {
				data[idx] = vp.getValue(values);
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
