package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.UserDataTable;
/** class for form data table user  */
public class UserDataTable extends FormDataTable {
	protected UserDataTable(final User form, final UserDataTable dataTable, final Object[][] values) {
		super(form, dataTable, values);
	}
}
