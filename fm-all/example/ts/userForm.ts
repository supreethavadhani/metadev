
import { Form , Field, ChildForm } from '../form/form';
import { FormData } from '../form/formData';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'
import { ServiceAgent} from '../form/serviceAgent';

export class UserForm extends Form {
	private static _instance = new UserForm();
	userId:Field = {
		name:'userId'
		,controlType: 'Hidden'
		,label: 'userId'
		,valueType: 1
		,errorId: 'invalidFlexibleId'
		,minValue: -1
		,maxValue: 9999999999999
	};
	instituteId:Field = {
		name:'instituteId'
		,controlType: 'Hidden'
		,label: 'instituteId'
		,valueType: 1
		,errorId: 'invalidTenentKey'
		,maxValue: 9999999999999
	};
	trustId:Field = {
		name:'trustId'
		,controlType: 'Hidden'
		,label: 'trustId'
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	userType:Field = {
		name:'userType'
		,controlType: 'Dropdown'
		,label: 'User Type'
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
		,valueType: 0
		,errorId: 'invalidLoginId'
		,maxLength: 50
	};
	loginToken:Field = {
		name:'loginToken'
		,controlType: 'Output'
		,label: 'Login Token'
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	loginEnabled:Field = {
		name:'loginEnabled'
		,controlType: 'Checkbox'
		,label: 'Login Enabled'
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
	currentLoginIp:Field = {
		name:'currentLoginIp'
		,controlType: 'Output'
		,label: 'Current Login IP'
		,valueType: 0
		,errorId: 'invalidIp'
		,maxLength: 1000
	};
	previousLoginIp:Field = {
		name:'previousLoginIp'
		,controlType: 'Output'
		,label: 'Previous Login IP'
		,valueType: 0
		,errorId: 'invalidIp'
		,maxLength: 1000
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
		,label: 'createdAt'
		,valueType: 5
		,errorId: 'invalidTimestamp'
	};
	updatedAt:Field = {
		name:'updatedAt'
		,controlType: 'Hidden'
		,label: 'updatedAt'
		,valueType: 5
		,errorId: 'invalidTimestamp'
	};

	public static getInstance(): UserForm {
		return UserForm._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('userId', [Validators.min(-1), Validators.max(9999999999999)]);
		this.fields.set('userId', this.userId);
		this.controls.set('instituteId', [Validators.max(9999999999999)]);
		this.fields.set('instituteId', this.instituteId);
		this.controls.set('trustId', [Validators.max(9999999999999)]);
		this.fields.set('trustId', this.trustId);
		this.controls.set('userType', [Validators.maxLength(20)]);
		this.fields.set('userType', this.userType);
		this.controls.set('loginId', [Validators.maxLength(50)]);
		this.fields.set('loginId', this.loginId);
		this.controls.set('loginToken', [Validators.maxLength(1000)]);
		this.fields.set('loginToken', this.loginToken);
		this.controls.set('loginEnabled', []);
		this.fields.set('loginEnabled', this.loginEnabled);
		this.controls.set('resetPasswordCount', [Validators.max(9999999999999)]);
		this.fields.set('resetPasswordCount', this.resetPasswordCount);
		this.controls.set('loginCount', [Validators.max(9999999999999)]);
		this.fields.set('loginCount', this.loginCount);
		this.controls.set('confirmationToken', [Validators.maxLength(1000)]);
		this.fields.set('confirmationToken', this.confirmationToken);
		this.controls.set('currentLoginIp', [Validators.maxLength(1000)]);
		this.fields.set('currentLoginIp', this.currentLoginIp);
		this.controls.set('previousLoginIp', [Validators.maxLength(1000)]);
		this.fields.set('previousLoginIp', this.previousLoginIp);
		this.controls.set('currentLoginAt', []);
		this.fields.set('currentLoginAt', this.currentLoginAt);
		this.controls.set('previousLoginAt', []);
		this.fields.set('previousLoginAt', this.previousLoginAt);
		this.controls.set('resetPasswordSentAt', []);
		this.fields.set('resetPasswordSentAt', this.resetPasswordSentAt);
		this.controls.set('createdAt', []);
		this.fields.set('createdAt', this.createdAt);
		this.controls.set('updatedAt', []);
		this.fields.set('updatedAt', this.updatedAt);
		this.opsAllowed = {get: true, filter: true};
		this.listFields = ['userType'];
	}

	public getName(): string {
		 return 'user';
	}
}


export class UserFd extends FormData {
	constructor(form: UserForm, sa: ServiceAgent) {
		super(form, sa);
	}

	setFieldValue(name: 'userId' | 'instituteId' | 'trustId' | 'userType' | 'loginId' | 'loginToken' | 'loginEnabled' | 'resetPasswordCount' | 'loginCount' | 'confirmationToken' | 'currentLoginIp' | 'previousLoginIp' | 'currentLoginAt' | 'previousLoginAt' | 'resetPasswordSentAt' | 'createdAt' | 'updatedAt', value: string | number | boolean | null ): void {
		super.setFieldValue(name, value);
	}

	getFieldValue(name: 'userId' | 'instituteId' | 'trustId' | 'userType' | 'loginId' | 'loginToken' | 'loginEnabled' | 'resetPasswordCount' | 'loginCount' | 'confirmationToken' | 'currentLoginIp' | 'previousLoginIp' | 'currentLoginAt' | 'previousLoginAt' | 'resetPasswordSentAt' | 'createdAt' | 'updatedAt' ): string | number | boolean | null {
		return super.getFieldValue(name);
	}
}


export interface UserVo extends Vo {
	loginId?: string, 
	updatedBy?: number, 
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
	createdBy?: number, 
	instituteId?: number, 
	confirmationToken?: string, 
	loginEnabled?: boolean, 
	userType?: string, 
	updatedAt?: string
}
