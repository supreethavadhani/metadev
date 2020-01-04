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
import org.simplity.fm.test.gen.schema.DepartmentData;
import org.simplity.fm.test.gen.schema.DepartmentDataTable;
/** class for form department  */
public class Department extends Form {
	protected static final String NAME = "department";
	protected static final String SCHEMA = "department";
	protected static final  boolean[] OPS = {false, false, false, false, false, false};
/** constructor */
public Department() {
		this.name = NAME;
		this.schema = ComponentProvider.getProvider().getSchema(SCHEMA);
		this.operations = OPS;
	}

	@Override
	protected DepartmentFd newFormData(final SchemaData schemaData, final Object[] values, final FormDataTable[] data) {
		return new DepartmentFd(this, (DepartmentData) schemaData, values, data);
	}

	@Override
	protected DepartmentFdt newFormDataTable(final SchemaDataTable table, final Object[][] values) {
		return new DepartmentFdt(this, (DepartmentDataTable) table, values);
	}
}
