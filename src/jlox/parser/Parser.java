package jlox.parser;

import java.util.List;

import jlox.scanner.*;
import jlox.*;

/**
 * Constructs the syntax tree from our language's grammar.
 * 
 * This class consumes tokens to match it to the correct expression grammar.
 * 
 * expression → equality ;
 * equality → comparison ( ( "!=" | "==" ) comparison )* ;
 * comparison → term ( ( ">" | ">=" | "<" | "<=" ) term )* ;
 * term → factor ( ( "-" | "+" ) factor )* ;
 * factor → unary ( ( "/" | "*" ) unary )* ;
 * unary → ( "!" | "-" ) unary | primary ;
 * primary → NUMBER | STRING | "true" | "false" | "nil" | "(" expression ")" ;
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
  public Expr parse() {
    try {
      return expression();
    } catch (ParseError error) {
      return null;
    }
  }

  private Expr expression() {
    return equality();
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

    return primary();
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
