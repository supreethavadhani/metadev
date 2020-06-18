##Simplity Concepts
This document describes the basic concepts that are the back bone of Simplity. We take a bottom-up approach.
#Field
#Value
following are all values
12
102.45
James Bond,
true
false
12-Sep-2015
18-July-1988 14:55:23.345 IST 

Value is a piece of data. Typical application that uses Simplity works with a data-base that organizes data in a structured way(Using tables and columns)

Concept of value is intuitive, but its precise definition would be confusing in the programming context because of the definitions of 'objects' and 'values'. For example in the Java programming language, 12 is a primitive value but "James Bond" is a string that is an object. 

It is important to keep the programming concept of value and object away for our purpose. How Java Programming language defines a given value, or  

##FORM USAGE ON THE CLIENT
#FormVo
If the data coming from the server is used as read-only by the client, and it is not to be sent-back to the server, then just use the Vo interface.
e.g.

`this.sa.serve(.....).subscribe(`
    `    data =>`
    `    this.myVo = data as FormVo;`
`);`


#PanelData
Panel data is a real class. It has methods to manipulate the underlying data structure, and to avail the form-based services. e.g.
`this.pd.fetch(.....).subscribe(`
    `    data =>`
    `    this.myVo = data as FormVo;`
`);
This can also be used to get field values for output fields in the HTML. like 

`{{pd.getFieldValue('fieldName')}}`
However, using the FormVo interface is more elegant  ` {{vo.fieldName}} `

#FormData
Form data is to be used when the page needs to edit data and submit it to the server. Form Data takes care of two-way binding for all input-capable fields. It also handles all interaction with the server.
