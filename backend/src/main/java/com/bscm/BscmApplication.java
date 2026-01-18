package com.bscm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BscmApplication {
  public static void main(String[] args) {
    // 设置JVM编码为UTF-8，确保控制台正确显示中文
    System.setProperty("file.encoding", "UTF-8");
    System.setProperty("console.encoding", "UTF-8");
    System.setProperty("user.language", "zh");
    System.setProperty("user.country", "CN");
    
    // 设置标准输出和错误输出的编码
    try {
      System.setOut(new java.io.PrintStream(System.out, true, "UTF-8"));
      System.setErr(new java.io.PrintStream(System.err, true, "UTF-8"));
    } catch (java.io.UnsupportedEncodingException e) {
      // 如果设置失败，继续运行（某些环境可能不支持）
    }
    
    SpringApplication.run(BscmApplication.class, args);
  }
}
