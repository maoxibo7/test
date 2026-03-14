package com.example.behavior;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class VideoContentExtractor {

    private final CommandRunner commandRunner;

    public VideoContentExtractor(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public VideoMetadata extractMetadata(Path videoPath) throws IOException, InterruptedException {
        if (!Files.exists(videoPath)) {
            throw new IOException("Video file not found: " + videoPath);
        }

        String durationRaw = commandRunner.run(
                "ffprobe", "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toString());

        String frameRateRaw = commandRunner.run(
                "ffprobe", "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=r_frame_rate",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoPath.toString());

        double durationSeconds = parseDouble(durationRaw);
        double frameRate = parseFrameRate(frameRateRaw);
        long fileSizeBytes = Files.size(videoPath);

        return new VideoMetadata(videoPath.toString(), durationSeconds, frameRate, fileSizeBytes);
    }

    public List<String> loadTranscriptSegments(Path transcriptPath) throws IOException {
        if (!Files.exists(transcriptPath)) {
            throw new IOException("Transcript file not found: " + transcriptPath);
        }

        return Files.readAllLines(transcriptPath, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty())
                .filter(line -> !line.matches("\\d+"))
                .filter(line -> !line.contains("-->"))
                .collect(Collectors.toList());
    }

    private double parseDouble(String value) {
        try {
            return Math.round(Double.parseDouble(value.trim()) * 100.0) / 100.0;
        } catch (NumberFormatException ex) {
            return 0.0;
        }
    }

    private double parseFrameRate(String value) {
        String text = value.trim();
        if (text.contains("/")) {
            List<String> parts = Arrays.stream(text.split("/")).toList();
            if (parts.size() == 2) {
                double numerator = parseDouble(parts.get(0));
                double denominator = parseDouble(parts.get(1));
                if (denominator > 0) {
                    return Math.round((numerator / denominator) * 100.0) / 100.0;
                }
            }
        }
        return parseDouble(text);
    }
}
