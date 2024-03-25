grammar Cockroach;

// Parser rules
startRule : statement+ EOF;

statement : assignment ';';

assignment : ID '=' INT;

ID : [a-zA-Z]+;
INT : [0-9]+;
STRING : '"' .*? '"';
WS : [ \t\r\n]+ -> skip;
