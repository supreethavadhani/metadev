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
import java.io.FileReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.stream.JsonReader;

/**
 * @author simplity.org
 *
 */
public class Page {
	protected static final Logger logger = LoggerFactory.getLogger(Page.class);

	String pageName;
	String componentForm;
	String templateType;
	String pageSelector;

	StringBuilder emitPageTs(StringBuilder sbf, Page page, String resourceRootFolder) {
		String templateTag = getTemplateTag(page, resourceRootFolder);
		sbf.append("import {\n" + "  Component,\n" + "  Input\n" + "} from '@angular/core';\n" + "\n" + "import { "
				+ page.templateType + "Component } from 'src/app/framework-modules/formdata/template/"
				+ page.templateType + "/component';\n" + "\n" + "@Component({\n" + "  standalone: true,\n"
				+ "  selector:'" + page.pageSelector + "',\n  template: `<" + templateTag + " [formName]= \"form\"></"
				+ templateTag + ">`,\n" + "  imports:[" + page.templateType + "Component],\n" + "  styleUrls: []\n"
				+ "})\n" + "\n" + "export class " + page.pageName + "Component {\n" + "  @Input() inputData: any;\n"
				+ "\n" + "  public form\n" + "  \n" + "  constructor() {\n" + "    this.form = \"" + page.componentForm
				+ "\"\n" + "  }\n" + "}\n" + "");
		return sbf;
	}

	String getTemplateTag(Page Page, String resourceRootFolder) {
		Template template = null;
		File f = new File(resourceRootFolder + "/template/" + Page.templateType + ".template.json");
		if (f.exists() == false) {
			logger.error("page folder {} not found. No Pages are processed", f.getPath());
		} else {
			try (final JsonReader reader = new JsonReader(new FileReader(f))) {
				template = Util.GSON.fromJson(reader, Template.class);
			} catch (final Exception e) {
				e.printStackTrace();
				logger.error("Form {} not generated. Error : {}, {}", e, e.getMessage());
			}
		}
		return template.htmlSelector;
	}

}
