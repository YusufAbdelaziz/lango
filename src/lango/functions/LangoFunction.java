package lango.functions;

import java.util.List;

import lango.Environment;
import lango.classes.LangoInstance;
import lango.Return;
import lango.astNodes.Stmt;
import lango.interpreter.Interpreter;

public class LangoFunction implements LangoCallable {

  private final Stmt.Function declaration;
  /**
   * Defined the lexical scope (variables, etc.) surrounding the function
   * declaration.
   */
  private final Environment closure;
  private final boolean isInitializer;

  public LangoFunction(Stmt.Function declaration, Environment closure, boolean isInitializer) {
    this.isInitializer = isInitializer;
    this.declaration = declaration;
    this.closure = closure;

  }

  public LangoFunction bind(LangoInstance instance) {
    /// A closure is created to capture the "this" within the closure.
    Environment environment = new Environment(closure);
    environment.define("this", instance);
    return new LangoFunction(declaration, environment, isInitializer);
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    Environment environment = new Environment(closure);
    for (int i = 0; i < declaration.params.size(); i++) {
      environment.define(declaration.params.get(i).lexeme,
          arguments.get(i));
    }

    try {
      interpreter.executeBlock(declaration.body, environment);
    } catch (Return returnValue) {
      if (isInitializer)
        return closure.getAt(0, "this");
      return returnValue.value;
    }

    if (isInitializer)
      return closure.getAt(0, "this");
    return null;
  }

  @Override
  public int arity() {
    return declaration.params.size();
  }

  @Override
  public String toString() {
    return "<fn " + declaration.name.lexeme + ">";
  }
}
