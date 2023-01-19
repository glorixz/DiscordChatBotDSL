package ui;

import ast.Program;
import ast.evaluator.EvaluateVisitor;

import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import parser.DSLLexer;
import parser.DSLParser;
import parser.ParseTreeToAST;
import parser.ThrowingErrorListener;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
    private static final boolean debugMode = false;
    private static final String inputFile = "./src/input.txt";
    private static final String outputFile = "./src/main/java/bot/bot.js";

    /**
     * Main program for this project. Given an input, parse it and create
     * a corresponding discord bot.
     */
    public static void main(String[] args) {
        try {
            File mainFile = new File(outputFile);
            mainFile.delete();
            mainFile.createNewFile();

            if (debugMode) System.out.println("Converting input file into tokens...");
            DSLLexer lexer = new DSLLexer(CharStreams.fromFileName(inputFile));
            // throw exceptions on error instead of just printing to console
            lexer.removeErrorListeners();
            lexer.addErrorListener(ThrowingErrorListener.INSTANCE);

            if (debugMode) {
                for (Token token : lexer.getAllTokens()) {
                    System.out.println(token);
                }
            }
            lexer.reset();
            TokenStream tokens = new CommonTokenStream(lexer);
            if (debugMode) System.out.println("Done tokenizing");

            DSLParser parser = new DSLParser(tokens);
            // throw exceptions on error instead of just printing to console
            parser.removeErrorListeners();
            parser.addErrorListener(ThrowingErrorListener.INSTANCE);

            Program parsedProgram = new ParseTreeToAST().visitProgram(parser.program());
            if (debugMode) System.out.println("Done parsing");

            EvaluateVisitor evaluateVisitor = new EvaluateVisitor();
            StringBuilder output = new StringBuilder();
            StringBuilder err = new StringBuilder();
            parsedProgram.accept(evaluateVisitor, output, err);

            if (!err.isEmpty()) {
                System.err.println("An error occurred while generating the Discord bot code.");
                System.err.println(err);
            } else {
                FileWriter writer = new FileWriter(mainFile);
                writer.write(output.toString());
                writer.close();
                System.out.println("Successfully created Discord bot code at the following location: " + outputFile);
            }
        } catch (ParseCancellationException | IOException e) {
            System.err.println(e.getMessage());
        }
    }

}
