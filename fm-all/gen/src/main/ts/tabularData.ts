import { PanelData } from './panelData';
import { Form } from './form';
import { FormData } from './formData';
import { ServerAgent } from './serverAgent';
import { Vo } from './types';

export class TabularData {
    private childData: Array<PanelData | FormData> = [];
    constructor(public readonly form: Form, private serverAgent: ServerAgent, public readonly isEditable: boolean) {
    }

    /**
     * set data to this panel
     * @param data 
     */
    setAll(data: Vo[]): void {
        this.childData = [];
        data.forEach(vo => {
            let fd: PanelData | FormData;
            if (this.isEditable) {
                fd = new FormData(this.form, this.serverAgent);
            } else {
                fd = new PanelData(this.form, this.serverAgent)
            }
            fd.setAll(vo);
            this.childData.push(fd);
        });
    }

    /**
     * extract data from each of the child-panel into an array
     */
    extractAll(): Vo[] {
        const data: Vo[] = [];
        this.childData.forEach(fd => data.push(fd.extractAll()));
        return data;
    }

    /**
     * validate all the forms
     * @returns true if all ok. false if any one form-control is in error, or any custom-validaiton fails
     */
    validateForm(): boolean {
        let allOk = true;
        this.childData.forEach(fd => {
            const ok = fd.validateForm();
            allOk = allOk && ok;
        });
        return allOk;
    }

    appendRow(): void {
        let fd: PanelData | FormData;
        if (this.isEditable) {
            this.childData.push(new FormData(this.form, this.serverAgent));
            return;
        }
        this.childData.push(new PanelData(this.form, this.serverAgent));
    }
}