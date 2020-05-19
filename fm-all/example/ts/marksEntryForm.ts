
import { Form , Field, ChildForm } from '../form/form';
import { FormData } from '../form/formData';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'
import { ServiceAgent} from '../form/serviceAgent';
import { MarksForAssessmentForm, MarksForAssessmentVo } from './marksForAssessmentForm';

export class MarksEntryForm extends Form {
	private static _instance = new MarksEntryForm();
	subjectSectionId:Field = {
		name:'subjectSectionId'
		,controlType: 'Hidden'
		,label: 'subjectSectionId'
		,isRequired: true
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	subjectName:Field = {
		name:'subjectName'
		,controlType: 'Output'
		,label: 'subjectName'
		,valueType: 0
		,defaultValue: ''
		,errorId: 'invalidName'
		,maxLength: 50
	};
	sectionName:Field = {
		name:'sectionName'
		,controlType: 'Output'
		,label: 'sectionName'
		,valueType: 0
		,defaultValue: ''
		,errorId: 'invalidName'
		,maxLength: 50
	};

	students: ChildForm = {
		name:'students',
		form:MarksForAssessmentForm.getInstance(),
		isEditable:false,
		isTabular:true,
		label:'',
		minRows:1,
		maxRows:0,
		errorId:null
	};

	public static getInstance(): MarksEntryForm {
		return MarksEntryForm._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('subjectSectionId', [Validators.required, Validators.max(9999999999999)]);
		this.fields.set('subjectSectionId', this.subjectSectionId);
		this.controls.set('subjectName', [Validators.maxLength(50)]);
		this.fields.set('subjectName', this.subjectName);
		this.controls.set('sectionName', [Validators.maxLength(50)]);
		this.fields.set('sectionName', this.sectionName);

		this.childForms = new Map();
		this.childForms.set('students', this.students);
		this.opsAllowed = {get: true, update: true};
		this.keyFields = ["subjectSectionId"];
	}

	public getName(): string {
		 return 'marksEntry';
	}
}


export class MarksEntryFd extends FormData {
	constructor(form: MarksEntryForm, sa: ServiceAgent) {
		super(form, sa);
	}

	setFieldValue(name: 'subjectSectionId' | 'subjectName' | 'sectionName', value: string | number | boolean | null ): void {
		super.setFieldValue(name, value);
	}

	getFieldValue(name: 'subjectSectionId' | 'subjectName' | 'sectionName' ): string | number | boolean | null {
		return super.getFieldValue(name);
	}
}


export interface MarksEntryVo extends Vo {
	levelSectionId?: number, 
	departmentId?: number, 
	attendanceFrozen?: boolean, 
	cieFrozen?: boolean, 
	sectionId?: number, 
	subjectId?: number, 
	sectionName?: string, 
	offeredSubjectId?: number, 
	isOffered?: boolean, 
	totalClasses?: number, 
	subjectSectionId?: number, 
	instituteId?: number, 
	subjectCode?: string, 
	subjectName?: string, 
	students?: MarksForAssessmentVo
}
