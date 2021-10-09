package joex;

import java.util.ArrayList;
import java.util.List;
import java.util.*;

public class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();

  // Points to the character that currently be considered.
  private int current = 0;
  // Points to the first character in the lexeme being scanned.
  private int start = 0;
  // Tracks what source line [current] is on so we can produce tokens that know
  // their location.
  private int line = 1;
  // Used to store the keywords as key/value pairs, in which each lexeme has its
  // equivalent
  private static final Map<String, TokenType> keywords;

  // Static initializer to initialize the keywords map.
  static {
    keywords = new HashMap<>();
    keywords.put("and", TokenType.AND);
    keywords.put("class", TokenType.CLASS);
    keywords.put("else", TokenType.ELSE);
    keywords.put("false", TokenType.FALSE);
    keywords.put("for", TokenType.FOR);
    keywords.put("fun", TokenType.FUN);
    keywords.put("if", TokenType.IF);
    keywords.put("nil", TokenType.NIL);
    keywords.put("or", TokenType.OR);
    keywords.put("print", TokenType.PRINT);
    keywords.put("return", TokenType.RETURN);
    keywords.put("super", TokenType.SUPER);
    keywords.put("this", TokenType.THIS);
    keywords.put("true", TokenType.TRUE);
    keywords.put("var", TokenType.VAR);
    keywords.put("while", TokenType.WHILE);
  }

  public Scanner(String source) {
    this.source = source;
  }

  public List<Token> scanTokens() {
    // Keep scanning until no characters are remaining
    while (!isAtEnd()) {
      start = current;
      scanToken();
    }
    // We're adding an end of file token to imply that there're no characters to
    // scan.
    tokens.add(new Token("", TokenType.EOF, null, line));

    return tokens;
  }

  private void scanToken() {
    char c = advance();

    switch (c) {
      case ')':
        addToken(TokenType.RIGHT_PAREN);
        break;
      case '(':
        addToken(TokenType.LEFT_PAREN);
        break;
      case '{':
        addToken(TokenType.LEFT_BRACE);
        break;
      case '}':
        addToken(TokenType.RIGHT_BRACE);
        break;
      case ',':
        addToken(TokenType.COMMA);
        break;
      case '.':
        addToken(TokenType.DOT);
        break;
      case '+':
        addToken(TokenType.PLUS);
        break;
      case '-':
        addToken(TokenType.MINUS);
        break;
      case ';':
        addToken(TokenType.SEMICOLON);
        break;
      case '*':
        addToken(TokenType.STAR);
        break;
      case '!':
        addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
        break;
      case '=':
        addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
        break;
      case '>':
        addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
        break;
      case '<':
        addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
        break;
      case '/':
        // If there's another slash, then it's a comment and it finishes till the end of
        // a line.
        if (match('/')) {
          while (peek() != '\n' && !isAtEnd())
            advance();
        } else {
          addToken(TokenType.SLASH);
        }
        break;
      case ' ':
      case '\t':
      case '\r':
        break;
      case '\n':
        line++;
        break;
      case '"':
        string();
        break;
      default:
        if (isDigit(c)) {
          number();
        } else if (isAlpha(c)) {
          identifier();
        } else {
          Joex.error(line, "Unexpected character.");
        }
        break;
    }
  }

  // Generates an identifier, by making serval lookahead searches to make sure the
  // characters that are after the first one are either numbers or letters.
  private void identifier() {
    while (isAlphaNumeric(peek()))
      advance();

    String text = source.substring(start, current);
    TokenType type = keywords.get(text);
    if (type == null)
      type = TokenType.IDENTIFIER;
    addToken(type);
  }

  // Checks if a character is either a number or letter.
  private boolean isAlphaNumeric(char c) {
    return isDigit(c) || isAlpha(c);
  }

  // Checks if a character is either belongs to alphabets or an underscore.
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
  }

  // Consumes the next character in the source file.
  private char advance() {
    return source.charAt(current++);
  }

  // Adds new token
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(text, type, literal, line));
  }

  // Checks if we reach the end of the file.
  private boolean isAtEnd() {
    return current >= source.length();
  }

  // Trying to match an expected character with the current character.
  private boolean match(char expected) {
    if (isAtEnd())
      return false;
    if (source.charAt(current) != expected)
      return false;
    current++;
    return true;
  }

  // Makes a lookahead search to check for the next character without consuming
  // it.
  private char peek() {
    if (isAtEnd())
      return '\0';
    return source.charAt(current);
  }

  // Generates string literal token.
  private void string() {
    // We'll keep consume characters until we find the closing double quote.
    // So we can find the value of the string literal.
    while (peek() != '"' && !isAtEnd()) {
      if (peek() == '\n')
        line++;
      advance();
    }

    // If we reach the end of source file without closing double quote, then the
    // string is not terminated.
    if (isAtEnd()) {
      Joex.error(line, "Unterminated string");
    }

    // Moving [current] pointer after the closing double quote '"'.
    advance();

    // Extracts the string value.
    //
    // The first character is at index [start + 1] since
    // [start] is the opening double quote.
    // While the last character is at [current - 2] since we just moved the
    // [current] pointer after closing double
    // quote to continue the scanning process.

    // [current - 1] exclusive which means [current - 2].
    String value = source.substring(start + 1, current - 1);
    addToken(TokenType.STRING, value);
  }

  // Generates number literal token.
  private void number() {

    // Consume the integer part.
    while (isDigit(peek()))
      advance();

    // Consume the fractional part.
    if (peek() == '.' && isDigit(peekNext())) {

      // Consume the '.'
      advance();

      // Consume the numbers after the decimal point.
      while (isDigit(peek()))
        advance();
    }
    addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
  }

  // Checks if a character is a digit character.
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  // Makes a lookahead search for the second character after [current].
  private char peekNext() {
    if (current + 1 >= source.length())
      return '\0';
    return source.charAt(current + 1);
  }
}
