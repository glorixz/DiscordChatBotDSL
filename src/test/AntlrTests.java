import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.testng.Assert;
import org.testng.annotations.*;
import parser.DSLLexer;
import parser.DSLParser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class AntlrTests {
    private final String testPath = "./src/test/inputs/";
    private final String outPath = "./src/test/outputs/";
    DSLLexer lexer;
    DSLParser parser;

    /**
     * Run input file through lexer and compare to expected output.
     * @param inputFile : a file name.
     */
    private void assertTokens(String inputFile, String outputFile) {
        System.out.println("Beginning test of input file: " + inputFile);
        String inputPath = testPath + inputFile;
        String outputPath = outPath + outputFile;

        List<String> expectedTokens = new ArrayList<String>();
        try {
            lexer = new DSLLexer(CharStreams.fromFileName(inputPath));

            Scanner sc = new Scanner(new File(outputPath));
            while (sc.hasNextLine()) {
                expectedTokens.add(sc.nextLine());
            }
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }

        ArrayList<String> tokenArr = new ArrayList<String>();
        for (Token token : lexer.getAllTokens()) {
            String tokenName = lexer.getVocabulary().getSymbolicName(token.getType());
            String tokenContent = token.getText().replace("\r", "\\r").replace("\n", "\\n");
            //System.out.println(tokenName + " : '" + tokenContent + "'");
            tokenArr.add(tokenName + " : '" + tokenContent + "'");
        }
        lexer.reset();
        Assert.assertEquals(tokenArr.size(), expectedTokens.size(),
                "Number of tokens: " + tokenArr.size() + " is not equal to expected: " + expectedTokens.size());
        Assert.assertEquals(tokenArr, expectedTokens, "Tokens do not match expected output.");
    }

    @Test
    public void testhelloConvo() {
        assertTokens("helloConvo.txt", "helloConvoOut.txt");
    }

    @Test
    public void testTrigger() {
        assertTokens("TriggerPhrase.txt", "TriggerPhraseOut.txt");
    }

    @Test
    public void testIfElse() {
        assertTokens("IfElse.txt", "IfElseOut.txt");
    }

    @Test
    public void testWhile() {
        assertTokens("while.txt", "whileOut.txt");
    }

    @Test
    public void testIncrAndDec() {
        assertTokens("incrementAndDecrement.txt", "incrementAndDecrementOut.txt");
    }
}