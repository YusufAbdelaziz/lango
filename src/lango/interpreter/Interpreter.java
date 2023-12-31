package lango.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lango.Environment;
import lango.classes.LangoClass;
import lango.classes.LangoInstance;
import lango.Return;
import lango.Break;
import lango.astNodes.Expr;
import lango.functions.LangoCallable;
import lango.functions.LangoFunction;
import lango.astNodes.Stmt;
import lango.astNodes.Expr.*;
import lango.astNodes.Stmt.*;
import lango.main.Lango;
import lango.parser.*;
import lango.scanner.Token;
import lango.scanner.TokenType;

public class Interpreter implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

  /**
   * Fixed reference to the outermost global environment.
   */

  private final Environment globals = new Environment();

  /**
   * Stores the the distance for each expression declared in local scopes.
   */
  private final Map<Expr, Integer> locals = new HashMap<>();
  /**
   * Tracks current environment as it changes when we enter or exit local scopes.
   */
  private Environment environment = globals;

  public Interpreter() {
    defineGlobalFunctions();
  }

  private void defineGlobalFunctions() {
    globals.define("clock", new LangoCallable() {

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        return (double) System.currentTimeMillis() / 1000.0;
      }

      @Override
      public int arity() {
        return 0;
      }

      @Override
      public String toString() {
        return "native function";
      }
    });

    globals.define("print", new LangoCallable() {

      @Override
      public Object call(Interpreter interpreter, List<Object> arguments) {
        System.out.println(arguments.get(0));
        return null;
      }

      @Override
      public int arity() {
        return 1;
      }

      @Override
      public String toString() {
        return "native function";
      }
    });
  }

  public void interpret(List<Stmt> statements) {
    try {
      for (Stmt statement : statements) {
        execute(statement);
      }
    } catch (RuntimeError error) {
      Lango.runtimeError(error);
    }
  }

  private void execute(Stmt stmt) {
    stmt.accept(this);
  }

  public void resolve(Expr expr, int depth) {
    locals.put(expr, depth);
  }

  @Override
  public Object visitBinaryExpr(Binary expr) {
    Object left = evaluate(expr.left);
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case MINUS:
        checkNumberOperands(expr.operator, right, left);
        return (double) left - (double) right;
      case SLASH:
        checkNumberOperands(expr.operator, right, left);
        if ((double) right == 0) {
          throw new RuntimeError(expr.operator, "You can't divide by zero");
        }
        return (double) left / (double) right;
      case STAR:
        checkNumberOperands(expr.operator, right, left);
        return (double) left * (double) right;
      case PLUS:
        if (left instanceof String || right instanceof String) {
          return stringify(left) + stringify(right);

        }
        if (left instanceof Double && right instanceof Double) {
          return (double) left + (double) right;
        }
        throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
      case GREATER:
        checkNumberOperands(expr.operator, right, left);
        return (double) left > (double) right;
      case GREATER_EQUAL:
        checkNumberOperands(expr.operator, right, left);
        return (double) left >= (double) right;
      case LESS:
        checkNumberOperands(expr.operator, right, left);
        return (double) left < (double) right;
      case LESS_EQUAL:
        checkNumberOperands(expr.operator, right, left);
        return (double) left <= (double) right;
      case BANG_EQUAL:
        return !isEqual(left, right);
      case EQUAL_EQUAL:
        return isEqual(left, right);
      default:
        break;
    }
    return null;
  }

  @Override
  public Object visitGroupingExpr(Grouping expr) {
    return evaluate(expr.expression);
  }

  @Override
  public Object visitLiteralExpr(Literal expr) {
    return expr.value;
  }

  @Override
  public Object visitUnaryExpr(Unary expr) {
    Object right = evaluate(expr.right);

    switch (expr.operator.type) {
      case BANG:
        return !isTruthy(right);
      case MINUS:
        checkNumberOperand(expr.operator, right);
        return -(double) right;
      default:
        return null;

    }
  }

  /**
   * Checks if the operand/s is/are of type double to make sure an mathematical
   * operation is valid.
   * 
   * @param operator
   * @param operand
   */
  private void checkNumberOperand(Token operator, Object operand) {
    if (operand instanceof Double)
      return;
    throw new RuntimeError(operator, "Operand must be a number.");
  }

  private void checkNumberOperands(Token operator, Object right, Object left) {
    if (right instanceof Double && left instanceof Double)
      return;
    throw new RuntimeError(operator, "Operands must be numbers.");
  }

  private Object evaluate(Expr expr) {
    return expr.accept(this);
  }

  private boolean isEqual(Object a, Object b) {
    if (a == null && b == null)
      return true;
    if (a == null)
      return false;

    return a.equals(b);
  }

  private String stringify(Object object) {
    if (object == null)
      return "nil";

    if (object instanceof Double) {
      String text = object.toString();
      if (text.endsWith(".0")) {
        text = text.substring(0, text.length() - 2);
      }

      return text;
    }

    return object.toString();
  }

  /**
   * Checks if the [object] is truthful.
   * 
   * Note that only 'false' and 'nil' are falsy values
   * 
   * @param object
   * @return
   */
  private boolean isTruthy(Object object) {
    if (object == null)
      return false;
    if (object instanceof Boolean)
      return (boolean) object;

    return true;
  }

  @Override
  public Void visitExpressionStmt(Expression stmt) {
    evaluate(stmt.expression);
    return null;
  }

  @Override
  public Void visitPrintStmt(Print stmt) {
    Object value = evaluate(stmt.expression);
    System.out.println(stringify(value));
    return null;
  }

  @Override
  public Void visitVarStmt(Var stmt) {
    Object value = null;
    if (stmt.initializer != null) {
      value = evaluate(stmt.initializer);
    }

    environment.define(stmt.name.lexeme, value);
    return null;
  }

  @Override
  public Object visitVariableExpr(Variable expr) {
    return lookUpVariable(expr.name, expr);
  }

  private Object lookUpVariable(Token name, Expr expr) {
    Integer distance = locals.get(expr);
    if (distance != null) {
      return environment.getAt(distance, name.lexeme);
    } else {
      return globals.get(name);
    }
  }

  @Override
  public Object visitAssignExpr(Assign expr) {
    Object value = evaluate(expr.value);
    Integer distance = locals.get(expr);
    if (distance != null) {
      environment.assignAt(distance, expr.name, value);
    } else {
      globals.assign(expr.name, value);
    }
    /**
     * Assigned value is returned because assignment is an expression that can be
     * nested inside other expressions.
     * 
     * ```
     * var a = 4;
     * print a = 5; // "5"
     * ```
     */
    return value;
  }

  @Override
  public Void visitBlockStmt(Block stmt) {
    executeBlock(stmt.statements, new Environment(environment));
    return null;
  }

  @Override
  public Void visitClassStmt(Stmt.Class stmt) {
    Object superclass = null;
    if (stmt.superclass != null) {
      superclass = evaluate(stmt.superclass);
      if (!(superclass instanceof LangoClass)) {
        throw new RuntimeError(stmt.superclass.name,
            "Superclass must be a class.");
      }
    }

    environment.define(stmt.name.lexeme, null);

    if (stmt.superclass != null) {
      environment = new Environment(environment);
      environment.define("super", superclass);
    }

    Map<String, LangoFunction> methods = new HashMap<>();
    for (Stmt.Function method : stmt.methods) {
      LangoFunction function = new LangoFunction(method, environment, method.name.lexeme.equals("init"));
      methods.put(method.name.lexeme, function);
    }

    LangoClass klass = new LangoClass(stmt.name.lexeme, (LangoClass) superclass, methods);

    if (superclass != null) {
      environment = environment.enclosing;
    }

    environment.assign(stmt.name, klass);
    return null;
  }

  public void executeBlock(List<Stmt> statements, Environment environment) {
    Environment previous = this.environment;

    try {
      this.environment = environment;
      for (Stmt stmt : statements) {
        execute(stmt);
      }
    } finally {
      this.environment = previous;
    }
  }

  @Override
  public Void visitIfStmt(If stmt) {
    if (isTruthy(evaluate(stmt.condition))) {
      execute(stmt.thenBranch);
    } else if (!stmt.elseIfBranches.isEmpty()) {
      for (Elif elseIfStatement : stmt.elseIfBranches) {
        if (isTruthy(evaluate(elseIfStatement.condition))) {
          execute(elseIfStatement.body);
          break;
        }
      }
    } else if (stmt.elseBranch != null) {
      execute(stmt.elseBranch);
    }

    return null;
  }

  @Override
  public Void visitElifStmt(Elif stmt) {
    if (isTruthy(stmt.condition))
      execute(stmt.body);
    return null;
  }

  @Override
  public Object visitLogicalExpr(Logical expr) {
    Object left = evaluate(expr.left);
    if (expr.operator.type == TokenType.OR) {
      if (isTruthy(left))
        return left;
    } else {
      if (!isTruthy(left))
        return left;
    }

    return evaluate(expr.right);
  }

  @Override
  public Object visitSetExpr(Set expr) {
    Object object = evaluate(expr.object);

    if (!(object instanceof LangoInstance)) {
      throw new RuntimeError(expr.name,
          "Only instances have fields.");
    }

    Object value = evaluate(expr.value);

    ((LangoInstance) object).set(expr.name, value);
    return value;
  }

  @Override
  public Object visitSuperExpr(Super expr) {
    int distance = locals.get(expr);
    LangoClass superclass = (LangoClass) environment.getAt(
        distance, "super");

    LangoInstance object = (LangoInstance) environment.getAt(
        distance - 1, "this");

    LangoFunction method = superclass.findMethod(expr.method.lexeme);

    if (method == null) {
      throw new RuntimeError(expr.method,
          "Undefined property '" + expr.method.lexeme + "'.");
    }

    return method.bind(object);
  }

  @Override
  public Object visitThisExpr(Expr.This expr) {
    return lookUpVariable(expr.keyword, expr);
  }

  @Override
  public Void visitWhileStmt(Stmt.While stmt) {
    while (isTruthy(evaluate(stmt.condition))) {
      try {
        execute(stmt.body);
      } catch (Break b) {
        break;
      }
    }
    return null;
  }

  @Override
  public Object visitCallExpr(Call expr) {
    Object callee = evaluate(expr.callee);

    List<Object> arguments = new ArrayList<>();

    for (Expr argument : expr.arguments) {
      arguments.add(evaluate(argument));
    }

    if (!(callee instanceof LangoCallable)) {
      throw new RuntimeError(expr.paren, "Can only call functions and classes");
    }

    LangoCallable function = (LangoCallable) callee;

    if (arguments.size() != function.arity()) {
      throw new RuntimeError(expr.paren, "Expected " +
          function.arity() + " arguments but got " +
          arguments.size() + ".");
    }
    return function.call(this, arguments);
  }

  @Override
  public Object visitGetExpr(Get expr) {
    Object object = evaluate(expr.object);

    if (object instanceof LangoInstance) {
      return ((LangoInstance) object).get(expr.name);
    }

    throw new RuntimeError(expr.name, "Only instances have properties.");
  }

  @Override
  public Void visitFunctionStmt(Function stmt) {
    LangoFunction function = new LangoFunction(stmt, environment, false);
    environment.define(stmt.name.lexeme, function);
    return null;
  }

  @Override
  public Void visitReturnStmt(Stmt.Return stmt) {
    Object value = null;
    if (stmt.value != null)
      value = evaluate(stmt.value);

    throw new Return(value);
  }

  @Override
  public Void visitBreakStmt(Stmt.Break stmt) {
    throw new Break();
  }

  @Override
  public Object visitAnonymousFuncExpr(AnonymousFunc expr) {
    return new LangoFunction(new Stmt.Function(null, expr.params, expr.body), environment, false);
  }
}