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

package org.simplity.fm.core;

import java.util.HashMap;
import java.util.Map;

import org.simplity.fm.core.form.Form;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * static class that can locate a design-time
 * 
 * @author simplity.org
 *
 */
public final class Forms {
	private static final Logger logger = LoggerFactory.getLogger(Forms.class);
	private static final Map<String, Form> allForms = new HashMap<>();

	/**
	 * 
	 * @param formName
	 * @return form , or null if there is such form
	 */
	public static Form getForm(String formName) {
		Form form = allForms.get(formName);
		if (form != null) {
			return form;
		}
		try {
			String cls = Config.getConfig().getGeneratedPackageName() + ".form."
					+ formName.substring(0, 1).toUpperCase() + formName.substring(1);
			form = (Form) Class.forName(cls).newInstance();
			allForms.put(formName, form);
			return form;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Form {} could not be located and used as a class for form. Error {}", formName, e.getMessage());
			return null;
		}
	}
}
