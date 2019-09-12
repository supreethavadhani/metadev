import { FormControl, ValidatorFn } from '@angular/forms';
import { SelectOption, Vo } from './types';
import { Form, Field } from './form';
import { ServerAgent } from './serverAgent';
import { PanelData } from './panelData';
import { Conventions } from './conventions';

/**
 * represents the data contained in a form. Manages two-way binding with input fields in the form
 */
export class FormData extends PanelData {
	/**
	 * list of options/values for all drop-downs in this form. 
     * html components should bind the drop-downs to a member in this 
	 */
    lists: { [key: string]: SelectOption[] };

    constructor(f: Form, sa: ServerAgent) {
        super(f, sa);
 
        if (this.form.controls) {
            Object.keys(this.form.controls).forEach(key => {
                const arr = this.form.controls[key] as [string, ValidatorFn[]];
                this.formGroup.addControl(key, new FormControl(arr[0], arr[1]));
            });
        }
 
        this.handleDropDowns(f);
    }

    /**
	 * set drop-down list of values for a field. 
	 * it may be available locally, or we my have to get it from the server
	 * @param field for which drop-down list id to be fetched
     * @param key value of the key field,if this is a keyed-list
	 */
    public setListValues(field: Field, key: string): void {
        if (field.keyedList) {
            /*
             * design-time list. locally avaliable
             */
            let arr = field.keyedList[key];
            if (!arr) {
                console.error('Design time list of values for drop-down not available for key=' + key);
                arr = [];
            }

            this.lists[field.name] = arr;
            return;
        }

        /**
         * we have to ask the server to get this
         */
        let data: any;
        if (key) {
            data = { list: field.listName, key: key };
        } else {
            data = { list: field.listName };
        }

        const obs = this.serverAgent.serve(Conventions.LIST_SERVICE, data, true);
        obs.subscribe(vo => {
            const arr = vo['list'] as SelectOption[];
            console.log('list values received for field ' + field.name + ' with ' + (arr && arr.length) + ' values');
            this.lists[field.name] = arr;
        }, msgs => {
            console.log('Error while receiving list values for field ' + field.name + JSON.stringify(msgs));
        });
    }

    private handleDropDowns(f: Form): void {
        if (!f.listFields) {
            return null;
        }
        this.lists = {};
        f.listFields.forEach(nam => {
            const field = f.fields.get(nam);
            if (field.valueList) {
                console.log('Field ' + nam + ' found design-time list with ' + field.valueList.length + ' entries in ti');
                this.lists[nam] = field.valueList;
            } else {
                this.lists[nam] = [];
                if (field.listKey) {
                    //register on-change for the parent field
                    console.log('Field ' + nam + '  is based on ' + field.listKey + '. Hence we just added a trigger');
                    const fc = this.formGroup.get(field.listKey) as FormControl;
                    fc.valueChanges.subscribe((value: string) => this.setListValues(field, value));
                } else {
                    console.log('Field ' + nam + '  is not key-based and not design-time. We will make a call to the server.');
                    //fixed list, but we have to get it from server at run time
                    this.setListValues(field, null);
                }
            }
        });
    }

    public validateForm(): boolean {
        this.formGroup.updateValueAndValidity();
        if (!this.formGroup.valid) {
            return false;
        }
        const vals = this.form.validations;
        let allOk = true;
        if (vals) {
            for (const v of this.form.validations) {
                const c1 = this.formGroup.get(v.f1) as FormControl;
                const c2 = this.formGroup.get(v.f2) as FormControl;
                const t = v.type;
                let ok: boolean;
                if (t === 'range') {
                    ok = this.validateRange(c1.value, c2.value, v.isStrict);
                } else if (t === 'incl') {
                    ok = this.validateInclPair(c1.value, c2.value, v.value);
                } else if (t === 'excl') {
                    ok = this.validateExclPair(c1.value, c2.value, v.atLeastOne);
                } else {
                    console.error('Form validation type ' + t + ' is not valid. validation ignored');
                    ok = true;
                }
                if (!ok) {
                    const err = { interfield: t, errorId: v.errorId }
                    c1.setErrors(err);
                    c2.setErrors(err);
                    allOk = false;
                }
            }
        }
        this.childData.forEach((fd, key) => {
            const b = fd.validateForm();
            allOk = allOk && b;
        });
        this.childTabularData.forEach((table, key) => {
            const b = table.validateForm();
            allOk = allOk && b;
        });
        return allOk;
    }
	/**
	 * check if v1 to v2 us a range
	 * @param v1 
	 * @param v2 
	 * @param useStrict if true, v2 must be > v2, v1 == v2 woudn't cut
	 */
    private validateRange(v1: string, v2: string, equalOk: boolean): boolean {
        const n1 = Number.parseFloat(v1);
        const n2 = Number.parseFloat(v2);
        if (n1 === NaN || n2 === NaN || n2 > n1) {
            return true;
        }
        if (n1 > n2) {
            return false;
        }
        //equal. is it ok?
        return equalOk;
    }

	/**
	 * two fields have to be both specified or both skipped.
	 * if value is specified, it means that the rule is applicable if v1 == value
	 * @param v1 
	 * @param v2 
	 * @param value 
	 */
    private validateInclPair(v1: string, v2: string, value: string): boolean {
		/*
		 * we assume v1 is specified when a value is given. 
		 * However, if value is specified, then it has to match it' 
		 */
        const v1Specified = v1 && (!value || value == v1);
        if (v1Specified) {
            if (v2) {
                return true;
            }
            return false;
        }
        // v1 is not specified, so v2 should not be specified
        if (v2) {
            return false;
        }
        return true;
    }

	/**
	 * 
	 * @param errorId v1 and v2 are exclusive
	 * @param primaryField 
	 * @param otherField 
	 * @param atLeastOne if true, exactly one of teh twoto be specified
	 */
    private validateExclPair(v1: string, v2: string, noneOk: boolean): boolean {
        if (v1) {
            if (v2) {
                return false;
            }
            return true;
        }
        if (v2) {
            return true;
        }
        //none specifield, is it ok?
        return noneOk;
    }

}