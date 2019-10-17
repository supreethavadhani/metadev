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

import java.util.Map;

import org.simplity.fm.core.form.Form;
import org.simplity.fm.core.form.FormData;
import org.simplity.fm.core.service.IserviceContext;

/**
 * provides mapping of form fields with columns in the input row
 * 
 * @author simplity.org
 *
 */
public class FormMapper {
	private final Form form;
	private final String generatedKeyOutputName;
	private final IValueProvider[] valueProviders;

	protected FormMapper(Form form, String generatedKeyOutputName, IValueProvider[] valueProviders) {
		this.form = form;
		this.generatedKeyOutputName = generatedKeyOutputName;
		this.valueProviders = valueProviders;
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
	public FormData loadData(Map<String, String> values, Map<String, Map<String, String>>lookupLists, IserviceContext ctx) {
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
