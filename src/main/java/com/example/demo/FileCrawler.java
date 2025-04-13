package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import com.github.cliftonlabs.json_simple.JsonObject;
import org.springframework.web.bind.annotation.PostMapping;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;

import java.util.Optional;

@Service
public class FileCrawler {

    @Autowired
    private TextFileRepository repository;


    @Value("${folder.root.path}")
    private String folderPath;

    @Value("${report.format}")
    private String reportFormat;


    @PostConstruct
    public void initialize() {
            crawlAndStore();
    }
    private int totalFilesProcessed = 0;
    private int totalFilesSkipped = 0;
    private int totalErrors = 0;


    public void crawlAndStore() {
        try {
            Files.walkFileTree(Paths.get(folderPath).toAbsolutePath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".txt")) {
                        try {
                            String content = new String(Files.readAllBytes(file));
                            String firstThreeLines = content.lines().limit(3).reduce("", (a, b) -> a + "\n" + b);

                            String fullPath = file.toAbsolutePath().toString();

                            Optional<TextFile> existingFile = repository.findByFilePath(fullPath);
                            ZonedDateTime lastModifiedTime = ZonedDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), java.time.ZoneId.systemDefault());
                            if (existingFile.isPresent()) {
                                TextFile textFile = existingFile.get();
                                textFile.setRankingScore(Ranking.computeRankingScore(
                                        file,
                                        content,
                                        "txt",
                                        Files.size(file),
                                        Files.getLastModifiedTime(file).toMillis()
                                ));

                                if (!textFile.getContent().equals(content)) {
                                    textFile.setContent(content);
                                    textFile.setFirstThreeLines(firstThreeLines);
                                    textFile.setTimestamp(lastModifiedTime);
                                }
                                repository.save(textFile);
                                totalFilesProcessed++;
                            } else {
                                double rankingScore = Ranking.computeRankingScore(
                                        file,
                                        content,
                                        "txt",
                                        Files.size(file),
                                        Files.getLastModifiedTime(file).toMillis()
                                );

                                repository.save(new TextFile(
                                        fullPath,
                                        file.getFileName().toString(),
                                        "txt",
                                        firstThreeLines,
                                        lastModifiedTime,
                                        content,
                                        rankingScore
                                ));

                                totalFilesProcessed++;

                            }
                        } catch (IOException e) {
                            totalErrors++;
                            e.printStackTrace();
                        }
                    }else {
                        totalFilesSkipped++;
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    totalErrors++;
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            totalErrors++;
            e.printStackTrace();
        }
        generateReport();
    }


    private void generateTXTReport() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("index_report.txt"))) {
            writer.write("Indexing Summary\n");
            writer.write("Total Files Processed: " + totalFilesProcessed + "\n");
            writer.write("Total Files Skipped: " + totalFilesSkipped + "\n");
            writer.write("Total Errors: " + totalErrors + "\n");
            System.out.println("Indexing report generated: index_report.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void generateReport() {

            if (reportFormat.trim().equalsIgnoreCase("json")) {
                generateJsonReport();
            } else {
                generateTXTReport();
            }

    }
    private void generateJsonReport() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("filesProcessed", totalFilesProcessed);
        jsonObject.put("filesSkipped", totalFilesSkipped);
        jsonObject.put("errors", totalErrors);

        try (FileWriter file = new FileWriter("index_report.json")) {
            file.write(jsonObject.toJson());
            System.out.println("Indexing report generated: index_report.json");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}