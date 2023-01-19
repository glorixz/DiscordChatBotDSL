package parser;

import ast.*;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

// based on ParseTreeToAST from TinyVars
public class ParseTreeToAST extends DSLParserBaseVisitor<Node> {
    /**
     * Check type of a child.
     * @param tree - ctx.getChild(i)
     * @param type - DSLParser.<typename>
     * @return true if token type matches input type. False if not a token or does not match.
     */
    private boolean isType(ParseTree tree, int type) {
        Object o = tree.getPayload();
        return o instanceof Token ? ((Token) o).getType() == type : false;
    }

    /**
     * Given a string beginning and ending with quote characters, remove them.
     * @param text
     * @return String without quote characters
     */
    private String removeQuotes(String text) {
        return text.substring(1, text.length() - 1);
    }

    /**
     * Process VAR_NAME | USER_REF | STRING | INTEGER tokens.
     * @param token
     * @return Value
     */
    private Value getValue(Token token) {
        if (token.getType() == DSLLexer.STRING) {
            return new Constant(removeQuotes(token.getText()), Type.TEXT);
        } else {
            return new TextVar(token.getText());
        }
    }

    @Override
    public Program visitProgram(DSLParser.ProgramContext ctx) {
        List<Statement> statements = new ArrayList<>();
        for (DSLParser.StatementContext s: ctx.statement()) {
            statements.add((Statement) s.accept(this));
        }
        return new Program(statements);
    }

    @Override
    public Statement visitStatement(DSLParser.StatementContext ctx) {
        return (Statement) ctx.getChild(0).accept(this);
    }

    @Override
    public FnBody visitFunction_body(DSLParser.Function_bodyContext ctx) {
        List<FnStatement> statements = new ArrayList<>();
        // We have to process a mix of fn_statement and cond_block.
        // So, get children of fn_body and process everything except fn_start and fn_end
        for (int i = 1; i < ctx.getChildCount()-1; i++) {
            ParseTree child = ctx.getChild(i);
            Node accept = child.accept(this);
            if (accept != null) { // skip NEWLINE
                statements.add((FnStatement) accept);
            }
        }
        return new FnBody(statements);
    }

    @Override
    public StringBuild visitStr_concat(DSLParser.Str_concatContext ctx) {
        List<Value> values = new ArrayList<>();
        for (int i = 0; i < ctx.getChildCount(); i+=2) { // every other token is an add
            ParseTree child = ctx.getChild(i);
            if (isType(child, DSLParser.STRING)) {
                values.add(new Constant(removeQuotes(child.getText()), Type.TEXT));
            } else {
                values.add(new TextVar(child.getText()));
            }
        }
        return new StringBuild(values);
    }

    @Override
    public Node visitConversation(DSLParser.ConversationContext ctx) {
        FnBody body = (FnBody) ctx.function_body().accept(this);
        return new Conversation(ctx.VAR_NAME().getText(), body);
    }

    @Override
    public Node visitTriggerphrase(DSLParser.TriggerphraseContext ctx) {
        StringBuild stringBuild = (StringBuild) ctx.str_concat().accept(this);
        FnBody body = (FnBody) ctx.function_body().accept(this);
        return new TriggerPhrase(stringBuild, body);
    }

    @Override
    public FnStatement visitFn_statement(DSLParser.Fn_statementContext ctx) {
        return (FnStatement) visitChildren(ctx);
    }

    @Override
    public InitCounterVar visitCounter_declare(DSLParser.Counter_declareContext ctx) {
        return new InitCounterVar(ctx.VAR_NAME().getText(), Integer.parseInt(ctx.INTEGER().getText()));
    }

    @Override
    public Node visitBot_statement(DSLParser.Bot_statementContext ctx) {
        return new Message((StringBuild) ctx.str_concat().accept(this));
    }

    @Override
    public Node visitForm_statement(DSLParser.Form_statementContext ctx) {
        TextVar name = new TextVar(ctx.VAR_NAME().getText());
        return new Form(name);
    }

    @Override
    public Node visitFn_call(DSLParser.Fn_callContext ctx) {
        return new FnCall(ctx.VAR_NAME().getText());
    }

    @Override
    public Node visitDeca_inca_statement(DSLParser.Deca_inca_statementContext ctx) {
        if (ctx.INCREMENT() != null){
            return new IncDec(new CounterVar(ctx.VAR_NAME().getText()), true);
        } else if (ctx.DECREMENT() != null) {
            return new IncDec(new CounterVar(ctx.VAR_NAME().getText()), false);
        }
        return null;
    }

    @Override
    public Node visitCond_block(DSLParser.Cond_blockContext ctx) {
//        return either If or While
        return visitChildren(ctx);
    }

    @Override
    public Node visitConditional_chain(DSLParser.Conditional_chainContext ctx) {
        List<IfStatement> ifStatements = new ArrayList<>();

        // every boolean statement is accompanied by an if-body.
        for (int i = 0; i < ctx.bool_condition().size(); i++) {
            Comparee condition = (Comparee) ctx.bool_condition(i).accept(this);
            FnBody fnBody = (FnBody) ctx.function_body(i).accept(this);
            ifStatements.add(new IfStatement(condition, fnBody));
        }
        FnBody elsePart = ctx.else_statement() != null ? (FnBody) ctx.else_statement().accept(this) : null;
        return new CondChain(ifStatements, elsePart);
    }

    @Override
    public FnBody visitElse_statement(DSLParser.Else_statementContext ctx) {
        return (FnBody) ctx.function_body().accept(this);
    }

    @Override
    public Node visitWhile_loop(DSLParser.While_loopContext ctx) {
        Comparee condition = (Comparee) ctx.bool_condition().accept(this);
        FnBody fnBody = (FnBody) ctx.function_body().accept(this);
        return new While(condition, fnBody);
    }

    @Override
    public Comparee visitBool_condition(DSLParser.Bool_conditionContext ctx) {
        return (Comparee) ctx.boolean_expr().accept(this);
    }

    @Override
    public Comparee visitBoolean_expr(DSLParser.Boolean_exprContext ctx) {
        if (ctx.BOOL() != null) {
            return new Constant(ctx.BOOL().getText(), Type.BOOLEAN);
        } else if (ctx.L_PAREN() != null) {
            return (Comparee) ctx.boolean_expr(0).accept(this);
        } else if (ctx.NOT() != null) {
            Comparee exp = (Comparee) ctx.boolean_expr(0).accept(this);
            return new Comparator(exp, exp, Operator.NOT);
        } else if (ctx.binary() != null) {
            Comparee lhs = (Comparee) ctx.left.accept(this);
            Comparee rhs = (Comparee) ctx.right.accept(this);
            Operator op = ctx.binary().OR() != null ? Operator.OR : Operator.AND;
            return new Comparator(lhs, rhs, op);
        } else {
//          a IS b case
            Token lhs_token = ctx.left_c;
            Token rhs_token = ctx.right_c;
            Comparee lhs = getValue(lhs_token);
            Comparee rhs = getValue(rhs_token);
            Operator op = Operator.IS;
            return new Comparator(lhs, rhs, op);
        }
    }

    @Override
    public Node visitStop(DSLParser.StopContext ctx) {
        return new Stop();
    }
}
