package org.example.process;

import com.opencsv.*;
import com.opencsv.exceptions.CsvException;

import java.io.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class DataProcess {

  static String RootPath = "/data0/shunliu/pythonfile/code_context_model_prediction/params_validation/git_repo_code";

  public static void main(String[] args) throws IOException, CsvException {
    extractJavaTokens("my_ecf");
    extractJavaTokens("my_pde");
    extractJavaTokens("my_platform");
    extractJavaTokens("my_mylyn");
  }

  /**
   * data process
   * field initialization: all non literal -> 0, true, '0'; literal keep the same; all reference -> null
   * String -> < STRING_LITERAL >
   *
   * @param projectName project name ['my_mylyn', 'my_platform',...]
   */
  public static void extractJavaTokens(String projectName) throws IOException, CsvException {
    // 使用 Paths.get() 创建 Path 对象
    Path rootPath = Paths.get(RootPath);
    // 使用 resolve() 方法拼接路径
    Path projectPath = rootPath.resolve(projectName).resolve("repo_first_3");
    File projectFile = new File(projectPath.toUri());
    File[] modelList = projectFile.listFiles();
    assert modelList != null;
    ArrayList<File> files = new ArrayList<>(Arrays.stream(modelList).toList());
    files.sort(Comparator.comparingInt(o -> Integer.parseInt(o.getName())));
//    boolean q = false;
    for (File model : files) {
//      System.out.println(model.getName());
//      if (!q && model.getName().equals("5571")) {
//        q = true;
//      }
//      if (!q) {
//        continue;
//      }
      System.out.println("----------------now progressing: " + model.getName());
      String absolutePath = model.getAbsolutePath();
      Path abPath = Paths.get(absolutePath);
      Path resolve = abPath.resolve("my_java_codes.tsv");
      File codesFile = resolve.toFile();
      // 如果model不存在，直接跳过
      if (!codesFile.exists()) {
        continue;
      }
      // 标题行
      String[] titleRow = {"id", "tokens"};
      // 数据行
      ArrayList<String[]> dataRows = new ArrayList<>();

      InputStreamReader reader = new InputStreamReader(new FileInputStream(codesFile), StandardCharsets.UTF_8);
//      CSVParser build = new CSVParserBuilder().withSeparator('\t').build();
      // 解决读取时转义字符的\丢失问题
      RFC4180Parser rFC4180Parser = new RFC4180ParserBuilder().withSeparator('\t').build();
      CSVReader csvReader = new CSVReaderBuilder(reader).withCSVParser(rFC4180Parser).withSkipLines(1).build();
      List<String[]> list = csvReader.readAll();
      for (String[] s : list) {
//        System.out.println(s[0]);
        String codeString = s[1];
        String finalCodes = Processor.process(codeString, s[0]);
        dataRows.add(new String[]{s[0], finalCodes});
      }
      csvReader.close();

      OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(absolutePath + "/" + "processed_java_codes.tsv"), StandardCharsets.UTF_8);
      // 1. 通过new CSVWriter对象的方式直接创建CSVWriter对象
      // CSVWriter csvWriter = new CSVWriter(writer);
      // 2. 通过CSVWriterBuilder构造器构建CSVWriter对象
      CSVWriter csvWriter = (CSVWriter) new CSVWriterBuilder(writer).withSeparator('\t').build();
      // 写入标题行
      csvWriter.writeNext(titleRow, false);
      // 写入数据行
      csvWriter.writeAll(dataRows, false);
      csvWriter.close();
//      System.out.println(model.getName() + " done~~~~~");
    }
  }

}
