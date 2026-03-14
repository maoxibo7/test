# 幼儿短视频/长视频行为分析 MVP（Java 版）

本仓库已重写为 **Java 实现**，用于演示两个核心能力：

- **短视频**：对已提取的文本片段进行行为标签识别，并生成儿童行为报告。
- **长视频**：对全天行为事件进行自动汇总，生成工作总结草稿。

> 当前仍是规则引擎 MVP，输入是“文本片段/事件数据”，不是直接读取视频文件。

## 技术栈

- Java 17
- 纯 Java 标准库（无第三方依赖）

## 本地编译与测试（无 Maven 依赖）

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java src/test/java -name "*.java")
java -cp out com.example.behavior.BehaviorAnalysisServiceTest
```

## 运行 CLI

```bash
java -cp out com.example.behavior.BehaviorCli --child-id child-001 --segments 认真听讲并举手 活动后排队收纳
java -cp out com.example.behavior.BehaviorCli --long-video
```

## 目录结构

- `src/main/java/com/example/behavior/BehaviorAnalysisService.java`：核心分析逻辑
- `src/main/java/com/example/behavior/BehaviorCli.java`：CLI 入口
- `src/test/java/com/example/behavior/BehaviorAnalysisServiceTest.java`：测试入口
