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
			new DbField("name", 1, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("code", 2, DefinedDataTypes.code, null, null, null, "code", ColumnType.OptionalData), 
			new DbField("createdAt", 3, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("updatedAt", 4, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT section_id, name, code, created_at, updated_at FROM section";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4};
	private static final  String INSERT = "INSERT INTO section(name, code, created_at, updated_at) values (?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP )";
	private static final int[] INSERT_IDX = {1, 2};
	private static final String WHERE = " WHERE section_id=?";
	private static final int[] WHERE_IDX = {0};
	private static final  String UPDATE = "UPDATE section SET name= ? , code= ? , updated_at= CURRENT_TIMESTAMP  WHERE section_id=?";
	private static final  int[] UPDATE_IDX = {1, 2, 0};
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
		a.generatedColumnName = "section_id";
		a.generatedKeyIdx = 0;
		a.nbrFieldsInARow = 5;
		this.initialize();
	}

	@Override
	public SectionData newDataObject() {
		return new SectionData(this, null);
	}

	@Override
	protected SectionData newDataObject(final Object[] data) {
		return new SectionData(this, data);
	}

	@Override
	public SectionDataTable newDataTable() {
		return new SectionDataTable(this, null);
	}

	@Override
	protected SectionDataTable newDataTable(final Object[][] data) {
		return new SectionDataTable(this, data);
	}
}
