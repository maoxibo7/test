"""Behavior analysis package for short and long classroom videos."""

from .analysis import (
    TagRule,
    VideoEvent,
    tag_short_video,
    build_child_behavior_report,
    summarize_long_video_day,
)

__all__ = [
    "TagRule",
    "VideoEvent",
    "tag_short_video",
    "build_child_behavior_report",
    "summarize_long_video_day",
]
