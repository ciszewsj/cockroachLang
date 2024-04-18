grammar Cockroach;

// Parser rules
startRule : (programbody|function)* EOF;

programbody: repeatstatement | ifstatement | statement;


function : function_type ID ':' fblock END ';';

function_type : FUNTION_INT | FUNTION_DOUBLE;

fblock: programbody*;


ifstatement : IF compare ':' ifbody END ';';

ifbody : programbody*;

repeatstatement : repeatheader repeatbody END ';';

repeatbody: programbody*;

statement : expression  ';';

expression : assignment | localassigment | print | scan | scand | convert;

assignment : ID '=' value;

localassigment : LOCAL ID '=' value;

value : add | substract | mul | divide | variable | convert;

add : variable ADD variable;
substract : variable SUBSTRACT variable;
mul : variable MUL variable;
divide : variable DIVIDE variable;

compare : ID operator ID;

operator : EQUALS | MORES | LESSS;



print : PRINT ID;
scan : SCAN ID;
scand : SCAND ID;
convert : convertSymbol ID;

variable : ID | INT | FLOAT | DOUBLE | LONG;


repeatheader : REPEAT ID KROPKI;

convertSymbol : ITOF | FTOI | ITOD | DTOI | ITOL | LTOI | DTOF | FTOD | DTOL | LTOD | FTOL | LTOF;

EQUALS : '==';
MORES : '>';
LESSS : '<';

ITOF : 'itof';
FTOI : 'ftoi';

ITOD : 'itod';
DTOI : 'dtoi';

ITOL : 'itol';
LTOI : 'ltoi';

DTOF : 'dtof';
FTOD : 'ftod';

DTOL : 'dtol';
LTOD : 'ltod';

FTOL : 'ftol';
LTOF : 'ltof';

REPEAT : 'repeat';

FUNTION_INT :'func_i';
FUNTION_DOUBLE :'func_d';

LOCAL : 'local';
IF : 'if';
END : 'end';
SCAN : 'scan';
SCAND : 'scand';
PRINT : 'print';
LONG : INT 'l';
FLOAT : INT '.' INT 'f';
DOUBLE : INT '.' INT;
ADD : '+';
SUBSTRACT : '-';
MUL : '*';
DIVIDE : '/';
ID : [a-zA-Z]+;
INT : [0-9]+;
STRING : '"' .*? '"';
KROPKI : ':';
WS : [ \t\r\n]+ -> skip;
