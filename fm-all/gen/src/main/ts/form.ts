import { ServerAgent } from './serverAgent';
import { FormData } from './formData';
import { PanelData } from './panelData';
import { Validators } from '@angular/forms';
import { SelectOption } from './types';

/**
 * represents the data model, both structure and run-time data
 */
export abstract class Form {
	/**
	 * key-Field pairs. has all the fields of this form
	 */
	public fields: Map<string, Field>;
	/**
	 * key-Table pairs. has all the tables (sub-forms) in this form
	 */
	public childForms: Map<string, ChildForm>;

	/**
	 * meta-data for all controls that can be used to create a formGroup using formBuilder.group()
	 */
	public controls: Map<string, Validators[]>;

	/**
	 * inter-field validations
	 */
	public validations: Array<{ [key: string]: any }>;

	/**
	 * field names that have enumeratedlist of values. That is,fields that are to be rendered as drop-downs
	 */
	public listFields: string[];

	/**
	 * fields that make up the primary key. generally onl one, but possible to have more than one
	 */
	public keyFields: string[];

	/**
	 * fields that make up a unique key other than the primary key
	 * this is used if keyFields are not provided
	 */
	public uniqueFields: string[];
	/**
	 * what operations are allowed on this form.
	 */
	public opsAllowed: { [key: string]: boolean };


	public constructor() {
	}
	/**
	 * name of this form. 
	 */
	public abstract getName(): string;

	/**
	 * create a model (data-holder) for this form
	 * @param serverAgent is injectable 
	 */
	public newFormData(serverAgent: ServerAgent): FormData {
		return new FormData(this, serverAgent);
	}

	/**
	 * create a model (data-holder) for this form
	 * @param serverAgent is injectable 
	 */
	public newPanelData(serverAgent: ServerAgent): PanelData {
		return new PanelData(this, serverAgent);
	}
	/**
	 * 
	 * @param operation 
	 * @returns service name of the form "operation-formName", say filter-costomer
	 */
    public getServiceName(operation: string): string {
        if (this.opAllowed(operation)) {
            return operation + '-' + this.getName();
        }
        console.error('Operation ' + operation + ' not allowed on form ' + this.getName());
        return null;
    }

	/**
	 * 
	 * @param operation 
	 * @returns true of this operation is designed for this form
	 */
    public opAllowed(operation: string): boolean {
        if (this.opsAllowed[operation]) {
            return true;
        }
        console.error('Form ', this.getName(), ' is not designed for ', operation, ' operation');
        return false;
    }
}

export interface ChildForm {
	name: string;
	form: Form;
	isTabular: boolean;

	label?: string;
	minRows?: number;
	maxRows?: number;
	errorId?: string;
	isEditable?: boolean;
}

export interface Field {
	/** 
	 * required attributes
	 */
	name: string;
	/**
	 * 0-text, 1-integer, 2-decimal, 3-boolean, 4-date, 5-timestamp
	 */
	valueType: 0 | 1 | 2 | 3 | 4 | 5;

	/**
	 * optional attributes
	 */
	defaultValue?: string;
	label?: string;
	altLabel?: string;
	placeHolder?: string;
	trueLabel?: string;
	falseLabel?: string;
	isEditable?: boolean;
	errorId?: string;
	isRequired?: boolean;
	minLength?: number;
	maxLength?: number;
	regex?: string;
	minValue?: number;
	maxValue?: number;
	nbrFractions?: number;
	nbrDecimals?: number;
	listName?: string;
	listKey?: string;
	valueList?: SelectOption[];
	keyedList?: { [key: string]: SelectOption[] };
}

