package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.StudentData;
/** class for form data studentWithGuardians  */
public class StudentWithGuardiansData extends FormData {
	protected StudentWithGuardiansData(final StudentWithGuardians form, final StudentData dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}
}
