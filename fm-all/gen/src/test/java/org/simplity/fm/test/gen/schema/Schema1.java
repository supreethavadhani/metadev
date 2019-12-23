package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.FromToValidation;
import org.simplity.fm.core.validn.ExclusiveValidation;
import org.simplity.fm.core.validn.InclusiveValidation;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of schema1
 */ 
public class Schema1 extends Schema {
	public static class Indexes {
	public static final int field1 = 0;
	}
	private static final DbField[] FIELDS = {
			new DbField("field1", 0, DefinedDataTypes.id, null, null, "list1", null, ColumnType.GeneratedPrimaryKey)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};

	private void setDbAssistant(){
		//
	}
	private static final IValidation[] VALIDS = {new FromToValidation(0, 0, false, null, null)
	};

	/**
	 *
	 */
	public Schema1() {
		this.name = "schema1";
		this.nameInDb = "null";
		this.fields = FIELDS;
		this.validations = VALIDS;
		this.operations = OPS;

		this.dbAssistant = null;
		this.initialize();
	}
	/** @return specific instance **/
	public Schema1Data newSchema1Data(){
		 return new Schema1Data(this);
	}
}
