package lango.classes;

import java.util.HashMap;
import java.util.Map;

import lango.functions.LangoFunction;
import lango.parser.RuntimeError;
import lango.scanner.Token;

public class LangoInstance {
  private LangoClass klass;
  private final Map<String, Object> fields = new HashMap<>();

  LangoInstance(LangoClass klass) {
    this.klass = klass;
  }

  public Object get(Token name) {
    if (fields.containsKey(name.lexeme)) {
      return fields.get(name.lexeme);
    }

    LangoFunction method = klass.findMethod(name.lexeme);
    if (method != null)
      return method.bind(this);

    throw new RuntimeError(name,
        "Undefined property '" + name.lexeme + "'.");
  }

  public void set(Token name, Object value) {
    fields.put(name.lexeme, value);
  }

  @Override
  public String toString() {
    return klass.name + " instance";
  }
}
