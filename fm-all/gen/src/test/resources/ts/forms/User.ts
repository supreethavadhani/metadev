
import { Form , Field, ChildForm } from '../form/form';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'

export class User extends Form {
	private static _instance = new User();
	userId:Field = {
		name:'userId'
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
	trustId:Field = {
		name:'trustId'
		,controlType: 'Hidden'
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	userType:Field = {
		name:'userType'
		,controlType: 'Dropdown'
		,label: 'User Type'
		,isRequired: true
		,listName: 'userType'
		,valueList: [
			{value:'student',text:'student'},
			{value:'staff',text:'staff'},
			{value:'admin',text:'admin'},
			{value:'guardian',text:'guardian'},
			{value:'trustee',text:'trustee'}
			]
		,valueType: 0
		,defaultValue: 'Student'
		,errorId: 'invalidUserType'
		,maxLength: 20
	};
	loginId:Field = {
		name:'loginId'
		,controlType: 'Input'
		,label: 'Login Id'
		,isRequired: true
		,valueType: 0
		,errorId: 'invalidLoginId'
		,maxLength: 50
	};
	loginTokan:Field = {
		name:'loginTokan'
		,controlType: 'Output'
		,label: 'Login Token'
	};
	loginEnabled:Field = {
		name:'loginEnabled'
		,controlType: 'Checkbox'
		,label: 'Login Enabled'
		,isRequired: true
		,valueType: 3
		,defaultValue: false
		,errorId: 'invalidBool'
	};
	resetPasswordCount:Field = {
		name:'resetPasswordCount'
		,controlType: 'Output'
		,label: 'Reset Password Count'
		,valueType: 1
		,errorId: 'invalidInteger'
		,maxValue: 9999999999999
	};
	loginCount:Field = {
		name:'loginCount'
		,controlType: 'Output'
		,label: 'Login Count'
		,valueType: 1
		,errorId: 'invalidInteger'
		,maxValue: 9999999999999
	};
	confirmationToken:Field = {
		name:'confirmationToken'
		,controlType: 'Output'
		,label: 'Confirmation Token'
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	currentLoginIP:Field = {
		name:'currentLoginIP'
		,controlType: 'Output'
		,label: 'Current Login IP'
	};
	country:Field = {
		name:'country'
		,controlType: 'Hidden'
		,label: 'Premanent Country'
	};
	previousLoginIP:Field = {
		name:'previousLoginIP'
		,controlType: 'Output'
		,label: 'Previous Login IP'
	};
	currentLoginAt:Field = {
		name:'currentLoginAt'
		,controlType: 'Output'
		,label: 'CurrentLogin At'
		,valueType: 5
		,errorId: 'invalidTimestamp'
	};
	previousLoginAt:Field = {
		name:'previousLoginAt'
		,controlType: 'Output'
		,label: 'Previous Login At'
		,valueType: 5
		,errorId: 'invalidTimestamp'
	};
	resetPasswordSentAt:Field = {
		name:'resetPasswordSentAt'
		,controlType: 'Output'
		,label: 'Reset Password Sent At'
		,valueType: 5
		,errorId: 'invalidTimestamp'
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

	public static getInstance(): User {
		return User._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('userId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('instituteId', [Validators.max(9999999999999)]);
		this.controls.set('trustId', [Validators.max(9999999999999)]);
		this.controls.set('userType', [Validators.required, Validators.maxLength(20)]);
		this.controls.set('loginId', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('loginEnabled', [Validators.required]);
		this.controls.set('resetPasswordCount', [Validators.max(9999999999999)]);
		this.controls.set('loginCount', [Validators.max(9999999999999)]);
		this.controls.set('confirmationToken', [Validators.maxLength(1000)]);
		this.controls.set('currentLoginAt', []);
		this.controls.set('previousLoginAt', []);
		this.controls.set('resetPasswordSentAt', []);
		this.controls.set('createdAt', []);
		this.controls.set('updatedAt', []);
		this.listFields = ['userType'];
		this.keyFields = ['userId'];
	}

	public getName(): string {
		 return 'user';
	}
}


export interface UserData extends Vo {
	loginId?: string, 
	currentLoginAt?: string, 
	resetPasswordSentAt?: string, 
	loginToken?: string, 
	resetPasswordCount?: number, 
	previousLoginAt?: string, 
	userId?: number, 
	loginCount?: number, 
	previousLoginIp?: string, 
	trustId?: number, 
	createdAt?: string, 
	password?: string, 
	currentLoginIp?: string, 
	instituteId?: number, 
	confirmationToken?: string, 
	loginEnabled?: boolean, 
	userType?: string, 
	updatedAt?: string
}
