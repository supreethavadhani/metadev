package org.simplity.fm.example.gen.rec;

import org.simplity.fm.core.data.DbTable;

/**
 * class that represents an array of records of subjectSectionDetail
 */
public class SubjectSectionDetailTable extends DbTable<SubjectSectionDetailRecord> {

	/** default constructor */
	public SubjectSectionDetailTable() {
		super(new SubjectSectionDetailRecord());
	}
}
