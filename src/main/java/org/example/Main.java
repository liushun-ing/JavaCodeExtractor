package org.example;

import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {

  static String RootPath = "D:\\pythonfile\\contextmodel\\git_repo_code";
  static String Class = "class";
  static String Interface = "interface";
  static String Function = "function";
  static String Variable = "variable";

  public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
    extractJavaCode("my_mylyn");
  }

  /**
   * extract java code for a project
   *
   * @param projectName project name ['my_mylyn', 'my_platform',...]
   */
  public static void extractJavaCode(String projectName) throws ParserConfigurationException, IOException, SAXException {
    // 使用 Paths.get() 创建 Path 对象
    Path rootPath = Paths.get(RootPath);
    // 使用 resolve() 方法拼接路径
    Path projectPath = rootPath.resolve(projectName);
    File projectFile = new File(projectPath.toUri());
    File[] modelList = projectFile.listFiles();
    assert modelList != null;
    for (File model : modelList) {
      if (!model.getName().equals("5840")) {
        continue;
      }
      System.out.println("----------------now progressing: " + model.getName());
      String absolutePath = model.getAbsolutePath();
      Path abPath = Paths.get(absolutePath);
      Path resolve = abPath.resolve("3_step_expanded_model.xml");
      File modelFile = resolve.toFile();
      // 如果model不存在，直接跳过
      if (!modelFile.exists()) {
        continue;
      }
      // 如果java_code目录已存在，则删除里面的所有文件
      Path java_code = abPath.resolve("java_code");
      File java_code_dir = java_code.toFile();
      if (java_code_dir.exists()) {
        FileUtil.deleteFile(java_code_dir);
      }
      java_code_dir.mkdir();
      // 读取xml文件
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document root = builder.parse(modelFile.getAbsolutePath());
      NodeList graphList = root.getElementsByTagName("graph");
      for (int i = 0; i < graphList.getLength(); i++) {
        Node graph = graphList.item(i);
        NamedNodeMap attributes = graph.getAttributes();
        String repoPath = attributes.getNamedItem("repo_path").getNodeValue();
        System.out.println("repoPath: " + repoPath);
        NodeList vertices = graph.getFirstChild().getChildNodes();
        // 分析每个vertex,提取代码
        for (int j = 0; j < vertices.getLength(); j++) {
          Node vertex = vertices.item(j);
          NamedNodeMap vertexAttr = vertex.getAttributes();
          String refId = vertexAttr.getNamedItem("ref_id").getNodeValue();
          String kind = vertexAttr.getNamedItem("kind").getNodeValue();
          String label = vertexAttr.getNamedItem("label").getNodeValue();
          // System.out.println("kind: " + kind);
          String finalCode = "";
          if (Class.equals(kind) || Interface.equals(kind)) {
            repoPath = repoPath.replace("\\doxygen", "");
            String originLabel = label;
            String originJavaFile = repoPath + "\\src\\" + originLabel.replace(".", "\\") + ".java";
            // System.out.println("origin_java_file: " + origin_java_file);
            // 可能存在节点是内部类的情况，递归不断往前找，知道找到一个存在的类，就是外部类
            while (!new File(originJavaFile).exists()) {
              int pos = originLabel.lastIndexOf(".");
              originLabel = originLabel.substring(0, pos);
              originJavaFile = repoPath + "\\src\\" + originLabel.replace(".", "\\") + ".java";
            }
            // 读文件
            String javaCode = FileUtils.readFileToString(new File(originJavaFile), StandardCharsets.UTF_8);
            finalCode = Class.equals(kind)
                ? SpoonUtil.spoonExtractClass(javaCode, label)
                : SpoonUtil.spoonExtractInterface(originJavaFile, label);
          } else if (Function.equals(kind) || Variable.equals(kind)) {
            String file = vertexAttr.getNamedItem("file").getNodeValue();
            int line = Integer.parseInt(vertexAttr.getNamedItem("line").getNodeValue());
            finalCode = Function.equals(kind)
                ? SpoonUtil.spoonExtractFunction(file, label, line)
                : SpoonUtil.spoonExtractVariable(file, label, line);
          }
          // 将抽取的代码保存到相应的文件
          String dest_java_file = java_code_dir.getAbsolutePath() + "\\" + model.getName() + '_' + kind + '_' + refId + ".java";
          FileUtils.writeStringToFile(new File(dest_java_file), finalCode, StandardCharsets.UTF_8);
          System.out.println(kind + " " + label + " extraction successes!!!");
        }
      }
    }
  }

}