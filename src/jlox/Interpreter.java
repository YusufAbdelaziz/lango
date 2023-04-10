package jlox;

import jlox.Expr.*;
import jlox.scanner.Token;
import jlox.scanner.TokenType;
import jlox.parser.*;

public class Interpreter implements Expr.Visitor<Object> {

  void interpret(Expr expr) {
    try {
      Object value = evaluate(expr);
      System.out.println(stringify(value));
    } catch (RuntimeError error) {
      JLox.runtimeError(error);
    }
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
        return (double) left / (double) right;
      case STAR:
        checkNumberOperands(expr.operator, right, left);
        return (double) left * (double) right;
      case PLUS:
        if (left instanceof String && right instanceof String) {
          return (String) left + (String) right;
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
   * Note that only false and nil are falsy values
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
}