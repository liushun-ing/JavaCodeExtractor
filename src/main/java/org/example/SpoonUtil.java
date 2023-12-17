package org.example;

import spoon.Launcher;
import spoon.reflect.CtModel;
import spoon.reflect.code.CtComment;
import spoon.reflect.declaration.*;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * <a href="https://github.com/INRIA/spoon">Spoon</a>
 */
public class SpoonUtil {
  /**
   * 使用spoon抽取代码
   *
   * @param path      原始代码路径
   * @param className 需要找的类名字
   * @return 抽取的类的名字
   */
  public static String spoonExtractClass(String path, String className) {
    // 创建 Spoon Launcher
    Launcher launcher = new Launcher();
    // 设置要解析的源代码路径
    launcher.addInputResource(path);
    // 执行 Spoon 解析
    launcher.run();
    // 获取 Spoon 模型
    CtModel model = launcher.getModel();
    CtClass<?> res = null;
    for (CtClass<?> ctClass : model.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtClass.class))) {
//      System.out.println(ctClass.getQualifiedName().replace("$", "."));
      if (ctClass.getQualifiedName().replace("$", ".").equals(className)) {
        res = ctClass;
        break;
      }
    }
    if (res == null) {
      throw new RuntimeException("class not exist");
    }
    // 去除开头的注解
    List<CtAnnotation<? extends Annotation>> annotations = res.getAnnotations();
    for (int i = annotations.size() - 1; i >= 0; i--) {
      res.removeAnnotation(annotations.get(i));
    }
    // 去除所有注释
    for (CtComment ctComment : res.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtComment.class))) {
      ctComment.delete();
    }
    // 为了避免他会将对象自动补充全包路径
    return res.prettyprint();
  }

  /**
   * extract interface
   *
   * @param path          文件路径
   * @param interfaceName interface qualified name
   * @return code
   */
  public static String spoonExtractInterface(String path, String interfaceName) {
    Launcher launcher = new Launcher();
    launcher.addInputResource(path);
    launcher.run();
    CtModel model = launcher.getModel();
    CtInterface<?> res = null;
    for (CtInterface<?> ctInterface : model.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtInterface.class))) {
      if (ctInterface.getQualifiedName().replace("$", ".").equals(interfaceName)) {
        res = ctInterface;
        break;
      }
    }
    if (res == null) {
      System.out.println(path);
      throw new RuntimeException("interface not exist");
    }
    List<CtAnnotation<? extends Annotation>> annotations = res.getAnnotations();
    for (int i = annotations.size() - 1; i >= 0; i--) {
      res.removeAnnotation(annotations.get(i));
    }
    for (CtComment ctComment : res.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtComment.class))) {
      ctComment.delete();
    }
    return res.prettyprint();
  }

  /**
   * extract variable
   *
   * @param path         文件路径
   * @param variableName field qualified name
   * @param line         line declaring field
   * @return code
   */
  public static String spoonExtractVariable(String path, String variableName, int line) {
    Launcher launcher = new Launcher();
    launcher.addInputResource(path);
    launcher.run();
    CtModel model = launcher.getModel();
    String name = variableName.substring(variableName.lastIndexOf(".") + 1);
    CtField<?> res = null;
    // 只有一个字段
    for (CtField<?> ctField : model.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtField.class))) {
      if (ctField.getSimpleName().equals(name) && ctField.getPosition().getLine() == line) {
        res = ctField;
        break;
      }
    }
    if (res == null) {
      throw new RuntimeException("field not exist");
    }
    List<CtAnnotation<? extends Annotation>> annotations = res.getAnnotations();
    for (int i = annotations.size() - 1; i >= 0; i--) {
      res.removeAnnotation(annotations.get(i));
    }
    for (CtComment ctComment : res.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtComment.class))) {
      ctComment.delete();
    }
    return res.prettyprint();
  }

  /**
   * extract function
   *
   * @param path         文件路径
   * @param functionName function qualified name
   * @param line         line declaring function
   * @return code
   */
  public static String spoonExtractFunction(String path, String functionName, int line) {
    Launcher launcher = new Launcher();
    launcher.addInputResource(path);
    launcher.run();
    CtModel model = launcher.getModel();
    functionName = functionName.substring(0, functionName.indexOf("("));
    String name = functionName.substring(functionName.lastIndexOf(".") + 1);
    CtElement res = null;
    // 只有一个方法
    for (CtMethod<?> ctMethod : model.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtMethod.class))) {
//      System.out.println(ctMethod.getSimpleName()+" "+name);
//      System.out.println(ctMethod.getPosition().getLine() +" "+ line);
      if (ctMethod.getSimpleName().equals(name) && ctMethod.getPosition().getLine() == line) {
        res = ctMethod;
        break;
      }
    }
    if (res == null) {
      for (CtConstructor<?> ctConstructor : model.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtConstructor.class))) {
//        System.out.println(ctConstructor + " " + name);
//        System.out.println(ctConstructor.getPosition().toString() + " " + line);
        if (ctConstructor.toString().contains(name) && ctConstructor.getPosition().toString().contains(":" + line + ")")) {
          res = ctConstructor;
          break;
        }
      }
    }
    if (res == null) {
      throw new RuntimeException("method not exist");
    }
    List<CtAnnotation<? extends Annotation>> annotations = res.getAnnotations();
    for (int i = annotations.size() - 1; i >= 0; i--) {
      res.removeAnnotation(annotations.get(i));
    }
    for (CtComment ctComment : res.getElements(new spoon.reflect.visitor.filter.TypeFilter<>(CtComment.class))) {
      ctComment.delete();
    }
    return res.prettyprint();
  }
}
