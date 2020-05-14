package org.simplity.fm.example.gen.rec;

import org.simplity.fm.core.data.DbTable;

/**
 * class that represents an array of records of user
 */
public class UserTable extends DbTable<UserRecord> {

	/** default constructor 
	 * @param record 
	 */
	public UserTable() {
		super(new UserRecord());
	}
}
