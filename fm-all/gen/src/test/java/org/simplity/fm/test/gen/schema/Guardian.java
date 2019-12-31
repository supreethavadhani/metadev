package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of guardian
 */ 
public class Guardian extends Schema {	private static final DbField[] FIELDS = {
			new DbField("guardianId", 0, DefinedDataTypes.id, null, null, null, "guardian_id", ColumnType.PrimaryKey), 
			new DbField("instituiteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", ColumnType.TenantKey), 
			new DbField("studentId", 2, DefinedDataTypes.id, null, null, null, "student_id", ColumnType.RequiredData), 
			new DbField("relationType", 3, DefinedDataTypes.text, null, null, "relationType", "relation_type", ColumnType.RequiredData), 
			new DbField("occupation", 4, DefinedDataTypes.text, null, null, null, "occupation", ColumnType.RequiredData), 
			new DbField("annualSalary", 5, DefinedDataTypes.income, null, null, null, "annual_salary", ColumnType.RequiredData), 
			new DbField("qualification", 6, DefinedDataTypes.text, null, null, null, "qualification", ColumnType.RequiredData), 
			new DbField("name", 7, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("addressLine1", 8, DefinedDataTypes.desc, null, null, null, "address_line1", ColumnType.RequiredData), 
			new DbField("addressLine2", 9, DefinedDataTypes.desc, null, null, null, "address_line2", ColumnType.OptionalData), 
			new DbField("city", 10, DefinedDataTypes.name, null, null, null, "city", ColumnType.RequiredData), 
			new DbField("state", 11, DefinedDataTypes.state, null, null, null, "state", ColumnType.RequiredData), 
			new DbField("pincode", 12, DefinedDataTypes.pin, null, null, null, "pincode", ColumnType.RequiredData), 
			new DbField("country", 13, DefinedDataTypes.country, "130", null, null, "country", ColumnType.RequiredData), 
			new DbField("phoneNumber", 14, DefinedDataTypes.phone, null, null, null, "phone_number", ColumnType.RequiredData), 
			new DbField("email", 15, DefinedDataTypes.email, null, null, null, "email", ColumnType.RequiredData), 
			new DbField("gender", 16, DefinedDataTypes.gender, null, null, "gender", "gender", ColumnType.RequiredData), 
			new DbField("updatedAt", 17, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt), 
			new DbField("createdAt", 18, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT guardian_id, institute_id, student_id, relation_type, occupation, annual_salary, qualification, name, address_line1, address_line2, city, state, pincode, country, phone_number, email, gender, updated_at, created_at FROM guardians";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
	private static final  String INSERT = "INSERT INTO guardians(guardian_id, institute_id, student_id, relation_type, occupation, annual_salary, qualification, name, address_line1, address_line2, city, state, pincode, country, phone_number, email, gender, updated_at, created_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP )";
	private static final int[] INSERT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
	private static final String WHERE = " WHERE guardian_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final  String UPDATE = "UPDATE guardians SET student_id= ? , relation_type= ? , occupation= ? , annual_salary= ? , qualification= ? , name= ? , address_line1= ? , address_line2= ? , city= ? , state= ? , pincode= ? , country= ? , phone_number= ? , email= ? , gender= ? , updated_at= CURRENT_TIMESTAMP  WHERE guardian_id=? AND institute_id=?";
	private static final  int[] UPDATE_IDX = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 0, 1};
	private static final String DELETE = "DELETE FROM guardians";
	private static final IValidation[] VALIDS = {new DependentListValidation(11, 13, "state", "state", null)
	};

	/**
	 *
	 */
	public Guardian() {
		this.name = "guardian";
		this.nameInDb = "guardians";
		this.fields = FIELDS;
		this.validations = VALIDS;
		this.operations = OPS;

		DbAssistant a = new DbAssistant();

		this.dbAssistant = a;
		a.selectClause = SELECT;
		a.selectParams = this.getParams(SELECT_IDX);
		a.whereClause = WHERE;
		a.whereParams = this.getParams(WHERE_IDX);
		a.insertClause = INSERT;
		a.insertParams = this.getParams(INSERT_IDX);
		a.updateClause = UPDATE;
		a.updateParams = this.getParams(UPDATE_IDX);
		a.deleteClause = DELETE;
		a.nbrFieldsInARow = 19;
		a.tenantField = this.fields[1];
		this.initialize();
	}

	@Override
	public GuardianData newDataObject() {
		return new GuardianData(this, null);
	}

	@Override
	protected GuardianData newDataObject(final Object[] data) {
		return new GuardianData(this, data);
	}

	@Override
	public GuardianDataTable newDataTable() {
		return new GuardianDataTable(this, null);
	}

	@Override
	protected GuardianDataTable newDataTable(final Object[][] data) {
		return new GuardianDataTable(this, data);
	}
}
