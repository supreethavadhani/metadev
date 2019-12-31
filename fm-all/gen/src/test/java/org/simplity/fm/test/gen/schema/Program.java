package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of program
 */ 
public class Program extends Schema {	private static final DbField[] FIELDS = {
			new DbField("programId", 0, DefinedDataTypes.id, null, null, null, "program_id", ColumnType.GeneratedPrimaryKey), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", ColumnType.TenantKey), 
			new DbField("departmentId", 2, DefinedDataTypes.id, null, null, "departmentList", "department_id", ColumnType.RequiredData), 
			new DbField("name", 3, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("code", 4, DefinedDataTypes.code, null, null, null, "code", ColumnType.OptionalData), 
			new DbField("createdAt", 5, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("updatedAt", 6, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT program_id, institute_id, department_id, name, code, created_at, updated_at FROM programs";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6};
	private static final  String INSERT = "INSERT INTO programs(institute_id, department_id, name, code, created_at, updated_at) values (?, ?, ?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP )";
	private static final int[] INSERT_IDX = {1, 2, 3, 4};
	private static final String WHERE = " WHERE program_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final  String UPDATE = "UPDATE programs SET department_id= ? , name= ? , code= ? , updated_at= CURRENT_TIMESTAMP  WHERE program_id=? AND institute_id=?";
	private static final  int[] UPDATE_IDX = {2, 3, 4, 0, 1};
	private static final String DELETE = "DELETE FROM programs";
	private static final IValidation[] VALIDS = {
	};

	/**
	 *
	 */
	public Program() {
		this.name = "program";
		this.nameInDb = "programs";
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
		a.generatedColumnName = "program_id";
		a.generatedKeyIdx = 0;
		a.nbrFieldsInARow = 7;
		a.tenantField = this.fields[1];
		this.initialize();
	}

	@Override
	public ProgramData newDataObject() {
		return new ProgramData(this, null);
	}

	@Override
	protected ProgramData newDataObject(final Object[] data) {
		return new ProgramData(this, data);
	}

	@Override
	public ProgramDataTable newDataTable() {
		return new ProgramDataTable(this, null);
	}

	@Override
	protected ProgramDataTable newDataTable(final Object[][] data) {
		return new ProgramDataTable(this, data);
	}
}
