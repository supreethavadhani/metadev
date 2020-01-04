
import { Form , Field, ChildForm } from '../form/form';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'

export class Institute extends Form {
	private static _instance = new Institute();
	instituteId:Field = {
		name:'instituteId'
		,controlType: 'Hidden'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	trustId:Field = {
		name:'trustId'
		,controlType: 'Hidden'
		,isRequired: true
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
	instituteType:Field = {
		name:'instituteType'
		,controlType: 'Input'
		,label: 'Institute Type'
		,isRequired: true
		,listName: 'instituteType'
		,valueList: [
			{value:'DSERTPS',text:'Karnataka State Syllabus Primary School'},
			{value:'DSERTPHS',text:'Karnataka State Syllabus Higher School'},
			{value:'CBSE ',text:'CBSE'},
			{value:'ENG_A_VTU',text:'Engineering College (Automonus Under VTU)'},
			{value:'ENG_VTU',text:'Engineering College ( VTU)'}
			]
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	addressLine1:Field = {
		name:'addressLine1'
		,controlType: 'Input'
		,label: 'Address Line1'
		,isRequired: true
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
	govtCode:Field = {
		name:'govtCode'
		,controlType: 'Input'
		,label: 'Government Code'
		,valueType: 0
		,errorId: 'invalidGovtCode'
		,maxLength: 50
	};
	phoneNumber:Field = {
		name:'phoneNumber'
		,controlType: 'Input'
		,label: 'Phone Number'
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
	isInactive:Field = {
		name:'isInactive'
		,controlType: 'Input'
		,label: 'Is Inactive'
		,valueType: 3
		,errorId: 'invalidBool'
	};

	public static getInstance(): Institute {
		return Institute._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('instituteId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('trustId', [Validators.required, Validators.max(9999999999999)]);
		this.controls.set('name', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('instituteType', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('addressLine1', [Validators.required, Validators.maxLength(1000)]);
		this.controls.set('addressLine2', [Validators.maxLength(1000)]);
		this.controls.set('city', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('state', [Validators.required, Validators.maxLength(50)]);
		this.controls.set('pincode', [Validators.required, Validators.minLength(6), Validators.maxLength(6), Validators.pattern('[1-9][0-9]{5}')]);
		this.controls.set('country', [Validators.required, Validators.max(999)]);
		this.controls.set('govtCode', [Validators.maxLength(50)]);
		this.controls.set('phoneNumber', [Validators.required, Validators.maxLength(20)]);
		this.controls.set('email', [Validators.required, Validators.email, Validators.maxLength(1000)]);
		this.controls.set('createdAt', []);
		this.controls.set('updatedAt', []);
		this.controls.set('isInactive', []);
		this.listFields = ['state', 'instituteType'];
		this.keyFields = ['instituteId'];
	}

	public getName(): string {
		 return 'institute';
	}
}


export interface InstituteData extends Vo {
	pincode?: string, 
	country?: number, 
	city?: string, 
	isInactive?: boolean, 
	trustId?: number, 
	createdAt?: string, 
	phoneNumber?: string, 
	name?: string, 
	govtCode?: string, 
	instituteId?: number, 
	addressLine1?: string, 
	addressLine2?: string, 
	state?: string, 
	instituteType?: string, 
	email?: string, 
	updatedAt?: string
}
