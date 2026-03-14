import unittest
from datetime import datetime

from app.analysis import (
    VideoEvent,
    build_child_behavior_report,
    summarize_long_video_day,
    tag_short_video,
)


class AnalysisTests(unittest.TestCase):
    def test_short_video_tagging_and_report(self) -> None:
        segments = [
            "小朋友认真听讲并主动举手回答问题",
            "活动后主动收纳并排队等待",
            "因为争执出现短暂哭闹但很快平复",
        ]
        tagged = tag_short_video(segments)

        self.assertEqual(len(tagged), 3)
        self.assertIn("专注学习", tagged[0])
        self.assertIn("积极互动", tagged[0])
        self.assertIn("规则意识", tagged[1])
        self.assertIn("情绪波动", tagged[2])

        report = build_child_behavior_report("child-007", tagged)
        self.assertEqual(report["child_id"], "child-007")
        self.assertEqual(report["total_segments"], 3)
        self.assertTrue(report["top_tags"])

    def test_long_video_summary(self) -> None:
        events = [
            VideoEvent("child-007", datetime(2026, 1, 1, 9, 1), "晨检完成", 0.95),
            VideoEvent("child-007", datetime(2026, 1, 1, 9, 35), "活动中积极互动", 0.90),
            VideoEvent("child-007", datetime(2026, 1, 1, 11, 20), "餐前洗手并排队", 0.92),
        ]

        summary = summarize_long_video_day(events)
        self.assertEqual(summary["event_count"], 3)
        self.assertEqual(summary["peak_period"], "09:00-09:59")
        self.assertEqual(summary["confidence_avg"], 0.92)


if __name__ == "__main__":
    unittest.main()
