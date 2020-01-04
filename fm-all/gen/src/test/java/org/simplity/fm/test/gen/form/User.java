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
import org.simplity.fm.test.gen.schema.UserData;
import org.simplity.fm.test.gen.schema.UserDataTable;
/** class for form user  */
public class User extends Form {
	protected static final String NAME = "user";
	protected static final String SCHEMA = "user";
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
/** constructor */
public User() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected UserFd newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new UserFd(this, (UserData) schemaData, values, data);
	}

	@Override
	protected UserFdt newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new UserFdt(this, (UserDataTable) table, values);
	}
}
