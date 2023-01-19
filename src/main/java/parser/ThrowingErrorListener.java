package parser;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.misc.ParseCancellationException;

import java.util.List;

// https://stackoverflow.com/questions/18132078/handling-errors-in-antlr4
// A class for doing two things:
// - instead of printing parser/lexer errors to console, force exception
//   to be thrown instead with a message.
// - Override certain errors with a more descriptive message.
public class ThrowingErrorListener extends BaseErrorListener {
    public static final ThrowingErrorListener INSTANCE = new ThrowingErrorListener();

    @Override
    public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine, String msg, RecognitionException e)
            throws ParseCancellationException {
        String locMsg = "line " + line + ":" + charPositionInLine;
        String defaultMsg = locMsg + " " + msg;

        // throw default message if lexer error occurred.
        if (!(recognizer instanceof DSLParser)) {
            throw new ParseCancellationException(defaultMsg);
        }

        DSLParser parser = (DSLParser) recognizer;
        List<String> stack = parser.getRuleInvocationStack(); // parser rules leading to this error, most recent first

        Token offSymbol = (Token) offendingSymbol;
        int tokInd = offSymbol.getType();
//        String offSymName = DSLLexer.VOCABULARY.getSymbolicName(offSymbol.getType());

        if (stack.get(0).equals("program")) {
            String mismatchedInput = "";
            if (msg.contains("mismatched input")) {
                mismatchedInput = msg.split(" ")[2];
            }
            throw new ParseCancellationException("Invalid program statement. Expecting one of the following: CONVO, TRIGGERPHRASE, COUNTER.\n"
                + "Instead found: " + mismatchedInput + " at " + locMsg);
        }

        if (stack.get(0).equals("function_body")) {
            if (msg.contains("expecting START_FN")) {
                throw new ParseCancellationException("Expecting function body. Check for a missing bracket or extraneous input at " + locMsg);
            }
            if (tokInd == DSLParser.TRIGGERPHRASE || tokInd == DSLParser.CONVO_START || tokInd == DSLParser.COUNTER) {
                throw new ParseCancellationException("Invalid statement body. Check for a missing bracket or extraneous input at " + locMsg);
            }
            if (msg.matches("extraneous input .*expecting .*END_FN.*")) {
                throw new ParseCancellationException("Invalid function statement body. Check for a missing bracket or extraneous input at " + locMsg);
            }
        }

        if (stack.get(0).equals("conversation")) {
            if (msg.contains("missing VAR_NAME")) {
                throw new ParseCancellationException("Conversation declared without a name at " + locMsg
                        + ".\nSyntax: CONVO <var_name> {<statements>}");
            }
        }

        if (stack.get(0).equals("str_concat")) {
            if (stack.get(1).equals("triggerphrase") && msg.contains("missing") && msg.contains("STRING")) {
                throw new ParseCancellationException("TRIGGERPHRASE is missing trigger string at " + locMsg
                        + ".\nSyntax: TRIGGERPHRASE (\"<trigger string>\") {<statements>}");
            }
        }

        if (stack.get(0).equals("triggerphrase") && msg.contains("expecting ')'")) {
            throw new ParseCancellationException("TRIGGERPHRASE must have a valid string. Error: "
                    + defaultMsg);
        }

        if (stack.get(0).equals("counter_declare")) {
            if (msg.contains("expecting '='")) {
                throw new ParseCancellationException("Uninitialized COUNTER variable at " + locMsg
                + ".\nSyntax: COUNTER <var_name> = <integer>");
            }
        }

        if (stack.get(0).equals("bot_statement")) {
            if (msg.contains("expecting NEWLINE")) {
                throw new ParseCancellationException("Strings, variables, and USER references are concatenated using '+'. Invalid string at: " + locMsg);
            }
            throw new ParseCancellationException("BOT statements must have a valid string. Error: "
                    + defaultMsg);
        }

        if (stack.get(0).equals("boolean_expr") || stack.get(1).equals("boolean_expr")) {
            if (msg.contains("missing") && msg.contains("STRING")) {
                throw new ParseCancellationException("The IS comparator compares strings, user ref (@User) or variables. Check for an error at "
                    + locMsg);
            }
            throw new ParseCancellationException("IF, ELSEIF, and WHILE require a valid boolean expression. Error: "
                + defaultMsg);
        }

        throw new ParseCancellationException(defaultMsg);
    }
}
