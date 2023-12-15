package tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/**
 * Used as a AST generator to gene rate various tree types.
 */
public class GenerateAst {
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
      System.err.println("Usage: generate_ast <output directory>");
      System.exit(64);
    }

    String outputDir = args[0];

    defineAst(outputDir, "Expr",
        Arrays.asList("Assign : Token name, Expr value",
            "Binary : Expr left, Token operator, Expr right",
            "Call: Expr callee, Token paren, List<Expr> arguments",
            "Get : Expr object, Token name",
            "Set : Expr object, Token name, Expr value",
            "Super : Token keyword, Token method",
            "This : Token keyword",
            "Grouping : Expr expression",
            "Literal : Object value",
            "Logical : Expr left, Token operator, Expr right",
            "Unary : Token operator, Expr right",
            "Variable : Token name"));

    defineAst(outputDir, "Stmt",
        Arrays.asList("Block : List<Stmt> statements",
            "Class      : Token name, Expr.Variable superclass," +
                " List<Stmt.Function> methods",
            "Expression : Expr expression",
            "Function : Token name, List<Token> params, List<Stmt> body",
            "If    : Expr condition, Stmt thenBranch, List<Elif> elseIfBranches, Stmt elseBranch",
            "Elif  : Expr condition, Stmt body",
            "Print : Expr expression",
            "Return: Token keyword, Expr value",
            "Var   : Token name, Expr initializer",
            "While : Expr condition, Stmt body"));

  }

  private static void defineAst(String outputDir, String baseName, List<String> exprTypes) throws IOException {
    String path = outputDir + "/" + baseName + ".java";
    PrintWriter writer = new PrintWriter(path, "UTF-8");

    writer.println("package lango.astNodes;");
    writer.println();
    writer.println("import java.util.List;");
    writer.println("import lango.scanner.Token;");
    writer.println();
    writer.println("public abstract class " + baseName + " {");

    defineVisitor(writer, baseName, exprTypes);
    // Base accept method that each expression type should implement.
    writer.println();
    writer.println("public abstract <R> R accept(Visitor<R> visitor);");
    for (String type : exprTypes) {
      List<String> classNameAndFields = extractSubClass(type);
      defineType(writer, baseName, classNameAndFields.get(0), classNameAndFields.get(1));
    }

    writer.println("}");
    writer.close();
  }

  /**
   * Define the types (aka subclasses) of expressions that extend from base
   * expression
   * 
   * @param writer    the writer that writes to the output file.
   * @param baseName  base class name.
   * @param className subclass name.
   * @param fieldList fields for each subclass.
   */
  private static void defineType(PrintWriter writer, String baseName, String className, String fieldList) {
    writer.println("public static class " + className + " extends " + baseName + " {");

    // Constructor of subclass.
    writer.println("    " + "public " + className + "(" + fieldList + ") {");

    // Assign object fields to constructor's parameters.

    String[] fields = fieldList.split(", ");
    for (String field : fields) {
      String name = field.split(" ")[1];
      writer.println("      this." + name + " = " + name + ";");
    }

    writer.println("    }");

    // Define overridden accept method.
    writer.println();
    writer.println("  @Override");
    writer.println("public  <R> R accept(Visitor<R> visitor) {");
    writer.println("    return visitor.visit" + className + baseName + "(this);");
    writer.println("    }");

    // Fields of the subclass.
    writer.println();
    for (String field : fields) {
      writer.println("public" + "    final " + field + ";");
    }

    writer.println("    }");
  }

  /**
   * Generates a visitor interface (to implement visitor pattern).
   * 
   * @param writer    the writer that adds the interface.
   * @param baseName
   * @param exprTypes
   */
  private static void defineVisitor(PrintWriter writer, String baseName, List<String> exprTypes) {
    writer.println("public interface Visitor<R> {");

    for (String type : exprTypes) {
      String typeName = type.split(":")[0].trim();
      writer.println("  R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
    }

    writer.println("  }");
  }

  /**
   * Extracts class name and its fields from a given string.
   * 
   * @param type represents the string that holds the class name and the fields of
   *             the class.
   * @return a list contains the class name as first element and fields as second
   *         element.
   */
  private static List<String> extractSubClass(String type) {
    String[] splittedString = type.split(":");
    String className = splittedString[0].trim();
    String fields = splittedString[1].trim();
    return Arrays.asList(className, fields);
  }
}
