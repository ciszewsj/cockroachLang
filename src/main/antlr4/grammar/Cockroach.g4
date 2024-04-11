grammar Cockroach;

// Parser rules
startRule : statement* EOF;

statement : expression  ';';

expression : assignment | print | scan | scand | convert;

assignment : ID '=' value;

value : add | substract | mul | divide | variable | convert;

add : variable ADD variable;
substract : variable SUBSTRACT variable;
mul : variable MUL variable;
divide : variable DIVIDE variable;

print : PRINT ID;
scan : SCAN ID;
scand : SCAND ID;
convert : convertSymbol ID;

variable : ID | INT | FLOAT | DOUBLE | LONG;


convertSymbol : ITOF | FTOI | ITOD | DTOI | ITOL | LTOI | DTOF | FTOD | DTOL | LTOD | FTOL | LTOF;

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

FREE : 'free';
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
WS : [ \t\r\n]+ -> skip;
