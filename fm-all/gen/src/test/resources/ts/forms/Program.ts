
import { Form , Field, ChildForm } from '../form/form';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'

export class Program extends Form {
	private static _instance = new Program();
	programId:Field = {
		name:'programId'
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
		,controlType: 'Hidden'
		,isRequired: true
		,listName: 'departmentList'
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
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
	code:Field = {
		name:'code'
		,controlType: 'Input'
		,label: 'Code'
		,valueType: 0
		,errorId: 'invalidCode'
		,maxLength: 50
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

	public static getInstance(): Program {
		return Program._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('programId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('instituteId', [Validators.max(9999999999999)]);
		this.controls.set('departmentId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('name', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('code', [Validators.maxLength(50)]);
		this.controls.set('createdAt', []);
		this.controls.set('updatedAt', []);
		this.listFields = ['departmentId'];
		this.keyFields = ['programId'];
	}

	public getName(): string {
		 return 'program';
	}
}


export interface ProgramData extends Vo {
	createdAt?: string, 
	code?: string, 
	departmentId?: number, 
	name?: string, 
	instituteId?: number, 
	programId?: number, 
	updatedAt?: string
}
