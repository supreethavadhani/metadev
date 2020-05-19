package org.simplity.fm.example.gen.form;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.LinkMetaData;
import org.simplity.fm.core.data.LinkedForm;
import org.simplity.fm.example.gen.rec.StudentDetailRecord;
/** class for form studentDetail  */
public class StudentDetailForm extends Form<StudentDetailRecord> {
	protected static final String NAME = "studentDetail";
	protected static final StudentDetailRecord RECORD = (StudentDetailRecord) ComponentProvider.getProvider().getRecord("studentDetail");
	protected static final  boolean[] OPS = {true, true, true, false, true, false};
	private static final LinkedForm<?>[] LINKS = null;
/** constructor */
public StudentDetailForm() {
		super(NAME, RECORD, OPS, LINKS);
	}
}
