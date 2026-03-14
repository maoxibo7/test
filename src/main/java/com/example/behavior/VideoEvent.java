package com.example.behavior;

import java.time.LocalDateTime;

public record VideoEvent(String childId, LocalDateTime timestamp, String description, double confidence) {
}
