package org.example.tokenizer;

import com.github.javaparser.JavaToken;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class JavaParserToken {

  static String Function = "function";
  static String Variable = "variable";

  public static String getJavaTokens(String javaCode, String t) {
    if(Function.equals(t) || Variable.equals(t)) {
      javaCode = "class A {" + javaCode + "}";
    }
    CompilationUnit compilationUnit = StaticJavaParser.parse(javaCode);
    // 获取所有 tokens
    TokenRange javaTokens = compilationUnit.getTokenRange().orElse(null);
    ArrayList<String> tokens = new ArrayList<>();
    if (javaTokens != null) {
      // 遍历所有 token
      for (JavaToken token : javaTokens) {
        if(!" ".equals(token.getText()) && !"".equals(token.getText()) && token.getText() != null) {
          tokens.add(token.getText());
        }
      }
    }
    if(Function.equals(t) || Variable.equals(t)) {
      tokens.remove(0);
      tokens.remove(0);
      tokens.remove(0);
      tokens.remove(tokens.size() - 1);
    }
    return StringUtils.join(tokens, " ");
  }
}
