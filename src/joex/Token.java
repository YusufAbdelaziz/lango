package joex;

public class Token {
  final String lexeme;
  final TokenType type;
  final Object literal;
  final int line;

  public Token(String lexeme, TokenType type, Object literal, int line) {
    this.lexeme = lexeme;
    this.type = type;
    this.literal = literal;
    this.line = line;
  }

  @Override
  public String toString() {
    return type + " " + lexeme + " " + literal;
  }
}