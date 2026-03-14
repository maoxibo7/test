package com.example.behavior;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class BehaviorAnalysisServiceTest {

    private final BehaviorAnalysisService service = new BehaviorAnalysisService();

    public static void main(String[] args) {
        BehaviorAnalysisServiceTest test = new BehaviorAnalysisServiceTest();
        test.shortVideoTaggingAndReportShouldWork();
        test.longVideoSummaryShouldWork();
        System.out.println("ALL_TESTS_PASSED");
    }

    void shortVideoTaggingAndReportShouldWork() {
        List<String> segments = List.of(
                "小朋友认真听讲并主动举手回答问题",
                "活动后主动收纳并排队等待",
                "因为争执出现短暂哭闹但很快平复"
        );

        List<Map<String, Double>> tagged = service.tagShortVideo(segments, null);
        assertTrue(tagged.size() == 3, "Expected 3 tagged segments");
        assertTrue(tagged.get(0).containsKey("专注学习"), "Segment 1 missing 专注学习");
        assertTrue(tagged.get(0).containsKey("积极互动"), "Segment 1 missing 积极互动");
        assertTrue(tagged.get(1).containsKey("规则意识"), "Segment 2 missing 规则意识");
        assertTrue(tagged.get(2).containsKey("情绪波动"), "Segment 3 missing 情绪波动");

        Map<String, Object> report = service.buildChildBehaviorReport("child-007", tagged);
        assertTrue("child-007".equals(report.get("childId")), "childId mismatch");
        assertTrue(((Integer) report.get("totalSegments")) == 3, "totalSegments mismatch");
        assertTrue(!((List<?>) report.get("topTags")).isEmpty(), "topTags should not be empty");
    }

    void longVideoSummaryShouldWork() {
        List<VideoEvent> events = List.of(
                new VideoEvent("child-007", LocalDateTime.of(2026, 1, 1, 9, 1), "晨检完成", 0.95),
                new VideoEvent("child-007", LocalDateTime.of(2026, 1, 1, 9, 35), "活动中积极互动", 0.90),
                new VideoEvent("child-007", LocalDateTime.of(2026, 1, 1, 11, 20), "餐前洗手并排队", 0.92)
        );

        Map<String, Object> summary = service.summarizeLongVideoDay(events);
        assertTrue(((Integer) summary.get("eventCount")) == 3, "eventCount mismatch");
        assertTrue("09:00-09:59".equals(summary.get("peakPeriod")), "peakPeriod mismatch");
        assertTrue(Math.abs((Double) summary.get("confidenceAvg") - 0.92) < 0.0001, "confidenceAvg mismatch");
    }

    private void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
    }
}
