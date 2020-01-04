package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of user
 */ 
public class User extends Schema {	private static final DbField[] FIELDS = {
			new DbField("userId", 0, DefinedDataTypes.id, null, null, null, "user_id", ColumnType.GeneratedPrimaryKey), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", ColumnType.OptionalData), 
			new DbField("trustId", 2, DefinedDataTypes.id, null, null, null, "trust_id", ColumnType.OptionalData), 
			new DbField("userType", 3, DefinedDataTypes.userType, "Student", null, "userType", "user_type", ColumnType.RequiredData), 
			new DbField("loginId", 4, DefinedDataTypes.loginId, null, null, null, "login_id", ColumnType.RequiredData), 
			new DbField("password", 5, DefinedDataTypes.text, null, null, null, "password", ColumnType.OptionalData), 
			new DbField("loginEnabled", 6, DefinedDataTypes.bool, "false", null, null, "login_enabled", ColumnType.RequiredData), 
			new DbField("previousLoginAt", 7, DefinedDataTypes.timestamp, null, null, null, "previous_login_at", ColumnType.OptionalData), 
			new DbField("currentLoginAt", 8, DefinedDataTypes.timestamp, null, null, null, "current_login_at", ColumnType.OptionalData), 
			new DbField("resetPasswordCount", 9, DefinedDataTypes.integer, null, null, null, "reset_password_count", ColumnType.OptionalData), 
			new DbField("resetPasswordSentAt", 10, DefinedDataTypes.timestamp, null, null, null, "reset_password_sent_at", ColumnType.OptionalData), 
			new DbField("currentLoginIp", 11, DefinedDataTypes.ip, null, null, null, "current_login_ip", ColumnType.OptionalData), 
			new DbField("previousLoginIp", 12, DefinedDataTypes.ip, null, null, null, "previous_login_ip", ColumnType.OptionalData), 
			new DbField("loginCount", 13, DefinedDataTypes.integer, null, null, null, "login_count", ColumnType.OptionalData), 
			new DbField("confirmationToken", 14, DefinedDataTypes.text, null, null, null, "confirmation_token", ColumnType.OptionalData), 
			new DbField("loginToken", 15, DefinedDataTypes.text, null, null, null, "login_token", ColumnType.OptionalData), 
			new DbField("createdAt", 16, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("updatedAt", 17, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT user_id, institute_id, trust_id, user_type, login_id, password, login_enabled, previous_login_at, current_login_at, reset_password_count, reset_password_sent_at, current_login_ip, previous_login_ip, login_count, confirmation_token, login_token, created_at, updated_at FROM users";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17};
	private static final  String INSERT = "INSERT INTO users(institute_id, trust_id, user_type, login_id, password, login_enabled, previous_login_at, current_login_at, reset_password_count, reset_password_sent_at, current_login_ip, previous_login_ip, login_count, confirmation_token, login_token, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP )";
	private static final int[] INSERT_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15};
	private static final String WHERE = " WHERE user_id=?";
	private static final int[] WHERE_IDX = {0};
	private static final  String UPDATE = "UPDATE users SET institute_id= ? , trust_id= ? , user_type= ? , login_id= ? , password= ? , login_enabled= ? , previous_login_at= ? , current_login_at= ? , reset_password_count= ? , reset_password_sent_at= ? , current_login_ip= ? , previous_login_ip= ? , login_count= ? , confirmation_token= ? , login_token= ? , updated_at= CURRENT_TIMESTAMP  WHERE user_id=?";
	private static final  int[] UPDATE_IDX = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0};
	private static final String DELETE = "DELETE FROM users";
	private static final IValidation[] VALIDS = {
	};

	/**
	 *
	 */
	public User() {
		this.name = "user";
		this.nameInDb = "users";
		this.fields = FIELDS;
		this.validations = VALIDS;
		this.operations = OPS;

		this.dbAssistant = new DbAssistant(18, null, SELECT, this.getParams(SELECT_IDX), WHERE, this.getParams(WHERE_IDX), INSERT, this.getParams(INSERT_IDX), UPDATE, this.getParams(UPDATE_IDX), DELETE, "user_id", 0, null);
		this.initialize();
	}

	@Override
	public UserData newSchemaData() {
		return new UserData(this, null);
	}

	@Override
	protected UserData newSchemaData(final Object[] data) {
		return new UserData(this, data);
	}

	@Override
	public UserDataTable newSchemaDataTable() {
		return new UserDataTable(this, null);
	}

	@Override
	protected UserDataTable newSchemaDataTable(final Object[][] data) {
		return new UserDataTable(this, data);
	}
}
