package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of trust
 */ 
public class Trust extends Schema {	private static final DbField[] FIELDS = {
			new DbField("trustId", 0, DefinedDataTypes.id, null, null, null, "trust_id", ColumnType.GeneratedPrimaryKey), 
			new DbField("name", 1, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("addressLine1", 2, DefinedDataTypes.desc, null, null, null, "address_line1", ColumnType.RequiredData), 
			new DbField("addressLine2", 3, DefinedDataTypes.desc, null, null, null, "address_line2", ColumnType.OptionalData), 
			new DbField("city", 4, DefinedDataTypes.name, null, null, null, "city", ColumnType.RequiredData), 
			new DbField("state", 5, DefinedDataTypes.state, null, null, "state", "state", ColumnType.RequiredData), 
			new DbField("pincode", 6, DefinedDataTypes.pin, null, null, null, "pincode", ColumnType.RequiredData), 
			new DbField("country", 7, DefinedDataTypes.country, "130", null, null, "country", ColumnType.RequiredData), 
			new DbField("phoneNumber", 8, DefinedDataTypes.phone, null, null, null, "phone_number", ColumnType.RequiredData), 
			new DbField("email", 9, DefinedDataTypes.email, null, null, null, "email", ColumnType.RequiredData), 
			new DbField("govtCode", 10, DefinedDataTypes.govtCode, null, null, null, "govt_code", ColumnType.OptionalData), 
			new DbField("accountStatus", 11, DefinedDataTypes.accountStatus, null, null, "accountStatus", "account_status", ColumnType.RequiredData), 
			new DbField("createdAt", 12, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("updatedAt", 13, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT trust_id, name, address_line1, address_line2, city, state, pincode, country, phone_number, email, govt_code, account_status, created_at, updated_at FROM trusts";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
	private static final  String INSERT = "INSERT INTO trusts(name, address_line1, address_line2, city, state, pincode, country, phone_number, email, govt_code, account_status, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP )";
	private static final int[] INSERT_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
	private static final String WHERE = " WHERE trust_id=?";
	private static final int[] WHERE_IDX = {0};
	private static final  String UPDATE = "UPDATE trusts SET name= ? , address_line1= ? , address_line2= ? , city= ? , state= ? , pincode= ? , country= ? , phone_number= ? , email= ? , govt_code= ? , account_status= ? , updated_at= CURRENT_TIMESTAMP  WHERE trust_id=?";
	private static final  int[] UPDATE_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 0};
	private static final String DELETE = "DELETE FROM trusts";
	private static final IValidation[] VALIDS = {
	};

	/**
	 *
	 */
	public Trust() {
		this.name = "trust";
		this.nameInDb = "trusts";
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
		a.generatedColumnName = "trust_id";
		a.generatedKeyIdx = 0;
		a.nbrFieldsInARow = 14;
		this.initialize();
	}

	@Override
	public TrustData newDataObject() {
		return new TrustData(this, null);
	}

	@Override
	protected TrustData newDataObject(final Object[] data) {
		return new TrustData(this, data);
	}

	@Override
	public TrustDataTable newDataTable() {
		return new TrustDataTable(this, null);
	}

	@Override
	protected TrustDataTable newDataTable(final Object[][] data) {
		return new TrustDataTable(this, data);
	}
}
