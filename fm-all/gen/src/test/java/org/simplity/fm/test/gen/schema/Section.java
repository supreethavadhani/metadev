package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of section
 */ 
public class Section extends Schema {	private static final DbField[] FIELDS = {
			new DbField("sectionId", 0, DefinedDataTypes.id, null, null, null, "section_id", ColumnType.GeneratedPrimaryKey), 
			new DbField("instituteId", 1, DefinedDataTypes.id, null, null, null, "institute_id", ColumnType.TenantKey), 
			new DbField("name", 2, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("code", 3, DefinedDataTypes.code, null, null, null, "code", ColumnType.OptionalData), 
			new DbField("createdAt", 4, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("updatedAt", 5, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT section_id, institute_id, name, code, created_at, updated_at FROM section";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5};
	private static final  String INSERT = "INSERT INTO section(institute_id, name, code, created_at, updated_at) values (?, ?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP )";
	private static final int[] INSERT_IDX = {1, 2, 3};
	private static final String WHERE = " WHERE section_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final  String UPDATE = "UPDATE section SET name= ? , code= ? , updated_at= CURRENT_TIMESTAMP  WHERE section_id=? AND institute_id=?";
	private static final  int[] UPDATE_IDX = {2, 3, 0, 1};
	private static final String DELETE = "DELETE FROM section";
	private static final IValidation[] VALIDS = {
	};

	/**
	 *
	 */
	public Section() {
		this.name = "section";
		this.nameInDb = "section";
		this.fields = FIELDS;
		this.validations = VALIDS;
		this.operations = OPS;

		this.dbAssistant = new DbAssistant(6, this.fields[1], SELECT, this.getParams(SELECT_IDX), WHERE, this.getParams(WHERE_IDX), INSERT, this.getParams(INSERT_IDX), UPDATE, this.getParams(UPDATE_IDX), DELETE, "section_id", 0, null);
		this.initialize();
	}

	@Override
	public SectionData newSchemaData() {
		return new SectionData(this, null);
	}

	@Override
	protected SectionData newSchemaData(final Object[] data) {
		return new SectionData(this, data);
	}

	@Override
	public SectionDataTable newSchemaDataTable() {
		return new SectionDataTable(this, null);
	}

	@Override
	protected SectionDataTable newSchemaDataTable(final Object[][] data) {
		return new SectionDataTable(this, data);
	}
}
