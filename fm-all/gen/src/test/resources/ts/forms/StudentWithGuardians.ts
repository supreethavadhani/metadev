
import { Form , Field, ChildForm } from '../form/form';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'
import { Guardian } from './guardian';

export class StudentWithGuardians extends Form {
	private static _instance = new StudentWithGuardians();
	studentId:Field = {
		name:'studentId'
		,controlType: 'Hidden'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	instituteId:Field = {
		name:'instituteId'
		,controlType: 'Hidden'
		,valueType: 1
		,errorId: 'invalidTenentKey'
		,maxValue: 9999999999999
	};
	departmentId:Field = {
		name:'departmentId'
		,controlType: 'Dropdown'
		,label: 'Department'
		,isRequired: true
		,listName: 'departmentList'
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	program:Field = {
		name:'program'
		,controlType: 'Dropdown'
		,label: 'Progrm'
	};
	level:Field = {
		name:'level'
		,controlType: 'Dropdown'
		,label: 'Level'
	};
	section:Field = {
		name:'section'
		,controlType: 'Input'
		,label: 'Section'
	};
	name:Field = {
		name:'name'
		,controlType: 'Input'
		,label: 'Name'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	gender:Field = {
		name:'gender'
		,controlType: 'Dropdown'
		,label: 'Gender'
		,isRequired: true
		,listName: 'gender'
		,valueList: [
			{value:'Male',text:'Male'},
			{value:'Female',text:'Female'},
			{value:'Others',text:'Others'},
			{value:'Not Applicable',text:'Not Applicable'}
			]
		,valueType: 0
		,errorId: 'invalidGender'
		,maxLength: 10
	};
	presentAddressLine1:Field = {
		name:'presentAddressLine1'
		,controlType: 'Input'
		,label: 'Address - Line 1'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidDesc'
		,maxLength: 1000
	};
	presentAddressLine2:Field = {
		name:'presentAddressLine2'
		,controlType: 'Input'
		,label: 'Address - Line 2'
		,valueType: 0
		,errorId: 'invalidDesc'
		,maxLength: 1000
	};
	presentCity:Field = {
		name:'presentCity'
		,controlType: 'Input'
		,label: 'City'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	presentState:Field = {
		name:'presentState'
		,controlType: 'Input'
		,label: 'State'
		,isRequired: true
		,listName: 'state'
		,listKey: 'presentCountry'
		,keyedList: {
			91 : [
				{value:'Karnataka',text:'Karnataka'}, 
				{value:'Tamil Nadu',text:'Tamil Nadu'}, 
				{value:'Kerala',text:'Kerala'}, 
				{value:'Uttar Pradesh',text:'Uttar Pradesh'}
			], 
			130 : [
				{value:'Karnataka',text:'Karnataka'}, 
				{value:'Tamil Nadu',text:'Tamil Nadu'}, 
				{value:'Kerala',text:'Kerala'}, 
				{value:'Uttar Pradesh',text:'Uttar Pradesh'}
			]
			}
		,valueType: 0
		,errorId: 'invalidState'
		,maxLength: 50
	};
	presentPincode:Field = {
		name:'presentPincode'
		,controlType: 'Input'
		,label: 'Pin Code'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidPin'
		,minLength: 6
		,maxLength: 6
	};
	presentCountry:Field = {
		name:'presentCountry'
		,controlType: 'Hidden'
		,label: 'Country'
		,isRequired: true
		,valueType: 1
		,defaultValue: 130
		,errorId: 'invalidCountry'
		,maxValue: 999
	};
	addressLine1:Field = {
		name:'addressLine1'
		,controlType: 'Input'
		,label: 'Address - Line 1'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidDesc'
		,maxLength: 1000
	};
	addressLine2:Field = {
		name:'addressLine2'
		,controlType: 'Input'
		,label: 'Address - Line 2'
		,valueType: 0
		,errorId: 'invalidDesc'
		,maxLength: 1000
	};
	city:Field = {
		name:'city'
		,controlType: 'Input'
		,label: 'City'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	state:Field = {
		name:'state'
		,controlType: 'Dropdown'
		,label: 'State'
		,isRequired: true
		,listName: 'state'
		,listKey: 'country'
		,keyedList: {
			91 : [
				{value:'Karnataka',text:'Karnataka'}, 
				{value:'Tamil Nadu',text:'Tamil Nadu'}, 
				{value:'Kerala',text:'Kerala'}, 
				{value:'Uttar Pradesh',text:'Uttar Pradesh'}
			], 
			130 : [
				{value:'Karnataka',text:'Karnataka'}, 
				{value:'Tamil Nadu',text:'Tamil Nadu'}, 
				{value:'Kerala',text:'Kerala'}, 
				{value:'Uttar Pradesh',text:'Uttar Pradesh'}
			]
			}
		,valueType: 0
		,errorId: 'invalidState'
		,maxLength: 50
	};
	pincode:Field = {
		name:'pincode'
		,controlType: 'Input'
		,label: 'Pin Code'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidPin'
		,minLength: 6
		,maxLength: 6
	};
	country:Field = {
		name:'country'
		,controlType: 'Hidden'
		,label: 'Country'
		,isRequired: true
		,valueType: 1
		,defaultValue: 130
		,errorId: 'invalidCountry'
		,maxValue: 999
	};
	phoneNumber:Field = {
		name:'phoneNumber'
		,controlType: 'Input'
		,label: 'Phone'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidPhone'
		,maxLength: 20
	};
	email:Field = {
		name:'email'
		,controlType: 'Input'
		,label: 'Email'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidEmail'
		,maxLength: 1000
	};
	admissionQuota:Field = {
		name:'admissionQuota'
		,controlType: 'Input'
		,label: 'Admission Quota'
		,isRequired: true
		,listName: 'admissionQuota'
		,valueList: [
			{value:'CET',text:'CET'},
			{value:'COMEDK',text:'COMEDK'},
			{value:'CETSNQ',text:'CET-SNQ'},
			{value:'MANG',text:'MANAGEMENT'},
			{value:'NRI',text:'NRI'},
			{value:'GOI',text:'GOI'},
			{value:'Other',text:'Other'}
			]
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	admittedLevel:Field = {
		name:'admittedLevel'
		,controlType: 'Input'
		,label: 'Admitted Level'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	admissionDate:Field = {
		name:'admissionDate'
		,controlType: 'Input'
		,label: 'Admission Date'
		,isRequired: true
		,valueType: 4
		,errorId: 'invalidDate'
		,minValue: 73000
		,maxValue: 73000
	};
	bloodGroup:Field = {
		name:'bloodGroup'
		,controlType: 'Input'
		,label: 'Blood Group'
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	religion:Field = {
		name:'religion'
		,controlType: 'Input'
		,label: 'Religion'
		,isRequired: true
		,listName: 'religion'
		,valueList: [
			{value:'Hindu',text:'Hindu'},
			{value:'Muslim',text:'Muslim'},
			{value:'Christian',text:'Christian'},
			{value:'Sikh',text:'Sikh'},
			{value:'Jain',text:'Jain'},
			{value:'Others',text:'Other'}
			]
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	caste:Field = {
		name:'caste'
		,controlType: 'Dropdown'
		,label: 'Caste'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	nationlity:Field = {
		name:'nationlity'
		,controlType: 'Input'
		,label: 'Nationality'
	};
	category:Field = {
		name:'category'
		,controlType: 'Input'
		,label: 'Category'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	personalId:Field = {
		name:'personalId'
		,controlType: 'Input'
		,label: 'Personal Id'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidUniqueId'
		,minLength: 16
		,maxLength: 16
	};
	dateOfBirth:Field = {
		name:'dateOfBirth'
		,controlType: 'Input'
		,label: 'Date ofBirth'
		,isRequired: true
		,valueType: 4
		,errorId: 'invalidDate'
		,minValue: 73000
		,maxValue: 73000
	};
	placeOfBirth:Field = {
		name:'placeOfBirth'
		,controlType: 'Input'
		,label: 'Place of Birth'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	domicileState:Field = {
		name:'domicileState'
		,controlType: 'Input'
		,label: 'Domicile State'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidState'
		,maxLength: 50
	};
	previousBoard:Field = {
		name:'previousBoard'
		,controlType: 'Input'
		,label: 'Previous Board'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	previousClass:Field = {
		name:'previousClass'
		,controlType: 'Input'
		,label: 'Previous Class'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	previousInstitute:Field = {
		name:'previousInstitute'
		,controlType: 'Input'
		,label: 'Previous Institute'
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	qualifyingExamRank:Field = {
		name:'qualifyingExamRank'
		,controlType: 'Input'
		,label: 'Qualifying Exam'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	createdAt:Field = {
		name:'createdAt'
		,controlType: 'Hidden'
		,valueType: 5
		,errorId: 'invalidTimestamp'
	};
	updatedAt:Field = {
		name:'updatedAt'
		,controlType: 'Hidden'
		,valueType: 5
		,errorId: 'invalidTimestamp'
	};

	guardians: ChildForm = {name:'guardians'
		,form:Guardian.getInstance()
		,isEditable:false
		,label:''
		,minRows:1
		,maxRows:10
		,errorId:null
	};

	public static getInstance(): StudentWithGuardians {
		return StudentWithGuardians._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('studentId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('instituteId', [Validators.max(9999999999999)]);
		this.controls.set('departmentId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('name', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('gender', [Validators.required, Validators.maxLength(10)]);
		this.controls.set('presentAddressLine1', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('presentAddressLine2', [Validators.maxLength(1000)]);
		this.controls.set('presentCity', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('presentState', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('presentPincode', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern('[1-9][0-9]{5}')]);
		this.controls.set('presentCountry', [Validators.required, Validators.max(999)]);
		this.controls.set('addressLine1', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('addressLine2', [Validators.maxLength(1000)]);
		this.controls.set('city', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('state', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('pincode', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern('[1-9][0-9]{5}')]);
		this.controls.set('country', [Validators.required, Validators.max(999)]);
		this.controls.set('phoneNumber', [Validators.required, Validators.maxLength(20)]);
		this.controls.set('email', [Validators.required, Validators.email, Validators.maxLength(1000)]);
		this.controls.set('admissionQuota', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('admittedLevel', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('admissionDate', [Validators.required]);
		this.controls.set('bloodGroup', [Validators.maxLength(1000)]);
		this.controls.set('religion', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('caste', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('category', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('personalId', [Validators.required, Validators.minLength(16), Validators.maxLength(16), Validators.pattern('[1-9][0-9]{15}')]);
		this.controls.set('dateOfBirth', [Validators.required]);
		this.controls.set('placeOfBirth', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('domicileState', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('previousBoard', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('previousClass', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('previousInstitute', [Validators.maxLength(1000)]);
		this.controls.set('qualifyingExamRank', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('createdAt', []);
		this.controls.set('updatedAt', []);

		this.childForms = new Map();
		this.childForms.set('guardians', this.guardians);
		this.listFields = ['gender', 'departmentId', 'presentState', 'state', 'admissionQuota', 'religion', 'programId'];
		this.keyFields = ['studentId'];
	}

	public getName(): string {
		 return 'studentWithGuardians';
	}
}


export interface StudentWithGuardiansData extends Vo {
	presentAddressLine1?: string, 
	presentAddressLine2?: string, 
	country?: number, 
	personalId?: string, 
	gender?: string, 
	city?: string, 
	departmentId?: number, 
	studentId?: number, 
	usn?: string, 
	bloodGroup?: string, 
	previousClass?: string, 
	createdAt?: string, 
	presentState?: string, 
	addressLine1?: string, 
	addressLine2?: string, 
	state?: string, 
	admissionQuota?: string, 
	email?: string, 
	updatedAt?: string, 
	pincode?: string, 
	admissionDate?: string, 
	placeOfBirth?: string, 
	domicileState?: string, 
	previousInstitute?: string, 
	caste?: string, 
	admittedLevel?: string, 
	dateOfBirth?: string, 
	previousBoard?: string, 
	qualifyingExamRank?: string, 
	religion?: string, 
	presentCity?: string, 
	phoneNumber?: string, 
	nationality?: string, 
	presentCountry?: number, 
	name?: string, 
	instituteId?: number, 
	tempUsn?: string, 
	category?: string, 
	programId?: number, 
	presentPincode?: string
}
