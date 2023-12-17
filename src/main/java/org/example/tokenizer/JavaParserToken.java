package org.example.tokenizer;

import com.github.javaparser.JavaToken;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.TokenRange;
import com.github.javaparser.ast.CompilationUnit;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class JavaParserToken {

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
  public static String getJavaTokens(String javaCode, String _id) {
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
//    StaticJavaParser.getParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_18);
    CompilationUnit compilationUnit = StaticJavaParser.parse(javaCode);
    // 获取所有 tokens
    TokenRange javaTokens = compilationUnit.getTokenRange().orElse(null);
    ArrayList<String> tokens = new ArrayList<>();
    if (javaTokens != null) {
      // 遍历所有 token
      for (JavaToken token : javaTokens) {
        if(!" ".equals(token.getText()) && !"".equals(token.getText()) && token.getText() != null && !"\n".equals(token.getText())) {
          // 解决分词的时候字符串常量里有空格，导致分词失败
          if(token.getKind() == 93) {
//            if("<INIT>".equals(token.getText())) {
//              tokens.add("<INIT>");
//            } else {
//              tokens.add("<STRING_LITERAL>");
//            }
            tokens.add("<STRING_LITERAL>");
          } else {
            tokens.add(token.getText());
          }
        }
      }
    }
    if(Function.equals(t) || Variable.equals(t)) {
      tokens.remove(0);
      tokens.remove(0);
      tokens.remove(0);
      tokens.remove(tokens.size() - 1);
    }
    String joinTokens = StringUtils.join(tokens, " ");
    if (Class.equals(t) || Interface.equals(t)) {
      joinTokens = prefix + joinTokens;
    }
    return joinTokens;
  }
}
