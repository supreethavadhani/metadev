
import { Form , Field, ChildForm } from '../form/form';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'

export class Level extends Form {
	private static _instance = new Level();
	levelId:Field = {
		name:'levelId'
		,controlType: 'Hidden'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
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
	levelSeq:Field = {
		name:'levelSeq'
		,controlType: 'Hidden'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidInteger'
		,maxValue: 9999999999999
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

	public static getInstance(): Level {
		return Level._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('levelId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('programId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('instituteId', [Validators.max(9999999999999)]);
		this.controls.set('name', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('code', [Validators.maxLength(50)]);
		this.controls.set('levelSeq', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('createdAt', []);
		this.controls.set('updatedAt', []);
		this.keyFields = ['levelId'];
	}

	public getName(): string {
		 return 'level';
	}
}


export interface LevelData extends Vo {
	createdAt?: string, 
	code?: string, 
	levelId?: number, 
	name?: string, 
	instituteId?: number, 
	levelSeq?: number, 
	programId?: number, 
	updatedAt?: string
}
