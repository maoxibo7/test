package com.example.behavior;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BehaviorAnalysisService {

    public List<Map<String, Double>> tagShortVideo(List<String> transcriptSegments, List<TagRule> customRules) {
        List<TagRule> rules = customRules == null || customRules.isEmpty() ? defaultRules() : customRules;
        List<Map<String, Double>> scoredSegments = new ArrayList<>();

        for (String segment : transcriptSegments) {
            String text = segment == null ? "" : segment.trim();
            if (text.isEmpty()) {
                scoredSegments.add(new LinkedHashMap<>());
                continue;
            }

            Map<String, Double> segmentScores = new LinkedHashMap<>();
            for (TagRule rule : rules) {
                long hitCount = rule.keywords().stream().filter(text::contains).count();
                if (hitCount > 0) {
                    double score = round2((double) hitCount / rule.keywords().size());
                    segmentScores.put(rule.tag(), score);
                }
            }
            scoredSegments.add(segmentScores);
        }

        return scoredSegments;
    }

    public Map<String, Object> analyzeVideoWithTranscript(String childId, VideoMetadata metadata, List<String> transcriptSegments) {
        List<Map<String, Double>> taggedSegments = tagShortVideo(transcriptSegments, null);
        Map<String, Object> report = buildChildBehaviorReport(childId, taggedSegments);
        report.put("videoMetadata", metadata);
        report.put("inputMode", "video+transcript");
        return report;
    }

    public Map<String, Object> buildChildBehaviorReport(String childId, List<Map<String, Double>> taggedSegments) {
        Map<String, Integer> observedTags = new HashMap<>();
        Map<String, List<Double>> tagScores = new HashMap<>();

        for (Map<String, Double> segment : taggedSegments) {
            for (Map.Entry<String, Double> entry : segment.entrySet()) {
                observedTags.merge(entry.getKey(), 1, Integer::sum);
                tagScores.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(entry.getValue());
            }
        }

        List<String> topTags = observedTags.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .toList();

        Map<String, Double> avgTagScores = new LinkedHashMap<>();
        for (Map.Entry<String, List<Double>> entry : tagScores.entrySet()) {
            double avg = entry.getValue().stream().mapToDouble(v -> v).average().orElse(0.0);
            avgTagScores.put(entry.getKey(), round2(avg));
        }

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("childId", childId);
        report.put("totalSegments", taggedSegments.size());
        report.put("observedTags", observedTags);
        report.put("topTags", topTags);
        report.put("avgTagScores", avgTagScores);
        report.put("summary", renderSummary(topTags, avgTagScores));
        return report;
    }

    public Map<String, Object> summarizeLongVideoDay(List<VideoEvent> events) {
        if (events == null || events.isEmpty()) {
            return Map.of(
                    "eventCount", 0,
                    "peakPeriod", null,
                    "confidenceAvg", 0.0,
                    "workSummary", "今日未采集到可分析事件。"
            );
        }

        List<VideoEvent> sortedEvents = events.stream()
                .sorted(Comparator.comparing(VideoEvent::timestamp))
                .toList();

        Map<Integer, Long> hourCounter = sortedEvents.stream()
                .collect(Collectors.groupingBy(event -> event.timestamp().getHour(), Collectors.counting()));

        int peakHour = hourCounter.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(0);

        double confidenceAvg = round2(sortedEvents.stream().mapToDouble(VideoEvent::confidence).average().orElse(0.0));

        String highlights = sortedEvents.stream()
                .limit(3)
                .map(VideoEvent::description)
                .collect(Collectors.joining("；"));

        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("eventCount", sortedEvents.size());
        summary.put("peakPeriod", String.format("%02d:00-%02d:59", peakHour, peakHour));
        summary.put("confidenceAvg", confidenceAvg);
        summary.put("workSummary", "全天共识别" + sortedEvents.size() + "条关键行为事件，高峰时段在" + peakHour
                + "点，模型平均置信度" + confidenceAvg + "。重点片段：" + highlights + "。");
        return summary;
    }

    public List<TagRule> defaultRules() {
        return List.of(
                new TagRule("专注学习", List.of("认真", "专注", "听讲", "阅读", "完成任务")),
                new TagRule("积极互动", List.of("举手", "回答", "讨论", "合作", "分享")),
                new TagRule("情绪波动", List.of("哭", "争执", "生气", "焦虑", "沮丧")),
                new TagRule("运动发展", List.of("跑", "跳", "平衡", "投掷", "攀爬")),
                new TagRule("规则意识", List.of("排队", "整理", "收纳", "遵守", "等待"))
        );
    }

    public List<VideoEvent> sampleLongVideoEvents(String childId) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return List.of(
                new VideoEvent(childId, java.time.LocalDateTime.parse("2026-01-01 09:00:00", fmt), "晨间活动主动分享玩具", 0.91),
                new VideoEvent(childId, java.time.LocalDateTime.parse("2026-01-01 10:00:00", fmt), "小组讨论中积极回答问题", 0.88),
                new VideoEvent(childId, java.time.LocalDateTime.parse("2026-01-01 10:30:00", fmt), "午前整理环节遵守排队规则", 0.93)
        );
    }

    private String renderSummary(List<String> topTags, Map<String, Double> avgScores) {
        if (topTags.isEmpty()) {
            return "本次视频未识别出明显行为标签，建议补充更多活动片段。";
        }

        String topDesc = String.join("、", topTags);
        String strongestTag = avgScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(topTags.get(0));

        return "主要行为表现为：" + topDesc + "。当前最稳定标签是“" + strongestTag + "”，可据此生成家园沟通建议与后续干预计划。";
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
