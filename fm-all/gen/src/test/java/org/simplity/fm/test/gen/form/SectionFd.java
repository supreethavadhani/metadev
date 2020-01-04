package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.SectionData;
/** class for form data section  */
public class SectionFd extends FormData {
	protected SectionFd(final Section form, final SectionData dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}
}
