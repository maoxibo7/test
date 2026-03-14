from __future__ import annotations

from collections import Counter, defaultdict
from dataclasses import dataclass
from datetime import datetime
from statistics import mean
from typing import Iterable


@dataclass(frozen=True)
class TagRule:
    """Keyword-based rule used for MVP video tagging."""

    tag: str
    keywords: tuple[str, ...]


@dataclass(frozen=True)
class VideoEvent:
    """Atomic behavior event extracted from a video segment."""

    child_id: str
    timestamp: datetime
    description: str
    confidence: float


def _default_rules() -> tuple[TagRule, ...]:
    return (
        TagRule("专注学习", ("认真", "专注", "听讲", "阅读", "完成任务")),
        TagRule("积极互动", ("举手", "回答", "讨论", "合作", "分享")),
        TagRule("情绪波动", ("哭", "争执", "生气", "焦虑", "沮丧")),
        TagRule("运动发展", ("跑", "跳", "平衡", "投掷", "攀爬")),
        TagRule("规则意识", ("排队", "整理", "收纳", "遵守", "等待")),
    )


def tag_short_video(
    transcript_segments: Iterable[str],
    rules: Iterable[TagRule] | None = None,
) -> list[dict[str, float]]:
    """
    Tag short-video transcript segments with behavior labels.

    Returns per-segment score dicts, where a tag score is normalized to [0, 1].
    """
    active_rules = tuple(rules) if rules else _default_rules()
    scored_segments: list[dict[str, float]] = []

    for segment in transcript_segments:
        text = segment.strip()
        if not text:
            scored_segments.append({})
            continue

        segment_scores: dict[str, float] = {}
        for rule in active_rules:
            hit_count = sum(1 for kw in rule.keywords if kw in text)
            if hit_count:
                segment_scores[rule.tag] = round(hit_count / len(rule.keywords), 2)

        scored_segments.append(segment_scores)

    return scored_segments


def build_child_behavior_report(
    child_id: str,
    tagged_segments: Iterable[dict[str, float]],
) -> dict[str, object]:
    """Aggregate segment tags into an easy-to-read child behavior report."""
    tag_counter: Counter[str] = Counter()
    tag_scores: defaultdict[str, list[float]] = defaultdict(list)

    total_segments = 0
    for segment in tagged_segments:
        total_segments += 1
        for tag, score in segment.items():
            tag_counter[tag] += 1
            tag_scores[tag].append(score)

    top_tags = [tag for tag, _ in tag_counter.most_common(3)]
    avg_scores = {tag: round(mean(scores), 2) for tag, scores in tag_scores.items()}

    return {
        "child_id": child_id,
        "total_segments": total_segments,
        "observed_tags": dict(tag_counter),
        "top_tags": top_tags,
        "avg_tag_scores": avg_scores,
        "summary": _render_summary(top_tags, avg_scores),
    }


def summarize_long_video_day(events: Iterable[VideoEvent]) -> dict[str, object]:
    """
    Build daily auto-summary from long-video events.

    This MVP keeps logic deterministic and explainable for teacher review.
    """
    sorted_events = sorted(events, key=lambda x: x.timestamp)
    if not sorted_events:
        return {
            "event_count": 0,
            "peak_period": None,
            "confidence_avg": 0.0,
            "work_summary": "今日未采集到可分析事件。",
        }

    by_hour: Counter[int] = Counter(event.timestamp.hour for event in sorted_events)
    peak_hour, _ = by_hour.most_common(1)[0]
    confidence_avg = round(mean(e.confidence for e in sorted_events), 2)

    descriptions = [e.description for e in sorted_events]
    highlights = "；".join(descriptions[:3])

    return {
        "event_count": len(sorted_events),
        "peak_period": f"{peak_hour:02d}:00-{peak_hour:02d}:59",
        "confidence_avg": confidence_avg,
        "work_summary": (
            f"全天共识别{len(sorted_events)}条关键行为事件，"
            f"高峰时段在{peak_hour:02d}点，"
            f"模型平均置信度{confidence_avg}。"
            f"重点片段：{highlights}。"
        ),
    }


def _render_summary(top_tags: list[str], avg_scores: dict[str, float]) -> str:
    if not top_tags:
        return "本次视频未识别出明显行为标签，建议补充更多活动片段。"

    top_desc = "、".join(top_tags)
    strongest_tag = max(avg_scores, key=avg_scores.get) if avg_scores else top_tags[0]
    return (
        f"主要行为表现为：{top_desc}。"
        f"当前最稳定标签是“{strongest_tag}”，"
        "可据此生成家园沟通建议与后续干预计划。"
    )
