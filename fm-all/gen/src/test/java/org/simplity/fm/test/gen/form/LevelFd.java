package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.test.gen.schema.LevelData;
/** class for form data level  */
public class LevelFd extends FormData {
	protected LevelFd(final Level form, final LevelData dataObject, final Object[] values, final FormDataTable[] data) {
		super(form, dataObject, values, data);
	}
}
