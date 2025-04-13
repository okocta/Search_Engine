package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private TextFileRepository textFileRepository;

    private final EventManager eventManager= new EventManager();
    private final LoggingListener loggingListener = new LoggingListener();


    public SearchController() {
       eventManager.addObserver(loggingListener);
    }

    @GetMapping
    public List<TextFile> getAllFiles() {

        List<TextFile> files = textFileRepository.findAll();
        files.sort(Comparator.comparingDouble(TextFile::getRankingScore).reversed());
        return files;
    }

    @GetMapping("/search")
    public List<TextFile> search(@RequestParam String query) {
        eventManager.notifyObservers(query);
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

    @GetMapping("/find")
    public Optional<TextFile> findByFilePath(@RequestParam String filePath) {
        return textFileRepository.findByFilePath(filePath);
    }
    @GetMapping("/search/history")
    public List<String> getSearchHistory() {
        return loggingListener.getHistory(10);
    }


}
