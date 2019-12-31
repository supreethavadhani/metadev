
import { Form , Field, ChildForm } from '../form/form';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'

export class Section extends Form {
	private static _instance = new Section();
	sectionId:Field = {
		name:'sectionId'
		,controlType: 'Hidden'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	instituteId:Field = {
		name:'instituteId'
		,controlType: 'Hidden'
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

	public static getInstance(): Section {
		return Section._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('sectionId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('name', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('code', [Validators.maxLength(50)]);
		this.controls.set('createdAt', []);
		this.controls.set('updatedAt', []);
		this.keyFields = ['sectionId'];
	}

	public getName(): string {
		 return 'section';
	}
}


export interface SectionData extends Vo {
	name?: string, 
	createdAt?: string, 
	code?: string, 
	sectionId?: number, 
	updatedAt?: string
}
