export class Conventions {
    /**
     * special pre-defined service to get drop-down values
     */
    static LIST_SERVICE = 'listService';
	/*
	 * form I/O service prefixes
	 */
    static OP_FETCH = 'get';
    static OP_NEW = 'create';
    static OP_UPDATE = 'update';
    static OP_DELETE = 'delete';
    static OP_FILTER = 'filter';

	/*
	 * filter operators
	 */
    static FILTER_EQ = '=';
    static FILTER_NE = '!=';
    static FILTER_LE = '<=';
    static FILTER_LT = '<';
    static FILTER_GE = '>=';
    static FILTER_GT = '>';
    static FILTER_BETWEEN = '><';
    static FILTER_STARTS_WITH = '^';
    static FILTER_CONTAINS = '~';
    
    /*
     * value types of fields 
     */
    static TYPE_TEXT = 0;
	static TYPE_INTEGER = 1;
	static TYPE_DECIMAL = 2;
	static TYPE_BOOLEAN = 3;
	static TYPE_DATE = 4;
	static TYPE_TIMESTAMP = 5;
}
