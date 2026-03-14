package com.example.behavior;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class BehaviorCli {

    public static void main(String[] args) throws Exception {
        CliArgs cliArgs = CliArgs.parse(args);
        BehaviorAnalysisService service = new BehaviorAnalysisService();

        if (cliArgs.videoFile() != null && cliArgs.transcriptFile() != null) {
            VideoContentExtractor extractor = new VideoContentExtractor(new SystemCommandRunner());
            VideoMetadata metadata = extractor.extractMetadata(Path.of(cliArgs.videoFile()));
            List<String> transcript = extractor.loadTranscriptSegments(Path.of(cliArgs.transcriptFile()));
            Map<String, Object> report = service.analyzeVideoWithTranscript(cliArgs.childId(), metadata, transcript);
            printMap(report);
            return;
        }

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

    private record CliArgs(String childId, List<String> segments, boolean longVideo, String videoFile, String transcriptFile) {
        static CliArgs parse(String[] args) {
            String childId = "child-001";
            boolean longVideo = false;
            String videoFile = null;
            String transcriptFile = null;
            List<String> segments = List.of();

            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--child-id" -> {
                        if (i + 1 < args.length) {
                            childId = args[++i];
                        }
                    }
                    case "--video-file" -> {
                        if (i + 1 < args.length) {
                            videoFile = args[++i];
                        }
                    }
                    case "--transcript-file" -> {
                        if (i + 1 < args.length) {
                            transcriptFile = args[++i];
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
            return new CliArgs(childId, segments, longVideo, videoFile, transcriptFile);
        }
    }
}
