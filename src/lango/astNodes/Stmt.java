package lango.astNodes;

import java.util.List;
import lango.scanner.Token;

public abstract class Stmt {
  public interface Visitor<R> {
    R visitBlockStmt(Block stmt);

    R visitClassStmt(Class stmt);

    R visitExpressionStmt(Expression stmt);

    R visitFunctionStmt(Function stmt);

    R visitIfStmt(If stmt);

    R visitElifStmt(Elif stmt);

    R visitPrintStmt(Print stmt);

    R visitReturnStmt(Return stmt);

    R visitBreakStmt(Break stmt);

    R visitVarStmt(Var stmt);

    R visitWhileStmt(While stmt);
  }

  public abstract <R> R accept(Visitor<R> visitor);

  public static class Block extends Stmt {
    public Block(List<Stmt> statements) {
      this.statements = statements;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBlockStmt(this);
    }

    public final List<Stmt> statements;
  }

  public static class Class extends Stmt {
    public Class(Token name, Expr.Variable superclass, List<Stmt.Function> methods) {
      this.name = name;
      this.superclass = superclass;
      this.methods = methods;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitClassStmt(this);
    }

    public final Token name;
    public final Expr.Variable superclass;
    public final List<Stmt.Function> methods;
  }

  public static class Expression extends Stmt {
    public Expression(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitExpressionStmt(this);
    }

    public final Expr expression;
  }

  public static class Function extends Stmt {
    public Function(Token name, List<Token> params, List<Stmt> body) {
      this.name = name;
      this.params = params;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitFunctionStmt(this);
    }

    public final Token name;
    public final List<Token> params;
    public final List<Stmt> body;
  }

  public static class If extends Stmt {
    public If(Expr condition, Stmt thenBranch, List<Elif> elseIfBranches, Stmt elseBranch) {
      this.condition = condition;
      this.thenBranch = thenBranch;
      this.elseIfBranches = elseIfBranches;
      this.elseBranch = elseBranch;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitIfStmt(this);
    }

    public final Expr condition;
    public final Stmt thenBranch;
    public final List<Elif> elseIfBranches;
    public final Stmt elseBranch;
  }

  public static class Elif extends Stmt {
    public Elif(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitElifStmt(this);
    }

    public final Expr condition;
    public final Stmt body;
  }

  public static class Print extends Stmt {
    public Print(Expr expression) {
      this.expression = expression;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitPrintStmt(this);
    }

    public final Expr expression;
  }

  public static class Return extends Stmt {
    public Return(Token keyword, Expr value) {
      this.keyword = keyword;
      this.value = value;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitReturnStmt(this);
    }

    public final Token keyword;
    public final Expr value;
  }

  public static class Break extends Stmt {
    public Break(Token keyword) {
      this.keyword = keyword;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitBreakStmt(this);
    }

    public final Token keyword;
  }

  public static class Var extends Stmt {
    public Var(Token name, Expr initializer) {
      this.name = name;
      this.initializer = initializer;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitVarStmt(this);
    }

    public final Token name;
    public final Expr initializer;
  }

  public static class While extends Stmt {
    public While(Expr condition, Stmt body) {
      this.condition = condition;
      this.body = body;
    }

    @Override
    public <R> R accept(Visitor<R> visitor) {
      return visitor.visitWhileStmt(this);
    }

    public final Expr condition;
    public final Stmt body;
  }
}
