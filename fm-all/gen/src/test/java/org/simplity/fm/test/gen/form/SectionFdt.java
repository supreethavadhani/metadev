package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.SectionDataTable;
/** class for form data table section  */
public class SectionFdt extends FormDataTable {
	protected SectionFdt(final Section form, final SectionDataTable dataTable, final Object[][] values) {
		super(form, dataTable, values);
	}
}
