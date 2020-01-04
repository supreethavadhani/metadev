package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.GuardianData;
/** class for form data guardian  */
public class GuardianFd extends FormData {
	protected GuardianFd(final Guardian form, final GuardianData dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}
}
