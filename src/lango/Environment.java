package lango;

import java.util.HashMap;
import java.util.Map;

import lango.parser.RuntimeError;
import lango.scanner.Token;

public class Environment {

  /**
   * Reference for the enclosing environment.
   */
  public final Environment enclosing;

  /**
   * No-argument constructor for the global environment.
   */
  public Environment() {
    enclosing = null;
  }

  /**
   * Creates new local scopes.
   * 
   * @param enclosing Reference for the enclosing environment.
   */
  public Environment(Environment enclosing) {
    this.enclosing = enclosing;
  }

  /**
   * Note that a string is used instead of a token as a key.
   * 
   * A token represent a unit of code at a specific place, so we'll use a raw
   * string to
   * ensure all of those tokens refer to the same map key.
   */
  private final Map<String, Object> values = new HashMap<>();

  public void define(String name, Object value) {
    values.put(name, value);
  }

  public Object get(Token name) {
    if (values.containsKey(name.lexeme)) {
      return values.get(name.lexeme);
    }

    if (enclosing != null) {
      return enclosing.get(name);
    }

    throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
  }

  public Object getAt(Integer distance, String name) {
    return ancestor(distance).values.get(name);
  }

  private Environment ancestor(Integer distance) {
    Environment environment = this;
    for (int i = 0; i < distance; i++) {
      environment = environment.enclosing;
    }

    return environment;
  }

  public void assign(Token name, Object value) {
    if (values.containsKey(name.lexeme)) {
      values.put(name.lexeme, value);
      return;
    }

    if (enclosing != null) {
      enclosing.assign(name, value);
      return;
    }

    throw new RuntimeError(name,
        "Undefined variable '" + name.lexeme + "'.");
  }

  public void assignAt(Integer distance, Token name, Object value) {
    ancestor(distance).values.put(name.lexeme, value);
  }

}
