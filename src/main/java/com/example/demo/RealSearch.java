package com.example.demo;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RealSearch implements Search {
    private TextFileRepository textFileRepository;
    private LoggingListener loggingListener;

    public RealSearch(TextFileRepository textFileRepository, LoggingListener loggingListener) {
        this.textFileRepository = textFileRepository;
        this.loggingListener = loggingListener;
    }

    @Override
    public List<TextFile> search(String query) {

        QueryParser parser = new QueryParser(query);

        List<TextFile> results = textFileRepository.findAll().stream()
                .filter(file -> {
                    boolean matches = true;

                    if (parser.has("content")) {
                        matches &= parser.get("content").stream()
                                .allMatch(value -> file.getContent() != null && file.getContent().toLowerCase().contains(value));
                    }

                    if (parser.has("path")) {
                        matches &= parser.get("path").stream()
                                .allMatch(value -> file.getFilePath() != null && file.getFilePath().toLowerCase().contains(value));
                    }
                    if (parser.has("filename")) {
                        matches &= parser.get("filename").stream()
                                .allMatch(value -> file.getFilename() != null && file.getFilename().toLowerCase().contains(value));
                    }

                    return matches;
                })
                .peek(file -> {
                    if (parser.has("content")) {
                        long boost = parser.get("content").stream()
                                .mapToLong(loggingListener::getCountTerm)
                                .sum();
                        file.setRankingScore(Math.min(100.0,Math.max(0.0,file.getRankingScore() + boost)));
                    }
                })
                .sorted(Comparator.comparingDouble(TextFile::getRankingScore).reversed())
                .collect(Collectors.toList());

        return results;
    }
}
