package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of trustee
 */ 
public class Trustee extends Schema {	private static final DbField[] FIELDS = {
			new DbField("trusteeId", 0, DefinedDataTypes.id, null, null, null, "trustee_id", ColumnType.GeneratedPrimaryKey), 
			new DbField("trustId", 1, DefinedDataTypes.id, null, null, null, "trust_id", ColumnType.RequiredData), 
			new DbField("name", 2, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("phoneNumber", 3, DefinedDataTypes.phone, null, null, null, "phone_number", ColumnType.RequiredData), 
			new DbField("email", 4, DefinedDataTypes.email, null, null, null, "email", ColumnType.RequiredData), 
			new DbField("designation", 5, DefinedDataTypes.name, null, null, null, "designation", ColumnType.OptionalData), 
			new DbField("createdAt", 6, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("updatedAt", 7, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT trustee_id, trust_id, name, phone_number, email, designation, created_at, updated_at FROM trustsees";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7};
	private static final  String INSERT = "INSERT INTO trustsees(trust_id, name, phone_number, email, designation, created_at, updated_at) values (?, ?, ?, ?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP )";
	private static final int[] INSERT_IDX = {1, 2, 3, 4, 5};
	private static final String WHERE = " WHERE trustee_id=?";
	private static final int[] WHERE_IDX = {0};
	private static final  String UPDATE = "UPDATE trustsees SET trust_id= ? , name= ? , phone_number= ? , email= ? , designation= ? , updated_at= CURRENT_TIMESTAMP  WHERE trustee_id=?";
	private static final  int[] UPDATE_IDX = {1, 2, 3, 4, 5, 0};
	private static final String DELETE = "DELETE FROM trustsees";
	private static final IValidation[] VALIDS = {
	};

	/**
	 *
	 */
	public Trustee() {
		this.name = "trustee";
		this.nameInDb = "trustsees";
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
		a.generatedColumnName = "trustee_id";
		a.generatedKeyIdx = 0;
		a.nbrFieldsInARow = 8;
		this.initialize();
	}

	@Override
	public TrusteeData newDataObject() {
		return new TrusteeData(this, null);
	}

	@Override
	protected TrusteeData newDataObject(final Object[] data) {
		return new TrusteeData(this, data);
	}

	@Override
	public TrusteeDataTable newDataTable() {
		return new TrusteeDataTable(this, null);
	}

	@Override
	protected TrusteeDataTable newDataTable(final Object[][] data) {
		return new TrusteeDataTable(this, data);
	}
}
