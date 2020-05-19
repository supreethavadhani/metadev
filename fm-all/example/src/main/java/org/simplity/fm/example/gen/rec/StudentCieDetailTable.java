package org.simplity.fm.example.gen.rec;

import org.simplity.fm.core.data.DbTable;

/**
 * class that represents an array of records of studentCieDetail
 */
public class StudentCieDetailTable extends DbTable<StudentCieDetailRecord> {

	/** default constructor */
	public StudentCieDetailTable() {
		super(new StudentCieDetailRecord());
	}
}
