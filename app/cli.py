from __future__ import annotations

import argparse
import json
from datetime import datetime

from .analysis import VideoEvent, build_child_behavior_report, summarize_long_video_day, tag_short_video


def main() -> None:
    parser = argparse.ArgumentParser(description="幼儿行为分析 MVP")
    parser.add_argument("--child-id", default="child-001")
    parser.add_argument("--segments", nargs="*", default=[])
    parser.add_argument("--long-video", action="store_true")
    args = parser.parse_args()

    if args.long_video:
        now = datetime.now().replace(minute=0, second=0, microsecond=0)
        events = [
            VideoEvent(args.child_id, now.replace(hour=9), "晨间活动主动分享玩具", 0.91),
            VideoEvent(args.child_id, now.replace(hour=10), "小组讨论中积极回答问题", 0.88),
            VideoEvent(args.child_id, now.replace(hour=10), "午前整理环节遵守排队规则", 0.93),
        ]
        print(json.dumps(summarize_long_video_day(events), ensure_ascii=False, indent=2))
        return

    tags = tag_short_video(args.segments)
    report = build_child_behavior_report(args.child_id, tags)
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
