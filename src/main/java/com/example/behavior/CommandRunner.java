package com.example.behavior;

import java.io.IOException;

@FunctionalInterface
public interface CommandRunner {
    String run(String... command) throws IOException, InterruptedException;
}
