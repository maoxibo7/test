# 幼儿短视频/长视频行为分析 MVP

这个仓库提供一个最小可运行实现，覆盖你提出的两条路线：

- **短视频**：将已有用户视频文本片段进行行为标签识别，生成儿童行为报告。
- **长视频**：按全天事件流自动汇总，生成分析与工作总结草稿。

## 快速运行

```bash
python -m app.cli --child-id child-001 --segments "认真听讲并举手" "活动后排队收纳"
python -m app.cli --long-video
```

## 结构

- `app/analysis.py`：核心算法（标签、报告、全天汇总）
- `app/cli.py`：命令行入口
- `tests/test_analysis.py`：基础测试

## 说明

当前为规则引擎 MVP：可解释、易迭代，适合先跑园所试点。后续可将 `tag_short_video` 替换为多模态模型推理结果，并复用报告汇总层。
