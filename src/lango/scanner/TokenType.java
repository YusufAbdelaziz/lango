package lango.scanner;

public enum TokenType {
  // Single character tokens.
  LEFT_PAREN, RIGHT_PAREN, LEFT_BRACE, RIGHT_BRACE, COMMA, DOT, MINUS, PLUS, SEMICOLON, SLASH, STAR,

  // One or more character tokens.
  BANG, BANG_EQUAL, EQUAL, EQUAL_EQUAL, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL,

  // Literals
  IDENTIFIER, NUMBER, STRING,

  // Keywords
  AND, OR, CLASS, THIS, SUPER, ELSE, IF, NIL, PRINT, RETURN, TRUE, FALSE, VAR, WHILE, FOR, FUN,

  // End of file
  EOF
}
