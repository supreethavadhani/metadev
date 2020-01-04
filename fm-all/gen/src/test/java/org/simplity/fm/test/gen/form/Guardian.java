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
import org.simplity.fm.test.gen.schema.GuardianData;
import org.simplity.fm.test.gen.schema.GuardianDataTable;
/** class for form guardian  */
public class Guardian extends Form {
	protected static final String NAME = "guardian";
	protected static final String SCHEMA = "guardian";
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
/** constructor */
public Guardian() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected GuardianFd newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new GuardianFd(this, (GuardianData) schemaData, values, data);
	}

	@Override
	protected GuardianFdt newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new GuardianFdt(this, (GuardianDataTable) table, values);
	}
}
