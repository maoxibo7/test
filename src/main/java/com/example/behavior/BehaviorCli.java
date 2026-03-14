package com.example.behavior;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BehaviorCli {

    public static void main(String[] args) {
        CliArgs cliArgs = CliArgs.parse(args);
        BehaviorAnalysisService service = new BehaviorAnalysisService();

        if (cliArgs.longVideo()) {
            List<VideoEvent> events = service.sampleLongVideoEvents(cliArgs.childId());
            Map<String, Object> summary = service.summarizeLongVideoDay(events);
            printMap(summary);
            return;
        }

        List<Map<String, Double>> tagged = service.tagShortVideo(cliArgs.segments(), null);
        Map<String, Object> report = service.buildChildBehaviorReport(cliArgs.childId(), tagged);
        printMap(report);
    }

    private static void printMap(Map<String, ?> map) {
        map.forEach((k, v) -> System.out.println(k + ": " + v));
    }

    private record CliArgs(String childId, List<String> segments, boolean longVideo) {
        static CliArgs parse(String[] args) {
            String childId = "child-001";
            boolean longVideo = false;
            List<String> segments = List.of();

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--child-id" -> {
                        if (i + 1 < args.length) {
                            childId = args[++i];
                        }
                    }
                    case "--segments" -> {
                        if (i + 1 < args.length) {
                            segments = Arrays.asList(Arrays.copyOfRange(args, i + 1, args.length));
                            i = args.length;
                        }
                    }
                    case "--long-video" -> longVideo = true;
                    default -> {
                    }
                }
            }
            return new CliArgs(childId, segments, longVideo);
        }
    }
}
