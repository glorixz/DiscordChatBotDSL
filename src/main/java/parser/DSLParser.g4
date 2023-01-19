parser grammar DSLParser;
options { tokenVocab=DSLLexer; }

program: NEWLINE? statement+ EOF;
statement: conversation | triggerphrase | (counter_declare NEWLINE?);

function_body: START_FN (fn_statement | cond_block)* END_FN;
str_concat: (VAR_NAME | USER_REF | STRING) (ADD (VAR_NAME | USER_REF | STRING))*;

conversation:
    CONVO_START VAR_NAME function_body;
triggerphrase:
    TRIGGERPHRASE L_PAREN str_concat R_PAREN function_body;

fn_statement: (
    counter_declare
    | bot_statement
    | form_statement
    | fn_call
    | deca_inca_statement
    | stop
    );
stop: STOP NEWLINE;
counter_declare:
    COUNTER VAR_NAME VAR_ASSIGN INTEGER NEWLINE;
bot_statement:
    BOT str_concat NEWLINE;
form_statement:
    USER_INPUT VAR_NAME NEWLINE;
fn_call:
    START VAR_NAME NEWLINE;
deca_inca_statement:
    (INCREMENT | DECREMENT) VAR_NAME NEWLINE;

cond_block:
    (conditional_chain | while_loop);
conditional_chain:
    IF bool_condition function_body (ELSEIF bool_condition function_body)* else_statement?;
else_statement:
    ELSE function_body;
while_loop:
    WHILE bool_condition function_body;

bool_condition: L_PAREN boolean_expr R_PAREN;
// https://stackoverflow.com/questions/30976962/nested-boolean-expression-parser-using-antlr
boolean_expr:
    L_PAREN boolean_expr R_PAREN
    | NOT boolean_expr
    | left=boolean_expr op=binary right=boolean_expr
    | left_c=(VAR_NAME | USER_REF | STRING | INTEGER) comparator right_c=(VAR_NAME | USER_REF | STRING | INTEGER)
    | BOOL;
binary:
    AND | OR;
comparator:
    IS;
