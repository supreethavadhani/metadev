
import { Form , Field, ChildForm } from '../form/form';
import { FormData } from '../form/formData';
import { SelectOption, Vo } from '../form/types';
import { Validators } from '@angular/forms'
import { ServiceAgent} from '../form/serviceAgent';

export class StudentCieDetailForm extends Form {
	private static _instance = new StudentCieDetailForm();
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
	subjectSectionId:Field = {
		name:'subjectSectionId'
		,controlType: 'Hidden'
		,label: 'subjectSectionId'
		,valueType: 1
		,errorId: 'invalidId'
		,maxValue: 9999999999999
	};
	departmentName:Field = {
		name:'departmentName'
		,controlType: 'Output'
		,label: 'Department Name'
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	name:Field = {
		name:'name'
		,controlType: 'Output'
		,label: 'Student Name'
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	usn:Field = {
		name:'usn'
		,controlType: 'Output'
		,label: 'USN'
		,valueType: 0
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	eligibility:Field = {
		name:'eligibility'
		,controlType: 'Output'
		,label: 'Eligibility'
		,valueType: 0
		,errorId: 'invalidName'
		,maxLength: 50
	};
	test1:Field = {
		name:'test1'
		,controlType: 'Output'
		,label: 'Test 1'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	test2:Field = {
		name:'test2'
		,controlType: 'Output'
		,label: 'Test 2'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	test3:Field = {
		name:'test3'
		,controlType: 'Output'
		,label: 'Test 3'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	quiz1:Field = {
		name:'quiz1'
		,controlType: 'Output'
		,label: 'Quiz 1'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	quiz2:Field = {
		name:'quiz2'
		,controlType: 'Output'
		,label: 'Quiz 2'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	theoryCie:Field = {
		name:'theoryCie'
		,controlType: 'Output'
		,label: 'Theory Marks'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	theoryClassesHeld:Field = {
		name:'theoryClassesHeld'
		,controlType: 'Output'
		,label: 'theory Classes Held'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	theoryClassesAttended:Field = {
		name:'theoryClassesAttended'
		,controlType: 'Output'
		,label: 'theory Classes Attended'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	theoryClassesPercentage:Field = {
		name:'theoryClassesPercentage'
		,controlType: 'Output'
		,label: 'theory Classes Percentage'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	practicalCie:Field = {
		name:'practicalCie'
		,controlType: 'Output'
		,label: 'Practicals Cie'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	practicalClassesHeld:Field = {
		name:'practicalClassesHeld'
		,controlType: 'Output'
		,label: 'Practical Classes Held'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	practicalClassesAttended:Field = {
		name:'practicalClassesAttended'
		,controlType: 'Output'
		,label: 'Practical Classes Attended'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	practicalClassesPercentage:Field = {
		name:'practicalClassesPercentage'
		,controlType: 'Output'
		,label: 'Practical Classes Percentage'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	selfStudy:Field = {
		name:'selfStudy'
		,controlType: 'Output'
		,label: 'Self Study'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	practicalMarks:Field = {
		name:'practicalMarks'
		,controlType: 'Output'
		,label: 'Practicals Marks'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};
	totalCie:Field = {
		name:'totalCie'
		,controlType: 'Output'
		,label: 'Total Cie'
		,valueType: 0
		,defaultValue: '1'
		,errorId: 'invalidText'
		,maxLength: 1000
	};

	public static getInstance(): StudentCieDetailForm {
		return StudentCieDetailForm._instance;
	}

	constructor() {
		super();
		this.fields = new Map();
		this.controls = new Map();
		this.controls.set('studentId', [Validators.min(-1), Validators.max(9999999999999)]);
		this.fields.set('studentId', this.studentId);
		this.controls.set('subjectSectionId', [Validators.max(9999999999999)]);
		this.fields.set('subjectSectionId', this.subjectSectionId);
		this.controls.set('departmentName', [Validators.maxLength(1000)]);
		this.fields.set('departmentName', this.departmentName);
		this.controls.set('name', [Validators.maxLength(50)]);
		this.fields.set('name', this.name);
		this.controls.set('usn', [Validators.maxLength(1000)]);
		this.fields.set('usn', this.usn);
		this.controls.set('eligibility', [Validators.maxLength(50)]);
		this.fields.set('eligibility', this.eligibility);
		this.controls.set('test1', [Validators.maxLength(1000)]);
		this.fields.set('test1', this.test1);
		this.controls.set('test2', [Validators.maxLength(1000)]);
		this.fields.set('test2', this.test2);
		this.controls.set('test3', [Validators.maxLength(1000)]);
		this.fields.set('test3', this.test3);
		this.controls.set('quiz1', [Validators.maxLength(1000)]);
		this.fields.set('quiz1', this.quiz1);
		this.controls.set('quiz2', [Validators.maxLength(1000)]);
		this.fields.set('quiz2', this.quiz2);
		this.controls.set('theoryCie', [Validators.maxLength(1000)]);
		this.fields.set('theoryCie', this.theoryCie);
		this.controls.set('theoryClassesHeld', [Validators.maxLength(1000)]);
		this.fields.set('theoryClassesHeld', this.theoryClassesHeld);
		this.controls.set('theoryClassesAttended', [Validators.maxLength(1000)]);
		this.fields.set('theoryClassesAttended', this.theoryClassesAttended);
		this.controls.set('theoryClassesPercentage', [Validators.maxLength(1000)]);
		this.fields.set('theoryClassesPercentage', this.theoryClassesPercentage);
		this.controls.set('practicalCie', [Validators.maxLength(1000)]);
		this.fields.set('practicalCie', this.practicalCie);
		this.controls.set('practicalClassesHeld', [Validators.maxLength(1000)]);
		this.fields.set('practicalClassesHeld', this.practicalClassesHeld);
		this.controls.set('practicalClassesAttended', [Validators.maxLength(1000)]);
		this.fields.set('practicalClassesAttended', this.practicalClassesAttended);
		this.controls.set('practicalClassesPercentage', [Validators.maxLength(1000)]);
		this.fields.set('practicalClassesPercentage', this.practicalClassesPercentage);
		this.controls.set('selfStudy', [Validators.maxLength(1000)]);
		this.fields.set('selfStudy', this.selfStudy);
		this.controls.set('practicalMarks', [Validators.maxLength(1000)]);
		this.fields.set('practicalMarks', this.practicalMarks);
		this.controls.set('totalCie', [Validators.maxLength(1000)]);
		this.fields.set('totalCie', this.totalCie);
		this.opsAllowed = {get: true, create: true, update: true, filter: true};
	}

	public getName(): string {
		 return 'studentCieDetail';
	}
}


export class StudentCieDetailFd extends FormData {
	constructor(form: StudentCieDetailForm, sa: ServiceAgent) {
		super(form, sa);
	}

	setFieldValue(name: 'studentId' | 'subjectSectionId' | 'departmentName' | 'name' | 'usn' | 'eligibility' | 'test1' | 'test2' | 'test3' | 'quiz1' | 'quiz2' | 'theoryCie' | 'theoryClassesHeld' | 'theoryClassesAttended' | 'theoryClassesPercentage' | 'practicalCie' | 'practicalClassesHeld' | 'practicalClassesAttended' | 'practicalClassesPercentage' | 'selfStudy' | 'practicalMarks' | 'totalCie', value: string | number | boolean | null ): void {
		super.setFieldValue(name, value);
	}

	getFieldValue(name: 'studentId' | 'subjectSectionId' | 'departmentName' | 'name' | 'usn' | 'eligibility' | 'test1' | 'test2' | 'test3' | 'quiz1' | 'quiz2' | 'theoryCie' | 'theoryClassesHeld' | 'theoryClassesAttended' | 'theoryClassesPercentage' | 'practicalCie' | 'practicalClassesHeld' | 'practicalClassesAttended' | 'practicalClassesPercentage' | 'selfStudy' | 'practicalMarks' | 'totalCie' ): string | number | boolean | null {
		return super.getFieldValue(name);
	}
}


export interface StudentCieDetailVo extends Vo {
	departmentName?: string, 
	theoryCie?: string, 
	practicalCie?: string, 
	practicalClassesHeld?: string, 
	eligibility?: string, 
	theoryClassesPercentage?: string, 
	totalCie?: string, 
	test1?: string, 
	theoryClassesAttended?: string, 
	studentId?: number, 
	practicalMarks?: string, 
	usn?: string, 
	test2?: string, 
	quiz2?: string, 
	practicalClassesAttended?: string, 
	test3?: string, 
	quiz1?: string, 
	subjectSectionId?: number, 
	selfStudy?: string, 
	name?: string, 
	theoryClassesHeld?: string, 
	practicalClassesPercentage?: string
}
