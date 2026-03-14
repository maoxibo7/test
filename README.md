# 幼儿短视频/长视频行为分析（Java + Qwen3-VL-Plus）

本仓库支持三种模式：

- 文本片段规则打标签（本地）
- 长视频事件汇总（本地）
- **短视频链接 AI 分析（阿里云 Qwen3-VL-Plus）**

## 1) 你要的“只输视频链接”模式

先配置阿里云 DashScope API Key：

```bash
export DASHSCOPE_API_KEY="你的Key"
```

执行：

```bash
java -cp out com.example.behavior.BehaviorCli --video-url "https://你的短视频链接.mp4"
```

也可显式传 key：

```bash
java -cp out com.example.behavior.BehaviorCli --video-url "https://你的短视频链接.mp4" --api-key "你的Key"
```

返回包括：
- `tags`：视频行为标签列表
- `report`：AI 生成行为报告
- `rawModelContent`：模型原始文本（方便排查）

## 2) 编译与测试

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src/main/java src/test/java -name "*.java")
java -cp out com.example.behavior.BehaviorAnalysisServiceTest
```

## 3) 其他兼容模式

文本片段：

```bash
java -cp out com.example.behavior.BehaviorCli --child-id child-001 --segments 认真听讲并举手 活动后排队收纳
```

长视频事件：

```bash
java -cp out com.example.behavior.BehaviorCli --long-video
```

## 说明

- AI 模型调用使用阿里云 OpenAI 兼容接口：`/compatible-mode/v1/chat/completions`。
- 当前实现通过 `video_url` + 文本指令让模型输出严格 JSON（`tags` + `report`）。
- 若视频链接无法公网访问，模型可能无法读取视频内容。
