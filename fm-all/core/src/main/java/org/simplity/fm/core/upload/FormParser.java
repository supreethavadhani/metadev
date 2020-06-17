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

package org.simplity.fm.core.upload;

import java.util.Map;
import java.util.Set;

import org.simplity.fm.core.Conventions;
import org.simplity.fm.core.app.App;
import org.simplity.fm.core.conf.ICompProvider;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.Record;
import org.simplity.fm.core.fn.IFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * provides mapping of form fields with columns in the input row
 * rules for parsing a value
 * <li>$A input from row to be uploaded. In case of spreadsheet, this is the
 * column name like $a, $b etc.. If the input is anything else, then this
 * corresponds to the filed name in the input
 * insensitive</li>
 * <li>@variable this is a variable/parameter. Value for this parameter is
 * provided in the uploaded header, or is made available at run time. This may
 * also be a generated key name that is output by any of the form</li>
 *
 * <li>#lookupName(name) look-up this value in the named list. The list is not
 * keyed. name can be constant, variable or lookup.
 * can not be function or lookup</li>
 * <li>#lookupName(keyName, name) same as above, except that the look up list is
 * keyed, and hence has two parameters</li>
 * <li>%functionName(param1, param2...) evaluate the function with these values.
 * Note that the params can not be a function/lookup. Should be constant,
 * variable or input.</li>
 * <li>=constant when constant starts with any of these reserved characters.
 * e.g. =$ to set '$' or == to set '='
 * </li>
 *
 * @author simplity.org
 *
 */
class FormParser {
	private static final Logger logger = LoggerFactory.getLogger(FormParser.class);

	private final ICompProvider compProvider = App.getApp().getCompProvider();
	/*
	 * what is defined in this processor
	 */
	private final Map<String, String> params;
	private final Map<String, Map<String, String>> valueLists;
	private final Set<String> listsRequiringKey;
	private final Map<String, IFunction> functions;

	/*
	 * we are going to parse these
	 */
	private DbRecord record;
	private String generatedKeyOutputName;
	private IValueProvider[] valueProviders;

	FormParser(final Map<String, String> params, final Map<String, Map<String, String>> valueLists,
			final Set<String> listsRequiringKey, final Map<String, IFunction> functions) {
		this.params = params;
		this.valueLists = valueLists;
		this.listsRequiringKey = listsRequiringKey;
		this.functions = functions;
	}

	protected FormLoader parse(final JsonObject json) {

		JsonElement ele = json.get(Conventions.Upload.TAG_FORM);
		if (ele == null || !ele.isJsonPrimitive()) {
			logger.error("{} is required for an form mapper", Conventions.Upload.TAG_FORM);
			return null;
		}

		final String text = ele.getAsString().trim();
		final Record rec = this.compProvider.getRecord(text);
		if (rec == null) {
			logger.error("{} is not a valid record name", text);
			return null;
		}

		if (rec instanceof DbRecord == false) {
			logger.error("{} is a Record, but not a DbRecord", text);
			return null;
		}

		this.record = (DbRecord) rec;
		ele = json.get(Conventions.Upload.TAG_GENERATED_KEY);
		if (ele != null) {
			if (ele.isJsonPrimitive()) {
				this.generatedKeyOutputName = ele.getAsString().trim();
			} else {
				logger.error("{} has an invalid value", Conventions.Upload.TAG_GENERATED_KEY);
				return null;
			}
		}

		ele = json.get(Conventions.Upload.TAG_FIELDS);
		if (ele == null || !ele.isJsonObject()) {
			logger.error("form mapper has {} attribute missing or invalid", Conventions.Upload.TAG_FIELDS);
			return null;
		}

		if (!this.parseFields((JsonObject) ele)) {
			return null;
		}

		return new FormLoader(this.record, this.generatedKeyOutputName, this.valueProviders);
	}

	private boolean parseFields(final JsonObject json) {
		this.valueProviders = new IValueProvider[this.record.length()];
		for (final Map.Entry<String, JsonElement> entry : json.entrySet()) {

			final String fieldName = entry.getKey();
			final Field field = this.record.fetchField(fieldName);

			if (field == null) {
				logger.error("{} is not a valid field name in the form {}", fieldName, this.record.fetchName());
				return false;
			}

			final JsonElement ele = entry.getValue();
			if (!ele.isJsonPrimitive()) {
				logger.error("Field {} in the form {} has an invalid value", fieldName, this.record.fetchName());
				return false;
			}

			final IValueProvider vp = this.parseVp(ele.getAsString().trim(), true);
			if (vp == null) {
				return false;
			}
			this.valueProviders[field.getIndex()] = vp;
		}
		return true;
	}

	private IValueProvider parseVp(final String value, final boolean fnOk) {
		if (value == null || value.isEmpty()) {
			return new ValueProvider(null, "");
		}

		final char c = value.charAt(0);
		final String text = value.substring(1);

		if (c == Conventions.Upload.TYPE_VAR) {
			return new ValueProvider(text, null);
		}

		if (c == Conventions.Upload.TYPE_CONST) {
			return new ValueProvider(null, text);
		}

		if (c == Conventions.Upload.TYPE_PARAM) {
			final String t = this.params.get(text);
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
			/*
			 * first char is not a special character. so the whole input is
			 * constant
			 */
			return new ValueProvider(null, value);
		}
		/*
		 * we are left with fn and lookup both use parameters. both are of the
		 * form name(p1, p2..)
		 */
		if (!fnOk) {
			logger.error("{} is an invalid paramater. Parameter of a function/lookup can not be function/lookup again.",
					value);
			return null;
		}

		final String nam = parseName(text);
		final IValueProvider[] vps = this.parseParams(text);
		if (nam == null || vps == null || vps.length == 0) {
			return null;
		}

		if (isFn) {
			final IFunction fn = this.functions.get(nam);
			if (fn == null) {
				logger.error("{} is not declared as a function", nam);
				return null;
			}
			return new FunctionValueProvider(fn, vps);
		}

		final Map<String, String> valueList = this.valueLists.get(nam);
		if (valueList == null) {
			logger.error("{} is not a valid lookup name", nam);
			return null;
		}

		final int nbr = vps.length;
		final IValueProvider vp1 = vps[0];
		IValueProvider vp2 = null;
		final boolean isKeyed = this.listsRequiringKey.contains(nam);
		if (isKeyed) {
			if (nbr == 2) {
				vp2 = vps[1];
			} else {
				logger.error("look up {} is used with {} params. It should use 2 parameters as it is a keyed-list", nam,
						nbr);
				return null;
			}
		} else if (nbr > 1) {
			logger.error("look up {} is used with {} params. It should use only one param as it is not a keyd list",
					nam, nbr);
			return null;
		}

		return new LookupValueProvider(valueList, vp1, vp2);
	}

	/*
	 * text is possibly of the form " abcd ( a, b, c)  " we have to return
	 * "abcd"
	 */
	private static String parseName(final String text) {
		final int idx = text.indexOf('(');
		if (idx == -1) {
			logger.error("'(' not found for a fn/lookup");
			invalidFn(text);
			return null;
		}

		return text.substring(0, idx).trim();
	}

	private static void invalidFn(final String fn) {
		logger.error("{} is not a valid function/lookup. expect text of the form fname(p1,p2,..)", fn);
	}

	/*
	 * text is possibly of the form " abcd ( =a, $b, #c)  " we have to return
	 * array of three IvalueProviders
	 */
	private IValueProvider[] parseParams(final String text) {
		// look for (
		int idx = text.indexOf('(');
		if (idx == -1) {
			logger.error("'(' not found for fn/lookup ");
			invalidFn(text);
			return null;
		}

		String s = text.substring(idx);// s="( =a, $b, #c)"
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
			logger.error("Fn/lookup has no parameters. We expect at least one parameter");
			invalidFn(text);
			return null;
		}

		final String[] arr = s.split(",");
		final IValueProvider[] result = new IValueProvider[arr.length];
		for (int i = 0; i < arr.length; i++) {
			final IValueProvider vp = this.parseVp(arr[i].trim(), false);
			if (vp == null) {
				return null;
			}
			result[i] = vp;
		}
		return result;
	}
}
