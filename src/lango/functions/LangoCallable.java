package lango.functions;

import java.util.List;

import lango.interpreter.Interpreter;

public interface LangoCallable {

  Object call(Interpreter interpreter, List<Object> arguments);

  int arity();
}
