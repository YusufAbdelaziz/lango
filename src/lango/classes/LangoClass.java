package lango.classes;

import java.util.List;
import java.util.Map;

import lango.functions.LangoCallable;
import lango.functions.LangoFunction;
import lango.interpreter.Interpreter;

public class LangoClass implements LangoCallable {

  final String name;
  final LangoClass superclass;
  private Map<String, LangoFunction> methods;

  public LangoClass(String name, LangoClass superclass, Map<String, LangoFunction> methods) {
    this.name = name;
    this.methods = methods;
    this.superclass = superclass;
  }

  @Override
  public Object call(Interpreter interpreter, List<Object> arguments) {
    LangoInstance instance = new LangoInstance(this);

    LangoFunction initializer = findMethod("init");
    if (initializer != null) {
      initializer.bind(instance).call(interpreter, arguments);
    }

    return instance;
  }

  public LangoFunction findMethod(String name) {
    if (methods.containsKey(name)) {
      return methods.get(name);
    }

    if (superclass != null) {
      return superclass.findMethod(name);
    }

    return null;
  }

  @Override
  public int arity() {
    LangoFunction initializer = findMethod("init");
    if (initializer == null)
      return 0;
    return initializer.arity();
  }

  @Override
  public String toString() {
    return name;
  }

}