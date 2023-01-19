lexer grammar DSLLexer;

CONVO_START: 'CONVO';
TRIGGERPHRASE: 'TRIGGERPHRASE';
START_FN: '{' NEWLINE*;
END_FN: '}' NEWLINE*;
L_PAREN: '(';
R_PAREN: ')';
INCREMENT: 'INCREMENT';
DECREMENT: 'DECREMENT';
COUNTER: 'COUNTER';

BOT: 'BOT' WS* ':';
USER_INPUT: 'USER' WS* ':';
START: 'START';
STOP: 'STOP';

WHILE: 'WHILE';
IF: 'IF';
ELSEIF: 'ELSEIF';
ELSE: 'ELSE';

IS: 'IS';
AND: 'AND';
OR: 'OR';
NOT: 'NOT';
BOOL: 'TRUE' | 'FALSE';

STRING: '"' ~[\r\n"]* '"';
ADD: '+';
VAR_ASSIGN: '=';

USER_REF: '@User' ('.' [a-zA-Z0-9_-]+)?;

INTEGER: [0-9]+;
VAR_NAME: [a-zA-Z0-9_-]+; // permitted variable names: letters, numbers, _, -
NEWLINE: (WS* ('\r\n' | '\n' | '\r' | COMMENT))+;  // newline ends a statement.

// All comments will be tokenized as newlines. This is just for readability.
COMMENT: '#' ~[\r\n]* ('\r'? '\n' | '\r') -> channel(HIDDEN);
WS: [\t ]+ -> skip;