package org.example;

import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class Main {

  static String RootPath = "/data0/shunliu/pythonfile/code_context_model_prediction/params_validation/git_repo_code";
  static String Class = "class";
  static String Interface = "interface";
  static String Function = "function";
  static String Variable = "variable";

  public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException {
    extractJavaCode("my_mylyn");
//    extractJavaCode("my_pde");
//    extractJavaCode("my_platform");
//    extractJavaCode("my_ecf");

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
    Path projectPath = rootPath.resolve(projectName).resolve("repo_first_3");
    File projectFile = new File(projectPath.toUri());
    File[] modelList = projectFile.listFiles();
    assert modelList != null;
    ArrayList<File> files = new ArrayList<>(Arrays.stream(modelList).toList());
    files.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
    boolean q = false;
    for (File model : files) {
//      System.out.println(model.getName());
      if (!q && model.getName().equals("3589")) {
        q = true;
      }
      if (!q) {
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
      // 读取xml文件
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder builder = factory.newDocumentBuilder();
      Document root = builder.parse(modelFile.getAbsolutePath());
      // 标题行
      String[] titleRow = {"id", "code"};
      // 数据行
      ArrayList<String[]> dataRows = new ArrayList<>();
      NodeList graphList = root.getElementsByTagName("graph");
      for (int i = 0; i < graphList.getLength(); i++) {
        Node graph = graphList.item(i);
        NamedNodeMap attributes = graph.getAttributes();
        String repoPath = attributes.getNamedItem("repo_path").getNodeValue();
//        System.out.println("repoPath: " + repoPath);
        NodeList vertices = graph.getFirstChild().getChildNodes();
        // 分析每个vertex,提取代码
        for (int j = 0; j < vertices.getLength(); j++) {
          Node vertex = vertices.item(j);
          NamedNodeMap vertexAttr = vertex.getAttributes();
          String refId = vertexAttr.getNamedItem("ref_id").getNodeValue();
          String kind = vertexAttr.getNamedItem("kind").getNodeValue();
          String label = vertexAttr.getNamedItem("label").getNodeValue();
//          System.out.println("--------------" + kind + " " + label);
//          System.out.println("kind: " + kind);
          String finalCode = "";
          if (Class.equals(kind) || Interface.equals(kind)) {
            repoPath = repoPath.replace("/doxygen", "");
            String originLabel = label;
            String originJavaFile = repoPath + "/src/" + originLabel.replace(".", "/") + ".java";
//            System.out.println("origin_java_file: " + originJavaFile);
            // 可能存在节点是内部类的情况，递归不断往前找，知道找到一个存在的类，就是外部类
            while (!new File(originJavaFile).exists()) {
              int pos = originLabel.lastIndexOf(".");
              originLabel = originLabel.substring(0, pos);
              originJavaFile = repoPath + "/src/" + originLabel.replace(".", "/") + ".java";
            }
            // 读文件
//            String javaCode = FileUtils.readFileToString(new File(originJavaFile), StandardCharsets.UTF_8);
            finalCode = Class.equals(kind)
                ? SpoonUtil.spoonExtractClass(originJavaFile, label)
                : SpoonUtil.spoonExtractInterface(originJavaFile, label);
          } else if (Function.equals(kind) || Variable.equals(kind)) {
            String file = vertexAttr.getNamedItem("file").getNodeValue();
            int line = Integer.parseInt(vertexAttr.getNamedItem("line").getNodeValue());
            finalCode = Function.equals(kind)
                ? SpoonUtil.spoonExtractFunction(file, label, line)
                : SpoonUtil.spoonExtractVariable(file, label, line);
          }
          // 将抽取的代码保存到tsv
          String _id = model.getName() + '_' + kind + '_' + refId;
          dataRows.add(new String[]{_id, finalCode});
//          System.out.println("extraction successes!!!");
        }
      }
      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(absolutePath + "/" + "my_java_codes.tsv"), StandardCharsets.UTF_8);
      // 1. 通过new CSVWriter对象的方式直接创建CSVWriter对象
      // CSVWriter csvWriter = new CSVWriter(writer);
      // 2. 通过CSVWriterBuilder构造器构建CSVWriter对象
      CSVWriter csvWriter = (CSVWriter) new CSVWriterBuilder(writer).withSeparator('\t').build();
      // 写入标题行
      csvWriter.writeNext(titleRow, false);
      // 写入数据行
      csvWriter.writeAll(dataRows, false);
      csvWriter.close();
      System.out.println(model.getName() + " done~~~~~");
    }
  }

}