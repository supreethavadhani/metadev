package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.DepartmentDataTable;
/** class for form data table department  */
public class DepartmentDataTable extends FormDataTable {
	protected DepartmentDataTable(final Department form, final DepartmentDataTable dataTable, final Object[][] values) {
		super(form, dataTable, values);
	}
}
