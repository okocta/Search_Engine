package com.example.demo;

import ch.qos.logback.classic.spi.ILoggingEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class SearchController {

    private TextFileRepository textFileRepository;

    private final EventManager eventManager;
    private final LoggingListener loggingListener;
    private Search search;
    private SpellService spellService;

    @Autowired
    public SearchController(TextFileRepository textFileRepository) throws IOException {
       this.textFileRepository = textFileRepository;
       this.loggingListener= new LoggingListener();
       this.eventManager = new EventManager();
       this.eventManager.addObserver(loggingListener);
       Spelling spelling= new Spelling("src/main/resources/big.txt");
       this.spellService=new SpellService(spelling);
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
    public Map<String,Object> search(@RequestParam String query) {
        eventManager.notifyObservers(query);
        String correctedQuery = spellService.correctQuery(query);
        System.out.println("corrected query: " + correctedQuery);
        List<TextFile> results =search.search(correctedQuery);
        Map<String, Long> type=results.stream().collect(Collectors.groupingBy(TextFile::getExtension, Collectors.counting()));
        Map<String, Long> year=results.stream().collect(Collectors.groupingBy(f->String.valueOf(f.getTimestamp().getYear()), Collectors.counting()));
        Map<String, Long> language=results.stream().filter(f->f.getExtension().equals("c")||f.getExtension().equals("java")).collect(Collectors.groupingBy(TextFile::getExtension, Collectors.counting()));
        Map<String, Object> response = new HashMap<>();
        response.put("results", results);
        response.put("correctedQuery", correctedQuery);
        response.put("type", type);
        response.put("year", year);
        response.put("language", language);
        return response;
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
