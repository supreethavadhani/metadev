package org.simplity.fm.example.gen.form;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.LinkMetaData;
import org.simplity.fm.core.data.LinkedForm;
import org.simplity.fm.example.gen.rec.SubjectSectionDetailRecord;
/** class for form marksEntry  */
public class MarksEntryForm extends Form<SubjectSectionDetailRecord> {
	protected static final String NAME = "marksEntry";
	protected static final SubjectSectionDetailRecord RECORD = (SubjectSectionDetailRecord) ComponentProvider.getProvider().getRecord("subjectSectionDetail");
	protected static final  boolean[] OPS = {true, false, true, false, false};
	private static final LinkMetaData L0 = new LinkMetaData("students", "marksForAssessment", 1, 0, null,null ,null, true);
	private static final Form<?> F0 = ComponentProvider.getProvider().getForm("marksForAssessment");
	private static final LinkedForm<?>[] LINKS = {new LinkedForm(L0, F0)};
/** constructor */
public MarksEntryForm() {
		super(NAME, RECORD, OPS, LINKS);
	}
}
