package com.solightingstats.git.statistics.plugin.utils;

import java.nio.file.Path;

public class FileUtils {
    public static String getOptimizedPath(Path path) {
        return path.toString().replaceAll("\\\\", "/");
    }
}
