package jlox.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

import jlox.scanner.*;
import jlox.*;
import jlox.main.JLox;

/**
 * Constructs the syntax tree from our language's grammar.
 * 
 * This class consumes tokens to match it to the correct expression grammar.
 * 
 * 
 * program → declaration* EOF ;
 * declaration -> funDecl | varDecl | statement;
 * varDecl -> "var" IDENTIFIER ( "=" expression )? ";" ;
 * 
 * funDecl -> "fun" function;
 * function -> IDENTIFIER "(" parameters? ")" block;
 * parameters -> IDENTIFIER ( "," IDENTIFIER )*;
 * 
 * statement → exprStmt
 * | returnStmt
 * | forStmt
 * | printStm*
 * | block
 * | ifStmt
 * | whileStmt;
 * 
 * returnStmt -> "return" expression? ";";
 * 
 * forStmt -> "for" "(" (varDecl | exprStmt | ";") expression? ";" expression?
 * ")" statement ;
 * whileStmt -> "while" "(" expression ")" statement;
 * ifStmt -> "if" "(" expression ")" statement ("else" statement)?;
 * block → "{" declaration* "}" ;
 * exprStmt → expression ";" ;
 * printStmt → "print" expression ";" ;
 * 
 * arguments -> expression ( "," expression )* ;
 * expression → assignment;
 * assignment -> IDENTIFIER "=" assignment | logic_or;
 * 
 * logic_or -> logic_and ("or" logic_and)*
 * logic_and -> equality ("and" equality)*
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary | call ;
 * call -> primary ( "(" arguments? ")" )*
 * primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" |
 * IDENTIFIER ;
 */

public class Parser {

  private static class ParseError extends RuntimeException {
  }

  /**
   * List of tokens to be parsed.
   */
  private final List<Token> tokens;

  /**
   * Points to the next token to be parsed.
   */
  private int current = 0;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  /**
   * Starts the parser to parse expressions.
   * 
   * @return
   */
  public List<Stmt> parse() {
    // try {
    // return expression();
    // } catch (ParseError error) {
    // return null;
    // }
    List<Stmt> statements = new ArrayList<>();
    while (!isAtEnd()) {
      statements.add(declaration());
    }

    return statements;
  }

  private Stmt declaration() {
    try {
      if (match(TokenType.FUN))
        return function("function");
      if (match(TokenType.VAR))
        return varDeclaration();

      return statement();
    } catch (ParseError error) {
      synchronize();
      return null;
    }
  }

  private Stmt.Function function(String kind) {
    Token name = consume(TokenType.IDENTIFIER, "Expect a" + kind + " name.");

    consume(TokenType.LEFT_PAREN, "Expect '(' after a " + kind + " name.");

    List<Token> parameters = new ArrayList<>();

    if (!check(TokenType.RIGHT_PAREN)) {

      do {
        if (parameters.size() >= 255) {
          error(peek(), "Can't have more than 255 parameters.");
        }

        parameters.add(consume(TokenType.IDENTIFIER, "Expect parameter name."));
      } while (match(TokenType.COMMA));
    }

    consume(TokenType.RIGHT_PAREN, "Expect '(' after a " + kind + " name.");

    consume(TokenType.LEFT_BRACE, "Expect '{' before a " + kind + " body.");

    List<Stmt> body = block();

    return new Stmt.Function(name, parameters, body);
  }

  private Stmt varDeclaration() {
    Token name = consume(TokenType.IDENTIFIER, "Expect a variable name.");
    Expr initializer = null;
    if (match(TokenType.EQUAL)) {
      initializer = expression();
    }

    consume(TokenType.SEMICOLON, "Expect ';' after variable declaration.");
    return new Stmt.Var(name, initializer);
  }

  private Stmt statement() {
    if (match(TokenType.IF))
      return ifStatement();
    if (match(TokenType.FOR))
      return forStatement();
    if (match(TokenType.WHILE))
      return whileStatement();
    if (match(TokenType.RETURN))
      return returnStatement();
    if (match(TokenType.PRINT))
      return printStatement();
    if (match(TokenType.LEFT_BRACE))
      return new Stmt.Block(block());

    return expressionStatement();
  }

  private Stmt returnStatement() {
    Token keyword = previous();

    Expr value = null;

    if (!check(TokenType.SEMICOLON)) {
      value = expression();
    }

    consume(TokenType.SEMICOLON, "Expect ';' after return value.");

    return new Stmt.Return(keyword, value);
  }

  private Stmt forStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'for'.");

    Stmt initializer;

    if (match(TokenType.SEMICOLON)) {
      initializer = null;
    } else if (match(TokenType.VAR)) {
      initializer = varDeclaration();
    } else {
      initializer = expressionStatement();
    }

    Expr condition = null;

    if (!check(TokenType.SEMICOLON)) {
      condition = expression();
    }

    consume(TokenType.SEMICOLON, "Expect ';' after loop condition.");

    Expr increment = null;
    if (!check(TokenType.RIGHT_PAREN)) {
      increment = expression();
    }
    consume(TokenType.RIGHT_PAREN, "Expect ')' after for clauses.");

    Stmt body = statement();

    // If increment is not null, we create a block of statements where the increment
    // statement should be the last
    // statement to be executed after each iteration.
    if (increment != null) {
      body = new Stmt.Block(Arrays.asList(body, new Stmt.Expression(increment)));
    }

    /// If condition is omitted, a true is placed to make an infinite loop.
    if (condition == null) {
      condition = new Expr.Literal(true);
    }

    body = new Stmt.While(condition, body);

    /// If there's an initializer statement, create a new block that has the
    /// initializer statement first followed by
    /// statements exist in the body.
    if (initializer != null) {
      body = new Stmt.Block(Arrays.asList(initializer, body));
    }

    return body;
  }

  private Stmt whileStatement() {
    consume(TokenType.LEFT_PAREN, "Expect '(' after 'while'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after condition.");
    Stmt body = statement();

    return new Stmt.While(condition, body);
  }

  private Stmt ifStatement() {

    consume(TokenType.LEFT_PAREN, "Expect '(' after 'if'.");
    Expr condition = expression();
    consume(TokenType.RIGHT_PAREN, "Expect ')' after if condition.");

    Stmt thenBranch = statement();
    Stmt elseBranch = null;
    if (match(TokenType.ELSE)) {
      elseBranch = statement();
    }

    return new Stmt.If(condition, thenBranch, elseBranch);
  }

  private List<Stmt> block() {
    List<Stmt> statements = new ArrayList<>();

    while (!check(TokenType.RIGHT_BRACE) && !isAtEnd()) {
      statements.add(declaration());
    }

    consume(TokenType.RIGHT_BRACE, "Expect '}' after block.");
    return statements;
  }

  private Stmt printStatement() {
    /*
     * Note that the match method already consumed the PRINT token, so what is
     * remaining the expression after it.
     */
    Expr value = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.");
    return new Stmt.Print(value);
  }

  private Stmt expressionStatement() {
    Expr expr = expression();
    consume(TokenType.SEMICOLON, "Expect ';' after value.");
    return new Stmt.Expression(expr);
  }

  private Expr expression() {

    return assignment();
  }

  private Expr assignment() {
    Expr expr = or();

    /// We want to make assignment expression like x = y = 5; valid.
    if (match(TokenType.EQUAL)) {
      Token equals = previous();
      Expr value = assignment();

      if (expr instanceof Expr.Variable) {
        Token name = ((Expr.Variable) expr).name;
        return new Expr.Assign(name, value);
      }

      error(equals, "Invalid assignment target.");
    }

    return expr;
  }

  private Expr or() {
    Expr expr = and();

    while (match(TokenType.OR)) {
      Token operator = previous();
      Expr right = and();
      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  private Expr and() {
    Expr expr = equality();

    while (match(TokenType.AND)) {
      Token operator = previous();
      Expr right = equality();

      expr = new Expr.Logical(expr, operator, right);
    }

    return expr;
  }

  /**
   * Generates an equality expression.
   * 
   * @return equality expression
   */
  private Expr equality() {
    Expr expr = comparison();

    while (match(TokenType.BANG_EQUAL, TokenType.EQUAL_EQUAL)) {
      Token operator = previous();
      Expr right = comparison();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr comparison() {
    Expr expr = term();

    while (match(TokenType.GREATER_EQUAL, TokenType.LESS_EQUAL, TokenType.GREATER, TokenType.LESS)) {
      Token operator = previous();
      Expr right = term();
      expr = new Expr.Binary(expr, operator, right);
    }
    return expr;
  }

  private Expr term() {
    Expr expr = factor();

    while (match(TokenType.MINUS, TokenType.PLUS)) {
      Token operator = previous();
      Expr right = factor();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr factor() {
    Expr expr = unary();

    while (match(TokenType.SLASH, TokenType.STAR)) {
      Token operator = previous();
      Expr right = unary();
      expr = new Expr.Binary(expr, operator, right);
    }

    return expr;
  }

  private Expr unary() {
    if (match(TokenType.BANG, TokenType.MINUS)) {
      Token operator = previous();
      Expr right = unary();
      return new Expr.Unary(operator, right);
    }

    return call();
  }

  private Expr call() {
    Expr expr = primary();
    while (true) {
      // call()()();
      if (match(TokenType.LEFT_PAREN)) {
        expr = finishCall(expr);
      } else {
        break;
      }
    }

    return expr;
  }

  private Expr finishCall(Expr callee) {
    List<Expr> arguments = new ArrayList<>();

    if (!check(TokenType.RIGHT_PAREN)) {
      do {
        if (arguments.size() >= 255) {
          error(peek(), "Can't have more than 255 arguments.");
        }
        arguments.add(expression());
      } while (match(TokenType.COMMA));
    }

    Token paren = consume(TokenType.RIGHT_PAREN, "Expect ')' after arguments.");

    return new Expr.Call(callee, paren, arguments);
  }

  private Expr primary() {
    if (match(TokenType.FALSE))
      return new Expr.Literal(false);
    if (match(TokenType.TRUE))
      return new Expr.Literal(true);
    if (match(TokenType.NIL))
      return new Expr.Literal(null);

    if (match(TokenType.NUMBER, TokenType.STRING)) {
      return new Expr.Literal(previous().literal);
    }

    if (match(TokenType.LEFT_PAREN)) {
      Expr expr = expression();
      consume(TokenType.RIGHT_PAREN, "Expect ')' after expression.");
      return new Expr.Grouping(expr);
    }

    if (match(TokenType.IDENTIFIER)) {
      return new Expr.Variable(previous());
    }

    throw error(peek(), "Expected expression.");
  }

  /**
   * Matches of the current token type is equal to one of the provided parameter
   * types.
   * 
   * @param types
   * @return boolean whether the current token type equal one of the provided
   *         parameter types.
   */
  private boolean match(TokenType... types) {
    for (TokenType type : types) {
      if (check(type)) {
        advance();
        return true;
      }
    }

    return false;
  }

  /**
   * Checks if a specific token type is found, if it's not found then throw an
   * error.
   * 
   * @param type
   * @param message
   * @return
   */
  private Token consume(TokenType type, String message) {
    if (check(type))
      return advance();

    throw error(peek(), message);
  }

  /**
   * Consumes the current token.
   * 
   * @return the current token.
   */
  private Token advance() {
    if (!isAtEnd())
      current++;

    return previous();
  }

  /**
   * checks if the current token is of the given type.
   * 
   * @param type : the type of token that we check if it's the same as the current
   *             token's type
   * @return true if current's type and given type are equal, false otherwise.
   */
  private boolean check(TokenType type) {
    if (isAtEnd())
      return false;
    return peek().type == type;
  }

  /**
   * Checks if we're at the end of the file
   * 
   * @return true if we're at the end of the file, false otherwise.
   */
  private boolean isAtEnd() {
    return peek().type == TokenType.EOF;
  }

  /**
   * Peeks for the next token without consuming it.
   * 
   * @return the next token.
   */
  private Token peek() {
    return tokens.get(current);
  }

  /**
   * Returns the previous token.
   * 
   * @return the previous token.
   */
  private Token previous() {
    return tokens.get(current - 1);
  }

  private ParseError error(Token token, String message) {
    JLox.error(token, message);
    return new ParseError();
  }

  /**
   * Synchronizes the parser state by discarding the tokens that violates the
   * current rule.
   */
  private void synchronize() {
    advance();

    while (!isAtEnd()) {
      if (previous().type == TokenType.SEMICOLON)
        return;

      switch (peek().type) {
        case CLASS:
        case FUN:
        case VAR:
        case FOR:
        case IF:
        case WHILE:
        case PRINT:
        case RETURN:
          return;
      }

      advance();
    }
  }
}
