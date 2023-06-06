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
	Boolean enableRoutes;
	String editRoute;

	public Boolean hasSaveButton = false;

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
				+ "@Input() routes?: [];\n");
		if (template.buttons.length > 0) {
			for (Button button : template.buttons) {
				if (button.action == ButtonActions.Update) {
					sbf.append("@Input() inputData:{};\n\n");
					this.hasSaveButton = true;
					break;
				}
			}
		}
		sbf.append("  public fd: FormData;\n" + "  public formHeader:string;\n"
				+ "  constructor(public sa: ServiceAgent, public router: Router) {}\n" + "  ngOnInit() {\n"
				+ "    this.fd = FormService.getFormFd(this.formName,this.sa,allForms)\n"
				+ "    this.formHeader = this.fd.form.getName().toUpperCase();\n");
		if (hasSaveButton) {
			sbf.append("    this.fd.setFieldValues(this.inputData);\n" + "    this.fd.fetchData().subscribe(\n"
					+ "      data=>{\n" + "      console.log(data,\"Data Fetched successfully\")\n" + "    },\n"
					+ "    err=>{\n" + "      console.log(err)\n" + "    })");
		}
		sbf.append("  }");
		getButtonsTs(template, sbf);
		sbf.append("\n}");
		return sbf;
	}

	StringBuilder getFormHtml(Template template, StringBuilder sbf) {
		BuiltInTags tags = new BuiltInTags();
		String tag = tags.getValue(template.templateType);
		sbf = getHeaderHtml(template, sbf);
		sbf.append("	<" + tag + " [formData]=\"fd\"></" + tag + ">\n");
		sbf = getButtonsHtml(template, sbf);
		sbf.append("</div>");
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
				+ "    @Input() formName: any;\n" + "\n" + "\n  @Input() routes?: [];\n@Input() editRoute: string\n"
				+ "public fd: FormData\n" + "    public tableData: TableMetaData;\n" + "public formHeader: string\n"
				+ "\n" + "    constructor(public sa: ServiceAgent, public router: Router) {}\n" + "    \n"
				+ "    async ngOnInit() {\n"
				+ "      this.fd = await FormService.getFormFd(this.formName,this.sa,allForms)\n"
				+ "      this.fetchData();\n     this.formHeader = this.fd.form.getName().toUpperCase();\n" + "    }\n"
				+ "    fetchData() {\n" + "  \n" + "      this.tableData = this.gtable.getColumnData(this.fd)\n"
				+ "      const filter: FilterRequest = {\n" + "        conditions: {}\n" + "      };\n" + "      \n"
				+ "     this.fd.filter(filter).subscribe({\n" + "      next: data =>{\n"
				+ "          this.tableData.data = data;\n" + "          this.gtable.update();\n" + "      },\n"
				+ "      error: msg => console.error(\"Error from server \", msg)\n" + "     });\n" + "    }");
		sbf = getButtonsTs(template, sbf);

		sbf.append("  \neditClicked($event) {\n" + "    let primaryKey = Object.keys(this.fd.extractKeyFields())[0]\n"
				+ "    let routeKey = {}\n" + "    routeKey[primaryKey] = this.tableData.data[$event][primaryKey]\n"
				+ "    this.router.navigate([this.editRoute,routeKey])\n" + "  }\n}");
		return sbf;
	}

	StringBuilder getTableHtml(Template template, StringBuilder sbf) {
		sbf = getHeaderHtml(template, sbf);
		sbf.append(
				" <app-mv-table (editAction)=\"editClicked($event)\" style=\"width:1000px;\" data [tableGridData]=\"tableData\" #gridTable></app-mv-table>");
		if (template.buttons != null && template.buttons.length > 0) {
			sbf = getButtonsHtml(template, sbf);
		}
		sbf.append("</div>");
		return sbf;
	}

	StringBuilder getButtonsHtml(Template template, StringBuilder sbf) {
		BuiltInTags tags = new BuiltInTags();
		sbf.append("<div style=\"text-align: center;padding-top: 2rem;\">");
		for (Button b : template.buttons) {
			sbf.append(" \n    <" + tags.getValue(b.buttonType + "") + " name= \"" + b.name + "\" (click)="
					+ tags.getValue(b.action + "") + b.name.replaceAll("\\s+", "") + "()> </"
					+ tags.getValue(b.buttonType + "") + ">");
		}
		sbf.append("\n</div>");
		return sbf;
	}

	StringBuilder getButtonsTs(Template template, StringBuilder sbf) {
		if (template.buttons.length > 0) {
			for (Button button : template.buttons) {
				if (button.action == ButtonActions.Create) {
					sbf.append(" \n create" + button.name.replaceAll("\\s+", "") + "() {\n"
							+ "    this.fd.saveAsNew().subscribe(\n" + "      data => {\n"
							+ "        console.log(\"saved\")\n");
					if (button.routeOnClick != null && button.routeOnClick && template.enableRoutes) {
						sbf = getRouteTs(sbf, button.name);
					}
					sbf.append("\n    },       err => {\n" + "        console.log(err)\n" + "      });   \n" + "    }");
				}
				if (button.action == ButtonActions.Update) {
					sbf.append("  \n save" + button.name.replaceAll("\\s+", "") + "() {\n"
							+ "    this.fd.save().subscribe(\n" + "      data => {\n"
							+ "        console.log(\"saved\")\n");
					if (button.routeOnClick != null && button.routeOnClick && template.enableRoutes) {
						sbf = getRouteTs(sbf, button.name);
					}
					sbf.append("\n    },       err => {\n" + "        console.log(err)\n" + "      });   \n" + "    }");
				}
				if (button.action == ButtonActions.Navigate) {
					sbf.append("   \n navigate" + button.name.replaceAll("\\s+", "") + "() {\n"
							+ "    this.fd = FormService.getFormFd(this.formName,this.sa,allForms)  \n");
					if (button.routeOnClick != null && button.routeOnClick && template.enableRoutes) {
						sbf = getRouteTs(sbf, button.name);
					}
					sbf.append("  }");
				}
			}
		}
		return sbf;
	}

	StringBuilder getHeaderHtml(Template template, StringBuilder sbf) {
		sbf.append("<div class=\"col-md-12\" style=\"margin: auto; margin-top: 3rem;\"> \n" + "     <div>\n"
				+ "       <b><h3 style=\"color: #353b48; font-weight: 600;\"> {{formHeader}}</h3></b>\n"
				+ "       <hr>\n" + "    </div>");
		return sbf;
	}

	StringBuilder getRouteTs(StringBuilder sbf, String name) {
		sbf.append("        this.router.navigate([this.routes.filter(routeTo=> routeTo['name'] == \"" + name
				+ "\" )[0]['routeTo']])");
		return sbf;
	}

}
