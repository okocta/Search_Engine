package com.example.demo;

import ch.qos.logback.classic.spi.ILoggingEvent;
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

    private TextFileRepository textFileRepository;

    private final EventManager eventManager;
    private final LoggingListener loggingListener;
    private Search search;

    @Autowired
    public SearchController(TextFileRepository textFileRepository) {
       this.textFileRepository = textFileRepository;
       this.loggingListener= new LoggingListener();
       this.eventManager = new EventManager();
       this.eventManager.addObserver(loggingListener);
       RealSearch realSearch = new RealSearch(this.textFileRepository, this.loggingListener);
       this.search = new SearchProxy(realSearch);
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
        return search.search(query);
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
