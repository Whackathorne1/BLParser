import components.map.Map;
import components.program.Program;
import components.program.Program1;
import components.queue.Queue;
import components.simplereader.SimpleReader;
import components.simplereader.SimpleReader1L;
import components.simplewriter.SimpleWriter;
import components.simplewriter.SimpleWriter1L;
import components.statement.Statement;
import components.utilities.Reporter;
import components.utilities.Tokenizer;

/**
 * Layered implementation of secondary method {@code parse} for {@code Program}.
 *
 * @author Put your name here
 *
 */
public final class Program1Parse1 extends Program1 {

    /*
     * Private members --------------------------------------------------------
     */

    /**
     * Parses a single BL instruction from {@code tokens} returning the
     * instruction name as the value of the function and the body of the
     * instruction in {@code body}.
     *
     * @param tokens
     *            the input tokens
     * @param body
     *            the instruction body
     * @return the instruction name
     * @replaces body
     * @updates tokens
     * @requires <pre>
     * [<"INSTRUCTION"> is a prefix of tokens]  and
     *  [<Tokenizer.END_OF_INPUT> is a suffix of tokens]
     * </pre>
     * @ensures <pre>
     * if [an instruction string is a proper prefix of #tokens]  and
     *    [the beginning name of this instruction equals its ending name]  and
     *    [the name of this instruction does not equal the name of a primitive
     *     instruction in the BL language] then
     *  parseInstruction = [name of instruction at start of #tokens]  and
     *  body = [Statement corresponding to the block string that is the body of
     *          the instruction string at start of #tokens]  and
     *  #tokens = [instruction string at start of #tokens] * tokens
     * else
     *  [report an appropriate error message to the console and terminate client]
     * </pre>
     */
    private static String parseInstruction(Queue<String> tokens,
            Statement body) {
        assert tokens != null : "Violation of: tokens is not null";
        assert body != null : "Violation of: body is not null";
        assert tokens.length() > 0 && tokens.front().equals("INSTRUCTION") : ""
                + "Violation of: <\"INSTRUCTION\"> is proper prefix of tokens";

        //get "INSTRUCTION"
        String firstToken = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isKeyword(firstToken),
                "ERROR: Illegal starting token");
        Reporter.assertElseFatalError(tokens.length() > 0,
                "ERROR: tokens is empty");

        //get identifier
        String identifier = tokens.dequeue();
        Reporter.assertElseFatalError(Tokenizer.isIdentifier(identifier),
                "ERROR: Instruction " + identifier + "invalid");

        //get "IS"
        Reporter.assertElseFatalError(
                tokens.length() > 0 && tokens.dequeue().equals("IS"),
                "ERROR: expected: IS, actual: " + tokens.front());

        //parse block
        body.parseBlock(tokens);

        //get "END"
        Reporter.assertElseFatalError(
                tokens.length() > 0 && tokens.dequeue().equals("END"),
                "ERROR: expected: END, actual: " + tokens.front());

        //get identifier
        Reporter.assertElseFatalError(
                tokens.length() > 0 && tokens.dequeue().equals(identifier),
                "ERROR: Instruction " + tokens.front() + "invalid");

        return identifier;
    }

    /*
     * Constructors -----------------------------------------------------------
     */

    /**
     * No-argument constructor.
     */
    public Program1Parse1() {
        super();
    }

    /*
     * Public methods ---------------------------------------------------------
     */

    @Override
    public void parse(SimpleReader in) {
        assert in != null : "Violation of: in is not null";
        assert in.isOpen() : "Violation of: in.is_open";
        Queue<String> tokens = Tokenizer.tokens(in);
        this.parse(tokens);
    }

    @Override
    public void parse(Queue<String> tokens) {
        assert tokens != null : "Violation of: tokens is not null";
        assert tokens.length() > 0 : ""
                + "Violation of: Tokenizer.END_OF_INPUT is a suffix of tokens";

        if (!tokens.front().equals(Tokenizer.END_OF_INPUT)) {
            Reporter.assertElseFatalError(tokens.dequeue().equals("PROGRAM"),
                    "ERROR: must begin with PROGRAM");

            Reporter.assertElseFatalError(
                    tokens.length() > 0
                            && Tokenizer.isIdentifier(tokens.front()),
                    "ERROR: Instruction name" + tokens.front() + "invalid");
            String identifierToken = tokens.dequeue();

            String errorMessage = tokens.front();
            Reporter.assertElseFatalError(
                    tokens.length() > 0 && tokens.dequeue().equals("IS"),
                    "ERROR: expected IS, actual " + errorMessage);
            Reporter.assertElseFatalError(tokens.length() > 0,
                    "ERROR: tokens is empty");

            Map<String, Statement> context = this.newContext();
            while (tokens.front().equals("INSTRUCTION")) {
                Statement block = this.newBody();
                String identifier = parseInstruction(tokens, block);
                Reporter.assertElseFatalError(!context.hasKey(identifier),
                        "ERROR: context missing unique instruction");
                context.add(identifier, block);
                Reporter.assertElseFatalError(tokens.length() > 0,
                        "ERROR: tokens is empty");
            }
            errorMessage = tokens.front();
            Reporter.assertElseFatalError(
                    tokens.length() > 0 && tokens.dequeue().equals("BEGIN"),
                    "ERROR: expected BEGIN, actual " + errorMessage);

            Statement body = this.newBody();
            body.parseBlock(tokens);

            errorMessage = tokens.front();
            Reporter.assertElseFatalError(
                    tokens.length() > 0 && tokens.dequeue().equals("END"),
                    "Expected END, actual " + errorMessage);
            Reporter.assertElseFatalError(
                    tokens.length() > 0
                            && tokens.dequeue().equals(identifierToken),
                    "Expected " + identifierToken);

            //END_OF_INPUT suffix check
            Reporter.assertElseFatalError(
                    tokens.length() == 1
                            && tokens.front().equals(Tokenizer.END_OF_INPUT),
                    "ERROR: missing END_OF_INPUT");

            //rebuild program
            this.setName(identifierToken);
            this.swapContext(context);
            this.swapBody(body);
        } else {
            this.clear();
        }

    }

    /*
     * Main test method -------------------------------------------------------
     */

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        SimpleReader in = new SimpleReader1L();
        SimpleWriter out = new SimpleWriter1L();
        /*
         * Get input file name
         */
        out.print("Enter valid BL program file name: ");
        String fileName = in.nextLine();
        /*
         * Parse input file
         */
        out.println("*** Parsing input file ***");
        Program p = new Program1Parse1();
        SimpleReader file = new SimpleReader1L(fileName);
        Queue<String> tokens = Tokenizer.tokens(file);
        file.close();
        p.parse(tokens);
        /*
         * Pretty print the program
         */
        out.println("*** Pretty print of parsed program ***");
        p.prettyPrint(out);

        in.close();
        out.close();
    }

}
