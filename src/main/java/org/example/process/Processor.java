package org.example.process;

import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.Name;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.visitor.GenericVisitor;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class Processor {

  static String Class = "class";
  static String Interface = "interface";
  static String Function = "function";
  static String Variable = "variable";



  /**
   * transfer java source code to java tokens joined by " "
   *
   * @param javaCode java source code
   * @param _id element id
   * @return a string represents java tokens joined by " "
   */
  public static String process(String javaCode, String _id) {
    int firstIndex = _id.indexOf('_');
    _id = _id.substring(firstIndex + 1);
    int second_index = _id.indexOf('_');
    String t = _id.substring(0, second_index);
    String prefix = "";
    if(Function.equals(t) || Variable.equals(t)) {
      if (_id.contains("interfaceorg")) {
        javaCode = "interface A {" + javaCode + "}";
      } else {
        javaCode = "class A {" + javaCode + "}";
      }
    } else if(Class.equals(t)) {
      int i = javaCode.indexOf(Class);
      prefix = javaCode.substring(0, i);
      javaCode = javaCode.substring(i);
    } else {
      int i = javaCode.indexOf(Interface);
      prefix = javaCode.substring(0, i);
      javaCode = javaCode.substring(i);
    }
    String finalCode = "";
//    StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_18);
    CompilationUnit compilationUnit = StaticJavaParser.parse(javaCode);
    // 获取所有 tokens
    compilationUnit.accept(new MyVisitor(), null);
    if(Function.equals(t) || Variable.equals(t)) {
      if (_id.contains("interfaceorg")) {
        finalCode = compilationUnit.toString().substring(13, compilationUnit.toString().length() - 2);
      } else {
        finalCode = compilationUnit.toString().substring(9, compilationUnit.toString().length() - 2);
      }
    } else if (Class.equals(t) || Interface.equals(t)) {
      finalCode = prefix + compilationUnit;
    }
    return finalCode;
  }

  private static class MyVisitor extends VoidVisitorAdapter {
    @Override
    public void visit(FieldDeclaration n, Object arg) {
      super.visit(n, arg);
      n.getVariables().forEach(v ->
          v.getInitializer().ifPresent(i -> {
            v.setInitializer("<INIT>");
          }));
    }

    @Override
    public void visit(StringLiteralExpr n, Object arg) {
      super.visit(n, arg);
      n.setString("<STRING_LITERAL>");
    }
  }
}
