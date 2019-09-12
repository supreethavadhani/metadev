export interface Message {
	/**
	 * one of the pre-defined type Message.
	 */
	type: "error" | "warning" | "info" | "success";
	/**
	 * unique name assigned to this message in the app
	 */
	id: string;
	/**
	 * formatted text in English that is reay to be rendered
	 */
	text: string;
	/**
	 * name of the field (primary one in case more than one field are involved) that is the
	 * cause of this error. null if this is not specific to any field.
	 */
	fieldName?: string;
	/**
	 * name of the table/object that the field is part of. null if this not relevant
	 */
	objectName?: string;
	/**
	 * 0-based row number in case the field in error is part of a table.
	 */
	idx?: number;

	/**
	 * run-time parameters that are used to compose this message. This is useful in i18n
	 */
	params?: string[];
}

/**
 * Value object represents a general JSON object that we may 
 * use to pass data back-and-forth between the client ans the server
 */
export interface Vo {
	[key: string]: string | number | boolean | null | Vo | Vo[] | SelectOption[];
}

/**
 * server responds with a json with specific structure
 */
export interface ServerResponse {
	allOk?: boolean;
	messages?: Array<Message>;
	data?: Vo;
}

export interface FieldValues {
	[key: string]: string | number | boolean | null;
}

export interface SelectOption {
	value: string | number;
	text: string;
}

export interface Condition {
	comp: "=" | "!=" | "<" | "<=" | ">" | ">=" | "><" | "^" | "~";
	value: string | number | boolean;
	toValue?: string | number;
}

export interface FilterRequest {
	conditions: {[key:string]: Condition};
	sort?: {[key:string]: "asc" | "desc" | ""};
	maxRows?: number;
}

