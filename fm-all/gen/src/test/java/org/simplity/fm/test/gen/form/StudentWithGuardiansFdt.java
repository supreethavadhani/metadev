package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.StudentDataTable;
/** class for form data table studentWithGuardians  */
public class StudentWithGuardiansFdt extends FormDataTable {
	protected StudentWithGuardiansFdt(final StudentWithGuardians form, final StudentDataTable dataTable, final Object[][] values) {
		super(form, dataTable, values);
	}
}
