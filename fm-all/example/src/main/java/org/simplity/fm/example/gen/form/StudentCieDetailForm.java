package org.simplity.fm.example.gen.form;

import org.simplity.fm.core.app.App;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.LinkMetaData;
import org.simplity.fm.core.data.LinkedForm;
import org.simplity.fm.example.gen.rec.StudentCieDetailRecord;
/** class for form studentCieDetail  */
public class StudentCieDetailForm extends Form<StudentCieDetailRecord> {
	protected static final String NAME = "studentCieDetail";
	protected static final StudentCieDetailRecord RECORD = (StudentCieDetailRecord) App.getApp().getCompProvider().getRecord("studentCieDetail");
	protected static final  boolean[] OPS = {true, true, true, false, true};
	private static final LinkedForm<?>[] LINKS = null;
/** constructor */
public StudentCieDetailForm() {
		super(NAME, RECORD, OPS, LINKS);
	}
}
