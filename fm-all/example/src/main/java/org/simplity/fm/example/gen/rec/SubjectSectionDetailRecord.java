package org.simplity.fm.example.gen.rec;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.RecordMetaData;
import org.simplity.fm.core.data.Dba;
import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.FieldType;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.service.IServiceContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.List;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.example.gen.DefinedDataTypes;

/**
 * class that represents structure of subjectSectionDetail
 */ 
public class SubjectSectionDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("subjectSectionId", 0, DefinedDataTypes.id, null, null, null, "subject_section_id", FieldType.PrimaryKey), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", FieldType.TenantKey), 
			new DbField("offeredSubjectId", 2, DefinedDataTypes.id, "0", null, null, "offered_subject_id", FieldType.OptionalData), 
			new DbField("subjectId", 3, DefinedDataTypes.id, "0", null, null, "subject_id", FieldType.OptionalData), 
			new DbField("levelSectionId", 4, DefinedDataTypes.id, "0", null, null, "level_section_id", FieldType.OptionalData), 
			new DbField("departmentId", 5, DefinedDataTypes.id, null, null, null, "department_id", FieldType.OptionalData), 
			new DbField("sectionId", 6, DefinedDataTypes.id, null, null, null, "section_id", FieldType.OptionalData), 
			new DbField("subjectName", 7, DefinedDataTypes.name, null, null, null, "subject_name", FieldType.OptionalData), 
			new DbField("subjectCode", 8, DefinedDataTypes.name, null, null, null, "subject_code", FieldType.OptionalData), 
			new DbField("sectionName", 9, DefinedDataTypes.name, null, null, null, "section_name", FieldType.OptionalData), 
			new DbField("totalClasses", 10, DefinedDataTypes.integer, null, null, null, "total_classes", FieldType.OptionalData), 
			new DbField("attendanceFrozen", 11, DefinedDataTypes.bool, "false", null, null, "attendance_frozen", FieldType.OptionalData), 
			new DbField("cieFrozen", 12, DefinedDataTypes.bool, "false", null, null, "cie_frozen", FieldType.OptionalData), 
			new DbField("isOffered", 13, DefinedDataTypes.bool, "false", null, null, "is_offered", FieldType.OptionalData)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("subjectSectionDetail", FIELDS, VALIDS);
	/* DB related */
	private static final  boolean[] OPS = {true, false, true, false, true, false};
	private static final String SELECT = "SELECT subject_section_id, institute_id, offered_subject_id, subject_id, level_section_id, department_id, section_id, subject_name, subject_code, section_name, total_classes, attendance_frozen, cie_frozen, is_offered FROM subject_section_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
	private static final  String INSERT = "INSERT INTO subject_section_details(subject_section_id, institute_id, offered_subject_id, subject_id, level_section_id, department_id, section_id, subject_name, subject_code, section_name, total_classes, attendance_frozen, cie_frozen, is_offered) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final int[] INSERT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13};
	private static final String WHERE = " WHERE subject_section_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final  String UPDATE = "UPDATE subject_section_details SET offered_subject_id= ? , subject_id= ? , level_section_id= ? , department_id= ? , section_id= ? , subject_name= ? , subject_code= ? , section_name= ? , total_classes= ? , attendance_frozen= ? , cie_frozen= ? , is_offered= ?  WHERE subject_section_id=? AND institute_id=?";
	private static final  int[] UPDATE_IDX = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 0, 1};
	private static final String DELETE = "DELETE FROM subject_section_details";

	private static final Dba DBA = new Dba(FIELDS, "subject_section_details", OPS, SELECT, SELECT_IDX, INSERT, INSERT_IDX, UPDATE, UPDATE_IDX, DELETE, WHERE, WHERE_IDX);

	/**  default constructor */
	public SubjectSectionDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values initial values
	 */
	public SubjectSectionDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public SubjectSectionDetailRecord newInstance(final Object[] values) {
		return new SubjectSectionDetailRecord(values);
	}
}
