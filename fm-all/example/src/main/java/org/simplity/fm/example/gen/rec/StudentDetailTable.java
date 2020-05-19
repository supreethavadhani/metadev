package org.simplity.fm.example.gen.rec;

import org.simplity.fm.core.data.DbTable;

/**
 * class that represents an array of records of studentDetail
 */
public class StudentDetailTable extends DbTable<StudentDetailRecord> {

	/** default constructor */
	public StudentDetailTable() {
		super(new StudentDetailRecord());
	}
}
