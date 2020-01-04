package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of institute
 */ 
public class Institute extends Schema {	private static final DbField[] FIELDS = {
			new DbField("instituteId", 0, DefinedDataTypes.id, null, null, null, "institute_id", ColumnType.GeneratedPrimaryKey), 
			new DbField("trustId", 1, DefinedDataTypes.id, null, null, null, "trust_id", ColumnType.RequiredData), 
			new DbField("name", 2, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("instituteType", 3, DefinedDataTypes.text, null, null, "instituteType", "institute_type", ColumnType.RequiredData), 
			new DbField("addressLine1", 4, DefinedDataTypes.desc, null, null, null, "address_line1", ColumnType.RequiredData), 
			new DbField("addressLine2", 5, DefinedDataTypes.desc, null, null, null, "address_line2", ColumnType.OptionalData), 
			new DbField("city", 6, DefinedDataTypes.name, null, null, null, "city", ColumnType.RequiredData), 
			new DbField("state", 7, DefinedDataTypes.state, null, null, null, "state", ColumnType.RequiredData), 
			new DbField("pincode", 8, DefinedDataTypes.pin, null, null, null, "pincode", ColumnType.RequiredData), 
			new DbField("country", 9, DefinedDataTypes.country, "130", null, null, "country", ColumnType.RequiredData), 
			new DbField("govtCode", 10, DefinedDataTypes.govtCode, null, null, null, "govt_code", ColumnType.OptionalData), 
			new DbField("phoneNumber", 11, DefinedDataTypes.phone, null, null, null, "phone_number", ColumnType.RequiredData), 
			new DbField("email", 12, DefinedDataTypes.email, null, null, null, "email", ColumnType.RequiredData), 
			new DbField("createdAt", 13, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("updatedAt", 14, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt), 
			new DbField("isInactive", 15, DefinedDataTypes.bool, null, null, null, "is_inactive", ColumnType.OptionalData)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT institute_id, trust_id, name, institute_type, address_line1, address_line2, city, state, pincode, country, govt_code, phone_number, email, created_at, updated_at, is_inactive FROM institutes";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	private static final  String INSERT = "INSERT INTO institutes(trust_id, name, institute_type, address_line1, address_line2, city, state, pincode, country, govt_code, phone_number, email, created_at, updated_at, is_inactive) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP , ?)";
	private static final int[] INSERT_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15};
	private static final String WHERE = " WHERE institute_id=?";
	private static final int[] WHERE_IDX = {0};
	private static final  String UPDATE = "UPDATE institutes SET trust_id= ? , name= ? , institute_type= ? , address_line1= ? , address_line2= ? , city= ? , state= ? , pincode= ? , country= ? , govt_code= ? , phone_number= ? , email= ? , updated_at= CURRENT_TIMESTAMP , is_inactive= ?  WHERE institute_id=?";
	private static final  int[] UPDATE_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 15, 0};
	private static final String DELETE = "DELETE FROM institutes";
	private static final IValidation[] VALIDS = {new DependentListValidation(7, 9, "state", "state", null)
	};

	/**
	 *
	 */
	public Institute() {
		this.name = "institute";
		this.nameInDb = "institutes";
		this.fields = FIELDS;
		this.validations = VALIDS;
		this.operations = OPS;

		this.dbAssistant = new DbAssistant(16, null, SELECT, this.getParams(SELECT_IDX), WHERE, this.getParams(WHERE_IDX), INSERT, this.getParams(INSERT_IDX), UPDATE, this.getParams(UPDATE_IDX), DELETE, "institute_id", 0, null);
		this.initialize();
	}

	@Override
	public InstituteData newSchemaData() {
		return new InstituteData(this, null);
	}

	@Override
	protected InstituteData newSchemaData(final Object[] data) {
		return new InstituteData(this, data);
	}

	@Override
	public InstituteDataTable newSchemaDataTable() {
		return new InstituteDataTable(this, null);
	}

	@Override
	protected InstituteDataTable newSchemaDataTable(final Object[][] data) {
		return new InstituteDataTable(this, data);
	}
}
