package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.Schema1DataTable;
/** class for form data table formWithChildren  */
public class FormWithChildrenDataTable extends FormDataTable {
	protected FormWithChildrenDataTable(final FormWithChildren form, final Schema1DataTable dataTable, final Object[][] values) {
		super(form, dataTable, values);
	}
}
