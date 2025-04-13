package com.example.demo;

import java.nio.file.Path;

public class Ranking {

    public static double computeRankingScore(Path file, String content, String extension, long fileSize, long lastModified) {
        double score = 0.0;

        String path = file.toAbsolutePath().toString().toLowerCase();

        if (path.contains("business")) score += 25;
        else if (path.contains("politics")) score += 20;

        if (path.contains("politics")) score += 10;

        int depth = path.split("[/\\\\]").length;
        score += Math.max(0, 10 - depth);

        if (extension.equalsIgnoreCase("txt")) score += 10;

        double minSize = 1024.0;     // 1KB
        double maxSize = 30 * 1024.0; // 30KB
        double normalizedSize = Math.max(minSize, Math.min(fileSize, maxSize));
        double sizeScore = (maxSize - normalizedSize) / (maxSize - minSize) * 20.0;
        score += sizeScore;

        long now = System.currentTimeMillis();
        long ageMillis = now - lastModified;
        long daysOld = ageMillis / (1000 * 60 * 60 * 24);
        score += Math.max(0, (4 - daysOld) * 5);

        return Math.min(100.0, Math.max(0.0, score));
    }

}
