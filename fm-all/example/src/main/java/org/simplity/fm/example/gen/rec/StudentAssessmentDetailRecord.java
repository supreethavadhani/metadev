package org.simplity.fm.example.gen.rec;

import org.simplity.fm.core.data.Field;
import org.simplity.fm.core.data.RecordMetaData;
import org.simplity.fm.core.data.Dba;
import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.DbRecord;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.service.IServiceContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.util.List;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.example.gen.DefinedDataTypes;

/**
 * class that represents structure of studentAssessmentDetail
 */ 
public class StudentAssessmentDetailRecord extends DbRecord {
	private static final Field[] FIELDS = {
			new DbField("studentAssessmentId", 0, DefinedDataTypes.id, null, null, null, "student_assessment_id", ColumnType.GeneratedPrimaryKey), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", ColumnType.TenantKey), 
			new DbField("subjectSectionId", 2, DefinedDataTypes.id, null, null, null, "subject_section_id", ColumnType.OptionalData), 
			new DbField("assessmentSchemeId", 3, DefinedDataTypes.id, null, null, null, "assessment_scheme_id", ColumnType.OptionalData), 
			new DbField("assessmentSeqNo", 4, DefinedDataTypes.integer, null, null, null, "assessment_seq_no", ColumnType.RequiredData), 
			new DbField("studentId", 5, DefinedDataTypes.id, null, null, null, "student_id", ColumnType.RequiredData), 
			new DbField("name", 6, DefinedDataTypes.name, null, null, null, "name", ColumnType.OptionalData), 
			new DbField("usn", 7, DefinedDataTypes.text, null, null, null, "usn", ColumnType.OptionalData), 
			new DbField("hasAttended", 8, DefinedDataTypes.bool, "false", null, null, "has_attended", ColumnType.OptionalData), 
			new DbField("marksScored", 9, DefinedDataTypes.integer, "0", null, null, "marks_scored", ColumnType.OptionalData), 
			new DbField("marks", 10, DefinedDataTypes.text, null, null, null, null, ColumnType.OptionalData)
	};
	private static final IValidation[] VALIDS = {
	};

	private static final RecordMetaData META = new RecordMetaData("studentAssessmentDetail", FIELDS, VALIDS);
	/* DB related */
	private static final  boolean[] OPS = {true, false, false, false, true, false};
	private static final String SELECT = "SELECT student_assessment_id, institute_id, subject_section_id, assessment_scheme_id, assessment_seq_no, student_id, name, usn, has_attended, marks_scored, null FROM student_assessment_details";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	private static final  String INSERT = "INSERT INTO student_assessment_details(institute_id, subject_section_id, assessment_scheme_id, assessment_seq_no, student_id, name, usn, has_attended, marks_scored, null) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final int[] INSERT_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
	private static final String WHERE = " WHERE student_assessment_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final  String UPDATE = "UPDATE student_assessment_details SET subject_section_id= ? , assessment_scheme_id= ? , assessment_seq_no= ? , student_id= ? , name= ? , usn= ? , has_attended= ? , marks_scored= ? , null= ?  WHERE student_assessment_id=? AND institute_id=?";
	private static final  int[] UPDATE_IDX = {2, 3, 4, 5, 6, 7, 8, 9, 10, 0, 1};
	private static final String DELETE = "DELETE FROM student_assessment_details";

	private static final Dba DBA = new Dba(FIELDS, "student_assessment_details", OPS, SELECT, SELECT_IDX, INSERT, INSERT_IDX, UPDATE, UPDATE_IDX, DELETE, WHERE, WHERE_IDX);

	/**  default constructor */
	public StudentAssessmentDetailRecord() {
		super(DBA, META, null);
	}

	/**
	 * @param values initial values
	 */
	public StudentAssessmentDetailRecord(Object[] values) {
		super(DBA, META, values);
	}

	@Override
	public StudentAssessmentDetailRecord newInstance(final Object[] values) {
		return new StudentAssessmentDetailRecord(values);
	}
}
