package com.example.demo;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/files")
@CrossOrigin(origins = "*")
public class SearchController {

    @Autowired
    private TextFileRepository textFileRepository;

    // List all files in folder_test
    @GetMapping
    public List<TextFile> getAllFiles() {
        return textFileRepository.findAll();
    }

    // Search files by name
    @GetMapping("/search")
    public List<TextFile> searchByFilename(@RequestParam String filename) {
        return textFileRepository.findByFilenameContainingIgnoreCase(filename);
    }

    @GetMapping("/search/content")
    public List<TextFile> searchByContent(@RequestParam String query) {
        try {
            // Sanitize input query for full-text search for multi-word search because there will be multiple '&' between words
            String fullTextQuery = sanitizeQuery(query);

            if (fullTextQuery.isEmpty()) {
                throw new IllegalArgumentException("Invalid search query.");
            }

            System.out.println("Query: " + fullTextQuery);
            return textFileRepository.searchByContent(fullTextQuery);
        } catch (Exception e) {
            System.err.println("Error during search: " + e.getMessage());
            throw new RuntimeException("An error occurred during the search process.");
        }
    }

    // Utility function to sanitize query input
    private String sanitizeQuery(String query) {
        // Split and remove empty words
        String[] words = query.trim().split("\\s+");

        // Join words with ' & ' ensuring valid syntax
        String sanitized = Arrays.stream(words)
                .filter(word -> word.matches("\\w+")) // Keep only valid words
                .collect(Collectors.joining(" & "));

        return sanitized.trim();
    }

    @GetMapping("/find")
    public Optional<TextFile> findByFilePath(@RequestParam String filePath) {
        return textFileRepository.findByFilePath(filePath);
    }

}
