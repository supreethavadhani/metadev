package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.UserData;
/** class for form data user  */
public class UserFd extends FormData {
	protected UserFd(final User form, final UserData dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}
}
