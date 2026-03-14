# 幼儿短视频/长视频行为分析 MVP（Java 版）

本仓库是 **Java 实现**，覆盖三种输入方式：

- **短视频文本片段模式**：对文本片段打行为标签并生成报告。
- **长视频事件模式**：对全天事件列表做自动汇总。
- **视频+转写文件模式（新增）**：读取视频元信息（`ffprobe`）+ 转写文本（`srt/txt`）后自动打标签。

> 说明：当前不是端到端视觉识别模型；视频内容标签仍来自“转写文本 + 规则引擎”。

## 技术栈

- Java 17
- 纯 Java 标准库（无第三方依赖）
- 外部工具：`ffprobe`（仅用于读取视频元信息）

## 本地编译与测试

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java src/test/java -name "*.java")
java -cp out com.example.behavior.BehaviorAnalysisServiceTest
```

## 运行 CLI

### 1) 文本片段模式

```bash
java -cp out com.example.behavior.BehaviorCli --child-id child-001 --segments 认真听讲并举手 活动后排队收纳
```

### 2) 长视频事件模式

```bash
java -cp out com.example.behavior.BehaviorCli --long-video
```

### 3) 视频+转写文件模式（新增）

```bash
java -cp out com.example.behavior.BehaviorCli --child-id child-001 --video-file /path/demo.mp4 --transcript-file /path/demo.srt
```

## 目录结构

- `src/main/java/com/example/behavior/BehaviorAnalysisService.java`：核心分析逻辑
- `src/main/java/com/example/behavior/VideoContentExtractor.java`：视频元信息与转写加载
- `src/main/java/com/example/behavior/BehaviorCli.java`：CLI 入口
- `src/test/java/com/example/behavior/BehaviorAnalysisServiceTest.java`：测试入口
