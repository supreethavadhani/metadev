
import { Form , Field, ChildForm } from '../form/form';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'

export class Department extends Form {
	private static _instance = new Department();
	departmentId:Field = {
		name:'departmentId'
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
	addressLine1:Field = {
		name:'addressLine1'
		,controlType: 'Input'
		,label: 'Address Line1'
		,valueType: 0
		,errorId: 'invalidDesc'
		,maxLength: 1000
	};
	addressLine2:Field = {
		name:'addressLine2'
		,controlType: 'Input'
		,label: 'Address Line2'
		,valueType: 0
		,errorId: 'invalidDesc'
		,maxLength: 1000
	};
	city:Field = {
		name:'city'
		,controlType: 'Input'
		,label: 'City'
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	state:Field = {
		name:'state'
		,controlType: 'Dropdown'
		,label: 'State'
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
		,valueType: 0
		,errorId: 'invalidPin'
		,minLength: 6
		,maxLength: 6
	};
	country:Field = {
		name:'country'
		,controlType: 'Hidden'
		,label: 'Country'
		,valueType: 1
		,defaultValue: 130
		,errorId: 'invalidCountry'
		,maxValue: 999
	};
	govtCode:Field = {
		name:'govtCode'
		,controlType: 'Input'
		,label: 'Government Code'
	};
	phoneNumber:Field = {
		name:'phoneNumber'
		,controlType: 'Input'
		,label: 'Phone Number'
		,valueType: 0
		,errorId: 'invalidPhone'
		,maxLength: 20
	};
	email:Field = {
		name:'email'
		,controlType: 'Input'
		,label: 'Email'
		,valueType: 0
		,errorId: 'invalidEmail'
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

	public static getInstance(): Department {
		return Department._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('departmentId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('instituteId', [Validators.max(9999999999999)]);
		this.controls.set('name', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('code', [Validators.maxLength(50)]);
		this.controls.set('addressLine1', [Validators.maxLength(1000)]);
		this.controls.set('addressLine2', [Validators.maxLength(1000)]);
		this.controls.set('city', [Validators.maxLength(50)]);
		this.controls.set('state', [Validators.maxLength(50)]);
		this.controls.set('pincode', [Validators.minLength(6), Validators.maxLength(6), Validators.pattern('[1-9][0-9]{5}')]);
		this.controls.set('country', [Validators.max(999)]);
		this.controls.set('phoneNumber', [Validators.maxLength(20)]);
		this.controls.set('email', [Validators.email, Validators.maxLength(1000)]);
		this.controls.set('createdAt', []);
		this.controls.set('updatedAt', []);
		this.listFields = ['state'];
		this.keyFields = ['departmentId'];
	}

	public getName(): string {
		 return 'department';
	}
}


export interface DepartmentData extends Vo {
	pincode?: string, 
	country?: number, 
	code?: string, 
	city?: string, 
	departmentId?: number, 
	createdAt?: string, 
	phoneNumber?: string, 
	name?: string, 
	instituteId?: number, 
	addressLine1?: string, 
	addressLine2?: string, 
	state?: string, 
	email?: string, 
	updatedAt?: string
}
