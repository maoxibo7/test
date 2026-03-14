package com.example.behavior;

import java.util.List;

public record AiVideoAnalysisResult(List<String> tags, String report, String rawModelContent) {
}
