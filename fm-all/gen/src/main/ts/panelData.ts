import { Form, ChildForm } from './form';
import { Message, Vo, FieldValues, FilterRequest } from './types';
import { ServerAgent } from './serverAgent';
import { Conventions } from './conventions';
import { FormGroup, FormArray } from '@angular/forms';
import { TabularData } from './tabularData';
import { FormData } from './formData';

export class PanelData {
    /**
    * data as received from the server
    */
    data: Vo = {};
	/**
	 * data for child forms that ane non-tabular
 	 */
    childData: Map<string, PanelData | FormData> = new Map();
    /** 
    * data for child forms that are tabukar
    */
    childTabularData: Map<string, TabularData> = new Map();
    /**
     * set to true when a service is requested from the server.
     * this can be used by the view-component to indicate aciton 
     */
    waitingForServerResponse: boolean = false;
    /**
       * errors returned by the server
       */
    errors: string[] = [];
    /**
     * warnings returned by the server
     */
    warnings: string[] = [];

    /**
     * informations messages received by the server
     */
    info: string[] = [];

    /**
     * form controls for fields/children. empty if this panel is not editable
     */
    formGroup: FormGroup;

    constructor(public readonly form: Form, protected readonly serverAgent: ServerAgent) {
        this.formGroup = new FormGroup({});
        if (!form.childForms) {
            return;
        }

        form.childForms.forEach((child: ChildForm, key: string) => {
            if (child.isTabular) {
                this.childTabularData.set(key, new TabularData(child.form, serverAgent, child.isEditable));
            } else {
                if (child.isEditable) {
                    this.childData.set(key, new FormData(child.form, serverAgent));
                } else {
                    this.childData.set(key, new PanelData(child.form, serverAgent));
                }
            }
        });
    }

    /**
    * @override data is to be set to form group
    * @param data as received from a service request
    */
    public setAll(data: Vo) {
        this.data = data;
        this.formGroup.patchValue(data);

        this.childData.forEach((fd, key) => {
            fd.setAll(data[key] as Vo || {});
        });

        this.childTabularData.forEach((table, key) => {
            table.setAll(data[key] as Vo[] || []);
        });
    }

    /**
	 * @returns object contianing all data from form controls.
     * Note that this data will not contain fields from non-form panel
	 */
    public extractAll(): Vo {
        const d = this.formGroup.value;
        this.childData.forEach((fd, key) => {
            d[key] = fd.extractAll();
        });
        this.childTabularData.forEach((table, key) => {
            d[key] = table.extractAll();
        });
        return d;
    }

    /**
     * 
     * @param name name of the field. Valid field names can be picked up from 
     * static definitions in the form 
     * @param value 
     */
    public setFieldValue(name: string, value: string | number | boolean) {
        if (!this.form.fields.get(name)) {
            console.error(name + ' is not a field in the form ' + this.form.getName())
            return null;
        }

        const fc = this.formGroup.get(name);
        if (fc) {
            fc.setValue(value);
            return;
        }

        this.data[name] = value;
        return;
    }

    /**
     * 
     * @param name name of the field. Valid field names can be picked up from 
     * static definitions in the form 
     * @returns value of this field, or null/undefined if this is not a field
     */
    public getFieldValue(name: string): string | number | boolean {
        if (!this.form.fields.get(name)) {
            console.error(name + ' is not a field in the form ' + this.form.getName())
            return null;
        }

        const fc = this.formGroup.get(name);
        if (fc) {
            return fc.value;
        }

        return this.data[name] as string | number | boolean;
    }

    /**
     * 
     * @param name name of the child field. 
     * Valid child names are available as static members of the form
     * @returns appropriate data for the child form. null/undefined if no such child
     */
    public getChildData(name: string): PanelData | FormData {
        if (this.childData) {
            return this.childData.get(name);
        }
        return null;
    }

    /**
     * 
     * @param name name of the child field. 
     * Valid child names are available as static members of the form
     * @returns appropriate data for the child form. null/undefined if no such child
     */
    public getChildTable(name: string): TabularData {
        if (this.childTabularData) {
            return this.childTabularData.get(name);
        }
        return null;
    }

    /**
     * extarct key fields only
     */
    public extractKeyFields(): FieldValues {
        let data = this.extractFields(this.form.keyFields);
        if (!data) {
            data = this.extractFields(this.form.uniqueFields);
        }
        return data;
    }

    private extractFields(fields: string[]): FieldValues {
        if (!fields) {
            return null;
        }
        const data: FieldValues = {};
        for (const field of fields) {
            const val = this.data[field];
            if (val == 'undefined' || val === null) {
                return null;
            }
            data[field] = val as string | number | boolean;
        }
        return data;
    }
    /**
     * reset the messages. typically called when user dismisses them, or before a server-request is made
     */
    public resetMessages() {
        this.errors = [];
        this.warnings = [];
        this.info = [];
    }

    /**
     * messages are set to this model, from where the 
     * html component can pick it up for rendering
     * @param messages 
     */
    public setMessages(messages: Message[]) {
        this.resetMessages();
        messages.forEach(msg => {
            switch (msg.type) {
                case "error":
                    this.errors.push(msg.text);
                    break;
                case "warning":
                    this.warnings.push(msg.text);
                    break;
                default:
                    this.info.push(msg.text);
                    break;
            }
        });
    }

    public fetchData() {
        const serviceName = this.form.getServiceName(Conventions.OP_FETCH);
        if (!serviceName) {
            return;
        }

        const data = this.extractKeyFields();
        if (data == null) {
            console.error('Key values not found. Fetch request abandoned');
            return;
        }

        const obs = this.serverAgent.serve(serviceName, data, true);
        this.waitingForServerResponse = true;
        this.resetMessages();
        obs.subscribe(vo => {
            this.setAll(vo);
            this.waitingForServerResponse = false;
        }, msgs => {
            this.setMessages(msgs);
            this.waitingForServerResponse = false;
        });
    }

        public fetchChildren(child: string, filters: FilterRequest) {
        const td = this.childTabularData.get(child);
        if(!td){
            console.error(child +' is not a tabular child of this panel. operation abandoned');
            return;
        }
        const childForm = this.form.childForms.get(child).form;
        const serviceName = childForm.getServiceName(Conventions.OP_FILTER);
        if (!serviceName) {
            return;
        }

        const obs = this.serverAgent.serve(serviceName, filters, true);
        this.waitingForServerResponse = true;
        this.resetMessages();
        obs.subscribe(vo => {
            td.setAll(vo.list as Vo[]);
            this.waitingForServerResponse = false;
        }, msgs => {
            this.setMessages(msgs);
            this.waitingForServerResponse = false;
        });
    }

    public validateForm(): boolean {
        this.formGroup.updateValueAndValidity();
        let ok = this.formGroup.valid;

        this.childData.forEach((fd, key) => {
            const b = fd.validateForm();
            ok = ok && b;
        });
        this.childTabularData.forEach((table, key) => {
            const b = table.validateForm();
            ok = ok && b;
        });
        return ok;
    }

    /**
     * should we convert this to a promise? Or should we have some standard way of handling error and success?
     */
    public saveAsNew() {
        const serviceName = this.form.getServiceName(Conventions.OP_NEW);
        if (!serviceName) {
            return;
        }

        if (!this.validateForm()) {
            //we have to ensure that the field in error is brought to focus!!
            alert("Form data has some errors. Please fix and then try again.");
            return;
        }
        const data = this.extractAll();
        const obs = this.serverAgent.serve(serviceName, data);
        this.waitingForServerResponse = true;
        this.resetMessages();
        obs.subscribe(vo => {
            alert('Data saved successfully');
            this.waitingForServerResponse = false;
        }, msgs => {
            this.setMessages(msgs);
            this.waitingForServerResponse = false;
        });
    }

    /**
     * update operation. WHat do we do after successful operation?
     */
    public save() {
        const serviceName = this.form.getServiceName(Conventions.OP_UPDATE);
        if (!serviceName) {
            return;
        }

        if (!this.validateForm()) {
            //we have to ensure that the field in error is brought to focus!!
            alert("Form data has some errors. Please fix and then try again.");
            return;
        }
        const data = this.extractAll();
        const obs = this.serverAgent.serve(serviceName, data);
        this.waitingForServerResponse = true;
        this.resetMessages();
        obs.subscribe(vo => {
            alert('Data saved successfully');
            this.waitingForServerResponse = false;
        }, msgs => {
            this.setMessages(msgs);
            this.waitingForServerResponse = false;
        });
        console.log('Update request initiated..')
    }

    public delete() {
        const serviceName = this.form.getServiceName(Conventions.OP_UPDATE);
        if (!serviceName) {
            return;
        }

        if (!this.validateForm()) {
            //we have to ensure that the field in error is brought to focus!!
            alert("Form data has some errors. Please fix and then try again.");
            return;
        }

        const data = this.extractKeyFields();
        const obs = this.serverAgent.serve(serviceName, data, true);
        this.waitingForServerResponse = true;
        this.resetMessages();
        obs.subscribe(vo => {
            alert('Data deleted successfully');
            this.waitingForServerResponse = false;
        }, msgs => {
            this.setMessages(msgs);
            this.waitingForServerResponse = false;
        });
        
        console.log('Delete request sent to server with data ', data);
    }
}
