package org.simplity.fm.test.gen.schema;

import org.simplity.fm.core.data.DbField;
import org.simplity.fm.core.data.Schema;
import org.simplity.fm.core.validn.IValidation;
import org.simplity.fm.core.data.DbAssistant;
import org.simplity.fm.core.data.ColumnType;
import org.simplity.fm.core.validn.DependentListValidation;
import org.simplity.fm.test.gen.DefinedDataTypes;

/**
 * class that represents structure of student
 */ 
public class Student extends Schema {	private static final DbField[] FIELDS = {
			new DbField("studentId", 0, DefinedDataTypes.id, null, null, null, "student_id", ColumnType.PrimaryKey), 
			new DbField("instituteId", 1, DefinedDataTypes.tenantKey, null, null, null, "institute_id", ColumnType.TenantKey), 
			new DbField("departmentId", 2, DefinedDataTypes.id, null, null, "departmentList", "department_id", ColumnType.RequiredData), 
			new DbField("programId", 3, DefinedDataTypes.id, null, null, null, "program_id", ColumnType.RequiredData), 
			new DbField("tempUsn", 4, DefinedDataTypes.text, null, null, null, "temp_usn", ColumnType.RequiredData), 
			new DbField("usn", 5, DefinedDataTypes.text, null, null, null, "usn", ColumnType.OptionalData), 
			new DbField("name", 6, DefinedDataTypes.name, null, null, null, "name", ColumnType.RequiredData), 
			new DbField("presentAddressLine1", 7, DefinedDataTypes.desc, null, null, null, "present_address_line1", ColumnType.RequiredData), 
			new DbField("presentAddressLine2", 8, DefinedDataTypes.desc, null, null, null, "present_address_line2", ColumnType.OptionalData), 
			new DbField("presentCity", 9, DefinedDataTypes.name, null, null, null, "present_city", ColumnType.RequiredData), 
			new DbField("presentState", 10, DefinedDataTypes.state, null, null, null, "present_state", ColumnType.RequiredData), 
			new DbField("presentPincode", 11, DefinedDataTypes.pin, null, null, null, "present_pincode", ColumnType.RequiredData), 
			new DbField("presentCountry", 12, DefinedDataTypes.country, "130", null, null, "present_country", ColumnType.RequiredData), 
			new DbField("addressLine1", 13, DefinedDataTypes.desc, null, null, null, "address_line1", ColumnType.RequiredData), 
			new DbField("addressLine2", 14, DefinedDataTypes.desc, null, null, null, "address_line2", ColumnType.OptionalData), 
			new DbField("city", 15, DefinedDataTypes.name, null, null, null, "city", ColumnType.RequiredData), 
			new DbField("state", 16, DefinedDataTypes.state, null, null, null, "state", ColumnType.RequiredData), 
			new DbField("pincode", 17, DefinedDataTypes.pin, null, null, null, "pincode", ColumnType.RequiredData), 
			new DbField("country", 18, DefinedDataTypes.country, "130", null, null, "country", ColumnType.RequiredData), 
			new DbField("phoneNumber", 19, DefinedDataTypes.phone, null, null, null, "phone_number", ColumnType.RequiredData), 
			new DbField("email", 20, DefinedDataTypes.email, null, null, null, "email", ColumnType.RequiredData), 
			new DbField("gender", 21, DefinedDataTypes.gender, null, null, "gender", "gender", ColumnType.RequiredData), 
			new DbField("admissionQuota", 22, DefinedDataTypes.text, null, null, "admissionQuota", "admission_quota", ColumnType.RequiredData), 
			new DbField("admittedLevel", 23, DefinedDataTypes.text, null, null, null, "admitted_level", ColumnType.RequiredData), 
			new DbField("admissionDate", 24, DefinedDataTypes.date, null, null, null, "admission_date", ColumnType.RequiredData), 
			new DbField("bloodGroup", 25, DefinedDataTypes.text, null, null, null, "blood_group", ColumnType.OptionalData), 
			new DbField("religion", 26, DefinedDataTypes.text, null, null, "religion", "religion", ColumnType.RequiredData), 
			new DbField("caste", 27, DefinedDataTypes.text, null, null, null, "caste", ColumnType.RequiredData), 
			new DbField("nationality", 28, DefinedDataTypes.text, null, null, null, "nationality", ColumnType.RequiredData), 
			new DbField("category", 29, DefinedDataTypes.text, null, null, null, "category", ColumnType.RequiredData), 
			new DbField("personalId", 30, DefinedDataTypes.uniqueId, null, null, null, "personal_id", ColumnType.RequiredData), 
			new DbField("dateOfBirth", 31, DefinedDataTypes.date, null, null, null, "date_of_birth", ColumnType.RequiredData), 
			new DbField("placeOfBirth", 32, DefinedDataTypes.text, null, null, null, "place_of_birth", ColumnType.RequiredData), 
			new DbField("domicileState", 33, DefinedDataTypes.state, null, null, null, "domicile_state", ColumnType.RequiredData), 
			new DbField("previousBoard", 34, DefinedDataTypes.text, null, null, null, "previous_board", ColumnType.RequiredData), 
			new DbField("previousClass", 35, DefinedDataTypes.text, null, null, null, "previous_class", ColumnType.RequiredData), 
			new DbField("previousInstitute", 36, DefinedDataTypes.text, null, null, null, "previous_institute", ColumnType.OptionalData), 
			new DbField("qualifyingExamRank", 37, DefinedDataTypes.text, null, null, null, "qualifying_exam_rank", ColumnType.RequiredData), 
			new DbField("createdAt", 38, DefinedDataTypes.timestamp, null, null, null, "created_at", ColumnType.CreatedAt), 
			new DbField("updatedAt", 39, DefinedDataTypes.timestamp, null, null, null, "updated_at", ColumnType.ModifiedAt)
	};
	private static final  boolean[] OPS = {false, false, false, false, false, false};
	private static final String SELECT = "SELECT student_id, institute_id, department_id, program_id, temp_usn, usn, name, present_address_line1, present_address_line2, present_city, present_state, present_pincode, present_country, address_line1, address_line2, city, state, pincode, country, phone_number, email, gender, admission_quota, admitted_level, admission_date, blood_group, religion, caste, nationality, category, personal_id, date_of_birth, place_of_birth, domicile_state, previous_board, previous_class, previous_institute, qualifying_exam_rank, created_at, updated_at FROM students";
	private static final int[] SELECT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39};
	private static final  String INSERT = "INSERT INTO students(student_id, institute_id, department_id, program_id, temp_usn, usn, name, present_address_line1, present_address_line2, present_city, present_state, present_pincode, present_country, address_line1, address_line2, city, state, pincode, country, phone_number, email, gender, admission_quota, admitted_level, admission_date, blood_group, religion, caste, nationality, category, personal_id, date_of_birth, place_of_birth, domicile_state, previous_board, previous_class, previous_institute, qualifying_exam_rank, created_at, updated_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,  CURRENT_TIMESTAMP ,  CURRENT_TIMESTAMP )";
	private static final int[] INSERT_IDX = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37};
	private static final String WHERE = " WHERE student_id=? AND institute_id=?";
	private static final int[] WHERE_IDX = {0, 1};
	private static final  String UPDATE = "UPDATE students SET department_id= ? , program_id= ? , temp_usn= ? , usn= ? , name= ? , present_address_line1= ? , present_address_line2= ? , present_city= ? , present_state= ? , present_pincode= ? , present_country= ? , address_line1= ? , address_line2= ? , city= ? , state= ? , pincode= ? , country= ? , phone_number= ? , email= ? , gender= ? , admission_quota= ? , admitted_level= ? , admission_date= ? , blood_group= ? , religion= ? , caste= ? , nationality= ? , category= ? , personal_id= ? , date_of_birth= ? , place_of_birth= ? , domicile_state= ? , previous_board= ? , previous_class= ? , previous_institute= ? , qualifying_exam_rank= ? , updated_at= CURRENT_TIMESTAMP  WHERE student_id=? AND institute_id=?";
	private static final  int[] UPDATE_IDX = {2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 0, 1};
	private static final String DELETE = "DELETE FROM students";
	private static final IValidation[] VALIDS = {new DependentListValidation(3, 2, "programList", "programId", null),
		new DependentListValidation(10, 12, "state", "presentState", null),
		new DependentListValidation(16, 18, "state", "state", null)
	};

	/**
	 *
	 */
	public Student() {
		this.name = "student";
		this.nameInDb = "students";
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
		a.nbrFieldsInARow = 40;
		a.tenantField = this.fields[1];
		this.initialize();
	}

	@Override
	public StudentData newDataObject() {
		return new StudentData(this, null);
	}

	@Override
	protected StudentData newDataObject(final Object[] data) {
		return new StudentData(this, data);
	}

	@Override
	public StudentDataTable newDataTable() {
		return new StudentDataTable(this, null);
	}

	@Override
	protected StudentDataTable newDataTable(final Object[][] data) {
		return new StudentDataTable(this, data);
	}
}
