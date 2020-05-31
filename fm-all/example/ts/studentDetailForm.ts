
import { Form , Field, ChildForm } from '../form/form';
import { FormData } from '../form/formData';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'
import { ServiceAgent} from '../form/serviceAgent';

export class StudentDetailForm extends Form {
	private static _instance = new StudentDetailForm();
	studentId:Field = {
		name:'studentId'
		,controlType: 'Hidden'
		,label: 'studentId'
		,valueType: 1
		,defaultValue: -1
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
	departmentId:Field = {
		name:'departmentId'
		,controlType: 'Dropdown'
		,label: 'Department'
		,listName: 'departmentList'
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	departmentName:Field = {
		name:'departmentName'
		,controlType: 'Input'
		,label: 'Department Name'
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	name:Field = {
		name:'name'
		,controlType: 'Input'
		,label: 'Level'
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	usn:Field = {
		name:'usn'
		,controlType: 'Input'
		,label: 'USN'
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	phoneNumber:Field = {
		name:'phoneNumber'
		,controlType: 'Input'
		,label: 'Phone'
		,valueType: 0
		,errorId: 'invalidPhone'
		,minLength: 10
		,maxLength: 12
	};

	public static getInstance(): StudentDetailForm {
		return StudentDetailForm._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('studentId', [Validators.min(-1), Validators.max(9999999999999)]);
		this.fields.set('studentId', this.studentId);
		this.controls.set('instituteId', [Validators.max(9999999999999)]);
		this.fields.set('instituteId', this.instituteId);
		this.controls.set('departmentId', [Validators.max(9999999999999)]);
		this.fields.set('departmentId', this.departmentId);
		this.controls.set('departmentName', [Validators.maxLength(1000)]);
		this.fields.set('departmentName', this.departmentName);
		this.controls.set('name', [Validators.maxLength(50)]);
		this.fields.set('name', this.name);
		this.controls.set('usn', [Validators.maxLength(1000)]);
		this.fields.set('usn', this.usn);
		this.controls.set('phoneNumber', [Validators.minLength(10), Validators.maxLength(12), Validators.pattern('[1-9][0-9]*')]);
		this.fields.set('phoneNumber', this.phoneNumber);
		this.opsAllowed = {get: true, create: true, update: true, filter: true};
		this.listFields = ['departmentId'];
	}

	public getName(): string {
		 return 'studentDetail';
	}
}


export class StudentDetailFd extends FormData {
	constructor(form: StudentDetailForm, sa: ServiceAgent) {
		super(form, sa);
	}

	setFieldValue(name: 'studentId' | 'instituteId' | 'departmentId' | 'departmentName' | 'name' | 'usn' | 'phoneNumber', value: string | number | boolean | null ): void {
		super.setFieldValue(name, value);
	}

	getFieldValue(name: 'studentId' | 'instituteId' | 'departmentId' | 'departmentName' | 'name' | 'usn' | 'phoneNumber' ): string | number | boolean | null {
		return super.getFieldValue(name);
	}
}


export interface StudentDetailVo extends Vo {
	studentId?: number, 
	departmentName?: string, 
	usn?: string, 
	phoneNumber?: string, 
	departmentId?: number, 
	name?: string, 
	instituteId?: number
}
