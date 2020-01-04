package org.simplity.fm.test.gen.form;

import org.simplity.fm.core.ComponentProvider;
import org.simplity.fm.core.data.Form;
import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.LinkedForm;
import org.simplity.fm.core.data.FormDataTable;
import org.simplity.fm.core.data.FormData;
import org.simplity.fm.core.data.SchemaData;
import org.simplity.fm.core.data.SchemaDataTable;
import org.simplity.fm.test.gen.DefinedDataTypes;
import org.simplity.fm.test.gen.schema.StudentData;
import org.simplity.fm.test.gen.schema.StudentDataTable;
/** class for form studentWithGuardians  */
public class StudentWithGuardians extends Form {
	protected static final String NAME = "studentWithGuardians";
	protected static final String SCHEMA = "student";
	protected static final  Field[] FIELDS = {
			new Field("levelId", 0, DefinedDataTypes.id, null, null, null, true), 
			new Field("sectionId", 0, DefinedDataTypes.id, null, null, "sectionList", true)};
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
	protected static final LinkedForm[] LINKED_FORMS = {
			new LinkedForm("guardians", "guardian", 1, 10, null, new String[]{"studentId"}, new String[]{"studentId"}, true)};
/** constructor */
public StudentWithGuardians() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
		this.linkedForms = LINKED_FORMS;
		this.initialize();
	}

	@Override
	protected StudentWithGuardiansFd newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new StudentWithGuardiansFd(this, (StudentData) schemaData, values, data);
	}

	@Override
	protected StudentWithGuardiansFdt newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new StudentWithGuardiansFdt(this, (StudentDataTable) table, values);
	}
}
