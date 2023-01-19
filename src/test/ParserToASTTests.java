import ast.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.TokenStream;
import org.testng.Assert;
import org.testng.annotations.Test;
import parser.DSLLexer;
import parser.DSLParser;
import parser.ParseTreeToAST;
import parser.ThrowingErrorListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ParserToASTTests {
    private static final String testPath = "./src/test/inputs/";

    public Program ParseProgram(String inputPath, String... expectExceptionMsg) {
        try {
            DSLLexer lexer = new DSLLexer(CharStreams.fromFileName(testPath + inputPath));
            // throw exceptions on error instead of just printing to console
            lexer.removeErrorListeners();
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

            TokenStream tokens = new CommonTokenStream(lexer);
            DSLParser parser = new DSLParser(tokens);
            // throw exceptions on error instead of just printing to console
            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);

            ParseTreeToAST visitor = new ParseTreeToAST();
            return visitor.visitProgram(parser.program());
        } catch (IOException e) {
            Assert.fail("IOException: " + e.getMessage());
        } catch (Exception e) {
            if (expectExceptionMsg.length > 0) {
                Assert.assertEquals(e.getMessage(), expectExceptionMsg[0]);
            } else {
                Assert.fail("Exception occurred: " + e.getMessage());
            }
        }
        return null;
    }

    @Test
    public void SimpleCounter() {
        Program p = ParseProgram("simpleCounter.txt");
        List<Statement> statements = new ArrayList<>();
        statements.add(new InitCounterVar("a", 0));
        Program e = new Program(statements);
        boolean equals = e.equals(p);
        Assert.assertEquals(e, p);
    }

    @Test
    public void SimpleCounterDecrement() {
        Program p = ParseProgram("simpleCounterDecrement.txt");
        List<Statement> statements = new ArrayList<>();
        statements.add(new InitCounterVar("a", 1));

        List<FnStatement> bodyStatements = new ArrayList<>();
        bodyStatements.add(new IncDec(new CounterVar("a"), false));
        statements.add(new Conversation("hey", new FnBody(bodyStatements)));
        Program e = new Program(statements);
        Assert.assertEquals(e, p);
    }

    @Test
    public void HelloConvo() {
        Program p = ParseProgram("helloConvo.txt");

        List<Statement> statements = new ArrayList<>();
        List<FnStatement> fnStatements = new ArrayList<>();

        fnStatements.add(new Message(new StringBuild(
                new ArrayList<>(Arrays.asList(new Constant("GoodBye", Type.TEXT))))));
        statements.add(new Conversation("goodbye", new FnBody(fnStatements)));

        fnStatements = new ArrayList<>();
        List<Value> values1 = new ArrayList<>(Arrays.asList(
                new Constant("Hello", Type.TEXT),
                new TextVar("@User.role")));
        fnStatements.add(new Message(new StringBuild(values1)));

        List<Value> values2 = new ArrayList<>(Arrays.asList(
                new Constant("What's your name (mine is Bot)", Type.TEXT)));
        fnStatements.add(new Message(new StringBuild(values2)));
        fnStatements.add(new Form(new TextVar("input_name")));

        List<Value> values3 = new ArrayList<>(Arrays.asList(
                new Constant("Your name is ", Type.TEXT),
                new TextVar("input_name")));
        fnStatements.add(new Message(new StringBuild(values3)));

        fnStatements.add(new FnCall("goodbye"));

        FnBody fnBody = new FnBody(fnStatements);
        statements.add(new Conversation("hello", fnBody));

        statements.add(new TriggerPhrase(
                new StringBuild(new ArrayList<>(List.of(new Constant("say hello", Type.TEXT)))),
                new FnBody(new ArrayList<>(List.of(new FnCall("hello"))))));

        Program e = new Program(statements);
        Assert.assertEquals(e, p);
    }

    @Test
    public void IfElse() {
        Program p = ParseProgram("IfElse.txt");
        List<Statement> statements = new ArrayList<>();
        List<FnStatement> fnStatements = new ArrayList<>();

        List<Value> v1 = new ArrayList<>(List.of(new Constant("Time to go", Type.TEXT)));
        fnStatements.add(new Message(new StringBuild(v1)));

        List<Value> v2 = new ArrayList<>(List.of(new Constant("OK? Answer yes or no", Type.TEXT)));
        fnStatements.add(new Message(new StringBuild(v2)));
        fnStatements.add(new Form(new TextVar("input_go")));

        List<IfStatement> ifStatements = new ArrayList<>();

        ifStatements.add(new IfStatement(
                new Comparator(new TextVar("input_go"), new Constant("yes", Type.TEXT), Operator.IS),
                new FnBody(new ArrayList<>(List.of(new Message(new StringBuild(new ArrayList<>(List.of(new Constant("Bye", Type.TEXT))))))))));

        Comparator input_go_is_no = new Comparator(new TextVar("input_go"), new Constant("NO", Type.TEXT), Operator.IS);
        Comparator user_is_default = new Comparator(new TextVar("@User.role"), new Constant("default", Type.TEXT), Operator.IS);
        Comparator notCompare = new Comparator(input_go_is_no, user_is_default, Operator.AND);
        ifStatements.add(new IfStatement(
                new Comparator(notCompare, notCompare, Operator.NOT),
                new FnBody(new ArrayList<>(List.of(new Message(new StringBuild(new ArrayList<>(List.of(new Constant("Are you sure", Type.TEXT))))))))));

        fnStatements.add(new CondChain(ifStatements,
                new FnBody(new ArrayList<>(List.of(new Message(new StringBuild(new ArrayList<>(List.of(new Constant("Never mind", Type.TEXT))))))))));

        FnBody fnBody = new FnBody(fnStatements);
        statements.add(new Conversation("goodbye", fnBody));

        Program e = new Program(statements);
        Assert.assertEquals(e, p);
    }

    @Test
    public void TriggerPhrase() {
        Program p = ParseProgram("TriggerPhrase.txt");
        List<Statement> statements = new ArrayList<>();
        statements.add(new TriggerPhrase(
                new StringBuild(new ArrayList<>(List.of(new Constant("say hello", Type.TEXT)))),
                new FnBody(new ArrayList<>(List.of(new FnCall("hello"))))));
        Program e = new Program(statements);
        Assert.assertEquals(e, p);
    }

    @Test
    public void While() {
        Program p = ParseProgram("while.txt");
        List<Statement> statements = new ArrayList<>();

        FnBody whileBody = new FnBody(new ArrayList<>(List.of(
                new Message(new StringBuild(new ArrayList<>(List.of(new Constant("Guess my name", Type.TEXT))))),
                new Form(new TextVar("bot_name")),
                new CondChain(
                        new ArrayList<>(List.of(new IfStatement(
                                new Comparator(new TextVar("bot_name"), new Constant("Bot", Type.TEXT), Operator.IS),
                                new FnBody(new ArrayList<>(List.of(new Stop())))
                        ))),
                        null
                )
        )));
        While whileStm = new While(
                new Constant("TRUE", Type.BOOLEAN),
                whileBody
        );
        statements.add(new Conversation("while", new FnBody(new ArrayList<>(List.of(whileStm)))));
        Program e = new Program(statements);
        Assert.assertEquals(e, p);
    }

    @Test
    public void IfTrue() {
        Program p = ParseProgram("IfTrue.txt");
        List<Statement> statements = new ArrayList<>();
        statements.add(new Conversation(
                "goodbye",
                new FnBody(new ArrayList<>(List.of(
                        new CondChain(new ArrayList<>(List.of(new IfStatement(
                                new Constant("FALSE", Type.BOOLEAN),
                                new FnBody(new ArrayList<>(List.of(new Message(new StringBuild(new ArrayList<>(List.of(new Constant("Bye", Type.TEXT))))))))
                        ))),
                                null))))));
        Program e = new Program(statements);
        Assert.assertEquals(e, p);
    }

    @Test(description = "Should fail for a program with no valid statements")
    public void noStatementProgram() {
        Program p = ParseProgram("emptyProgram.txt",
                "Invalid program statement. Expecting one of the following: CONVO, TRIGGERPHRASE, COUNTER.\n" +
                        "Instead found: '<EOF>' at line 1:0");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if a function body is missing a bracket")
    public void functionMissingBracket() {
        Program p = ParseProgram("funcWithMissingBracket.txt",
                "Invalid statement body. Check for a missing bracket or extraneous input at line 8:0");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if a function statement is missing a start bracket")
    public void functionStatementMissingBracket() {
        Program p = ParseProgram("fnStmtWithMissingBracket.txt",
                "Expecting function body. Check for a missing bracket or extraneous input at line 9:7");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if a function statement is missing an end bracket")
    public void functionStatementMissingEndBracket() {
        Program p = ParseProgram("fnStmtWithMissingEndBracket.txt",
                "Invalid function statement body. Check for a missing bracket or extraneous input at line 9:2");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if a conversation has no name")
    public void convoMissingName() {
        Program p = ParseProgram("convoMissingName.txt",
                "Conversation declared without a name at line 1:5.\n"
                        + "Syntax: CONVO <var_name> {<statements>}");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if a triggerphrase has no trigger string")
    public void trigMissingTriggerString() {
        Program p = ParseProgram("trigMissingTriggerString.txt",
                "TRIGGERPHRASE is missing trigger string at line 1:15."
                        + "\nSyntax: TRIGGERPHRASE (\"<trigger string>\") {<statements>}");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if a counter is uninitialized")
    public void uninitializedCounter() {
        Program p = ParseProgram("uninitializedCounter.txt",
                "Uninitialized COUNTER variable at line 2:9."
                        + "\nSyntax: COUNTER <var_name> = <integer>");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if a BOT or TRIGGER statement has an invalid string")
    public void invalidBotString() {
        Program p = ParseProgram("invalidBotString.txt",
                "Strings, variables, and USER references are concatenated using '+'. Invalid string at: line 2:14");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if IF or ELSEIF has invalid or missing boolean expression")
    public void invalidBooleanExpr() {
        Program p = ParseProgram("invalidBooleanExpr.txt",
                "IF, ELSEIF, and WHILE require a valid boolean expression. Error: line 5:14 mismatched input 'NOT' expecting 'IS'");
        Assert.assertNull(p);
    }

    @Test(description = "Should fail if comparator IS missing RHS")
    public void invalidBooleanExpr2() {
        Program p = ParseProgram("invalidBooleanExpr2.txt",
                "The IS comparator compares strings, user ref (@User) or variables. Check for an error at line 5:16");
        Assert.assertNull(p);
    }
}
