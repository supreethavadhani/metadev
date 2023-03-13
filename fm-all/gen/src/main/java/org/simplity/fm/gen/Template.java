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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author simplity.org
 *
 */
public class Template {
	protected static final Logger logger = LoggerFactory.getLogger(Template.class);

	String templateName;
	String templateType;
	String htmlSelector;
	Button[] buttons;

	/**
	 * @param sbf
	 * @param typesMap
	 * @param lists
	 * @param keyedLists
	 * @param tsImportPrefix
	 */
	void emitTemplateTs(StringBuilder sbf, Template template, final String templateRoot, final String fn,
			final String tsRootFolder) {
		if (template.templateType.equalsIgnoreCase("form")) {
			sbf = getFormTs(template, sbf, tsRootFolder);
		}
		if (template.templateType.equalsIgnoreCase("table")) {
			sbf = getTableTs(template, sbf, tsRootFolder);
		}
		Util.writeOut(templateRoot + "/" + fn + "/" + "component.ts", sbf);

	}

	void emitTemplateHtml(StringBuilder sbf, Template template, final String templateRoot, final String fn) {
		if (template.templateType.equalsIgnoreCase("form")) {
			sbf = getFormHtml(template, sbf);
		}
		if (template.templateType.equalsIgnoreCase("table")) {
			sbf = getTableHtml(template, sbf);
		}
		Util.writeOut(templateRoot + "/" + fn + "/" + "component.html", sbf);
	}

	/**
	 * 
	 * @param template
	 * @param sbf
	 * @return
	 */
	StringBuilder getFormTs(Template template, StringBuilder sbf, String tsRootFolder) {
		sbf.append("import {\n" + "  Component,\n" + "  Input,\n" + "  OnInit,\n" + "} from '@angular/core';\n"
				+ "import { Router } from '@angular/router';\n" + "import {\n" + "  FormData,\n"
				+ "  MVClientCoreAppModule,\n" + "  MVComponentsModule,\n" + "  ServiceAgent,\n" + "  FormService\n"
				+ "} from 'mv-core';\n" + "import { allForms } from \"" + tsRootFolder + "allForms\"\n\n"
				+ "@Component({\n" + "  standalone: true,\n" + "  selector: '" + template.htmlSelector + "',\n"
				+ "  templateUrl: './component.html',\n" + "  imports: [MVClientCoreAppModule, MVComponentsModule],\n"
				+ "  exportAs: \"" + template.templateName + "Component\"\n" + "})\n" + "\n" + "export class "
				+ template.templateName + "Component implements OnInit {\n" + "  @Input() formName: any;\n"
				+ "  public fd: FormData;\n" + "  public formHeader:string;\n"
				+ "  constructor(public sa: ServiceAgent) {}\n" + "  ngOnInit() {\n"
				+ "    this.fd = FormService.getFormFd(this.formName,this.sa,allForms)\n"
				+ "    this.formHeader = this.fd.form.getName();\n" + "  }");
		getButtonsTs(template, sbf);
		sbf.append("\n}");
		return sbf;
	}

	StringBuilder getFormHtml(Template template, StringBuilder sbf) {
		BuiltInTags tags = new BuiltInTags();
		String tag = tags.getValue(template.templateType);
		sbf = getHeaderHtml(template, sbf);
		sbf.append("<div  class =\"col-md-8\" style=\"margin: auto; padding-top:3rem\"> \n	<" + tag
				+ " [formData]=\"fd\"></" + tag + ">\n</div>");
		sbf = getButtonsHtml(template, sbf);
		return sbf;
	}

	StringBuilder getTableTs(Template template, StringBuilder sbf, String tsRootFolder) {
		sbf.append("import {\n" + "  AfterViewInit,\n" + "    Component, Input, OnInit, ViewChild,\n"
				+ "  } from '@angular/core';\n" + "import { Router } from '@angular/router';\n" + "import { \n"
				+ "    FilterRequest, FormData, MvTableComponent, ServiceAgent, TableMetaData, MVClientCoreAppModule, MVComponentsModule, FormService \n"
				+ "  } from 'mv-core';\n" + "import { allForms } from \"" + tsRootFolder + "allForms\";\n"
				+ "  @Component({\n" + "    standalone: true,\n" + "    selector: '" + template.htmlSelector + "',\n"
				+ "    templateUrl: './component.html',\n" + "    imports:[MVClientCoreAppModule,MVComponentsModule],\n"
				+ "    exportAs:\"" + template.templateName + "Component\",\n" + "    styleUrls: []\n" + "  })\n"
				+ "  \n" + "  export class " + template.templateName + "Component implements OnInit{\n"
				+ "    @ViewChild(\"gridTable\", { static: false }) gtable: MvTableComponent;\n"
				+ "    @Input() formName: any;\n" + "\n" + "    public fd: FormData\n"
				+ "    public tableData: TableMetaData;\n" + "public formHeader: string\n" + "\n"
				+ "    constructor(public sa: ServiceAgent) {}\n" + "    \n" + "    async ngOnInit() {\n"
				+ "      this.fd = await FormService.getFormFd(this.formName,this.sa,allForms)\n"
				+ "      this.fetchData();\n     this.formHeader = this.fd.form.getName();\n" + "    }\n"
				+ "    fetchData() {\n" + "  \n" + "      this.tableData = this.gtable.getColumnData(this.fd)\n"
				+ "      const filter: FilterRequest = {\n" + "        conditions: {}\n" + "      };\n" + "      \n"
				+ "     this.fd.filter(filter).subscribe({\n" + "      next: data =>{\n"
				+ "          this.tableData.data = data;\n" + "          this.gtable.update();\n" + "      },\n"
				+ "      error: msg => console.error(\"Error from server \", msg)\n" + "     });\n" + "    }");
		sbf.append("\n}");
		return sbf;
	}

	StringBuilder getTableHtml(Template template, StringBuilder sbf) {
		sbf = getHeaderHtml(template, sbf);
		sbf.append(
				" <app-mv-table style=\"width:1000px;\" data [tableGridData]=\"tableData\" #gridTable></app-mv-table>");
		sbf = getButtonsHtml(template, sbf);
		return sbf;
	}

	StringBuilder getButtonsHtml(Template template, StringBuilder sbf) {
		BuiltInTags tags = new BuiltInTags();
		sbf.append("<div style=\"text-align: center;padding-top: 2rem;margin:2rem\">");
		for (Button b : template.buttons) {
			sbf.append("<" + tags.getValue(b.buttonType + "") + " name= \"" + b.name + "\" (click)="
					+ tags.getValue(b.action + "") + "()> </" + tags.getValue(b.buttonType + "") + "> \n");
		}
		sbf.append("</div>");
		return sbf;
	}

	StringBuilder getButtonsTs(Template template, StringBuilder sbf) {
		if (template.buttons.length > 0) {
			for (Button button : template.buttons) {
				if (button.action == ButtonActions.Create) {
					sbf.append(" \n create() {\n" + "    this.fd.saveAsNew().subscribe(\n" + "      data => {\n"
							+ "        console.log(\"saved\")\n" + "      },\n" + "      err => {\n"
							+ "        console.log(err)\n" + "      }\n" + "    )\n" + "  } ");
				}
				if (button.action == ButtonActions.Update) {
					sbf.append("  \n save() {\n" + "    this.fd.save().subscribe(\n" + "      data => {\n"
							+ "        console.log(\"saved\")\n" + "      },\n" + "      err => {\n"
							+ "        console.log(err)\n" + "      }\n" + "    )\n" + "  }");
				}
				if (button.action == ButtonActions.Cancel) {
					sbf.append("   \n cancel() {\n"
							+ "    this.fd = FormService.getFormFd(this.formName,this.sa,allForms)  \n" + "  }");
				}
			}
		}
		return sbf;
	}

	StringBuilder getHeaderHtml(Template template, StringBuilder sbf) {
		sbf.append("<mat-card class =\"col-md-8\" style=\"margin: auto\">\n" + "    <h2> {{formHeader}}</h2>\n"
				+ "</mat-card>");
		return sbf;
	}

}
