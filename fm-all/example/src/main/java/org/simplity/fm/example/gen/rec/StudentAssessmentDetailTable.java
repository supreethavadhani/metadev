package org.simplity.fm.example.gen.rec;

import org.simplity.fm.core.data.DbTable;

/**
 * class that represents an array of records of studentAssessmentDetail
 */
public class StudentAssessmentDetailTable extends DbTable<StudentAssessmentDetailRecord> {

	/** default constructor */
	public StudentAssessmentDetailTable() {
		super(new StudentAssessmentDetailRecord());
	}
}
