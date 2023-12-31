package lango.main;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
// import java.nio.file.Paths;
import java.util.List;

import lango.astNodes.Stmt;
import lango.interpreter.Interpreter;
import lango.parser.Parser;
import lango.parser.RuntimeError;
import lango.scanner.Scanner;
import lango.scanner.Token;
import lango.scanner.TokenType;
import lango.semanticAnalysis.Resolver;

public class Lango {
  private static final Interpreter interpreter = new Interpreter();

  /**
   * Used to ensure that we don't execute code that has a known error.
   */
  static boolean hadError = false;
  static boolean hadRuntimeError = false;

  public static void main(String[] args) throws IOException {
    if (args.length > 1) {
      System.out.println("Usage: lango [script]");
      System.exit(64);
    } else if (args.length == 1) {
      runFile(args[0]);
    } else {
      // Run the REPL.
      runPrompt();
    }
  }

  /**
   * Reads one line at a time and executes that line immediately (REPL).
   * 
   * @param path : The path given by the user which has Lox's source code.
   * @throws IOException
   */
  private static void runFile(String path) throws IOException {
    // Paths.get(path);
    byte[] bytes = Files.readAllBytes(Path.of(path));
    run(new String(bytes, Charset.defaultCharset()));
    if (hadError)
      System.exit(65);
    if (hadRuntimeError)
      System.exit(70);
  }

  /**
   * Reads one line at a time and executes that line immediately (REPL).
   * 
   * @throws IOException
   */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
    BufferedReader reader = new BufferedReader(input);

    while (true) {
      System.out.print("> ");
      /**
       * readLine reads a line which the user enters.
       *
       * In case the user decided to kill the CMD, the user would type CTRL + C which
       * signals an "end-of-file" (EOF) that results in a null line.
       */
      String line = reader.readLine();
      if (line == null)
        break;
      run(line);
      // The flag is reset for each loop because the REPL shouldn't be terminated when
      // the user makes a mistake.
      hadError = false;
    }
  }

  private static void run(String source) {
    Scanner scanner = new Scanner(source);
    List<Token> tokens = scanner.scanTokens();
    Parser parser = new Parser(tokens);
    List<Stmt> statements = parser.parse();

    // Stop if there is a syntax error.
    if (hadError)
      return;

    Resolver resolver = new Resolver(interpreter);

    resolver.resolve(statements);

    // Stop if there's a resolution error.

    if (hadError)
      return;

    interpreter.interpret(statements);
  }

  // TODO : Add the beginning and end column.
  /**
   * Notifies the user about syntax error that occurred at a specific line
   * number using a message.
   * 
   * @param line
   * @param message
   */
  public static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.out.println("[line " + line + "] Error" + where + ": " + message);
    hadError = true;
  }

  public static void error(Token token, String message) {
    if (token.type == TokenType.EOF) {
      report(token.line, " at end", message);
    } else {
      report(token.line, " at '" + token.lexeme + "'", message);
    }
  }

  public static void runtimeError(RuntimeError error) {
    System.err.println(error.getMessage() +
        "\n[line " + error.token.line + "]");
    hadRuntimeError = true;
  }
}