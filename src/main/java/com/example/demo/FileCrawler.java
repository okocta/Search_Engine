package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class FileCrawler {

    @Autowired
    private TextFileRepository repository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Value("${folder.root.path}")
    private String folderPath;
    private WatchService watchService;
    private final Map<WatchKey, Path> watchKeys = new HashMap<>();

    @PostConstruct
    public void initialize() {
        try {
            watchService = FileSystems.getDefault().newWatchService();
            crawlAndStore();
            startWatching();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private int totalFilesProcessed = 0;
    private int totalFilesSkipped = 0;
    private int totalErrors = 0;

    private void logProgress() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("index_report.txt"))) {
            writer.write("Indexing Summary\n");
            writer.write("Total Files Processed: " + totalFilesProcessed + "\n");
            writer.write("Total Files Skipped: " + totalFilesSkipped + "\n");
            writer.write("Total Errors: " + totalErrors + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void crawlAndStore() {
        try {
            // Start walking through the folder and subfolders using full paths
            Files.walkFileTree(Paths.get(folderPath).toAbsolutePath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.toString().endsWith(".txt")) {
                        try {
                            String content = new String(Files.readAllBytes(file));
                            String firstThreeLines = content.lines().limit(3).reduce("", (a, b) -> a + "\n" + b);

                            // Use full absolute path instead of relative
                            String fullPath = file.toAbsolutePath().toString();

                            Optional<TextFile> existingFile = repository.findByFilePath(fullPath);
                            ZonedDateTime lastModifiedTime = ZonedDateTime.ofInstant(Files.getLastModifiedTime(file).toInstant(), java.time.ZoneId.systemDefault());
                            if (existingFile.isPresent()) {
                                TextFile textFile = existingFile.get();
                                if (!textFile.getContent().equals(content)) {
                                    textFile.setContent(content);
                                    textFile.setFirstThreeLines(firstThreeLines);
                                    textFile.setTimestamp(lastModifiedTime);
                                    repository.save(textFile);
                                    totalFilesProcessed++;
                                }
                            } else {
                                repository.save(new TextFile(
                                        fullPath,
                                        file.getFileName().toString(),
                                        "txt",
                                        firstThreeLines,
                                        lastModifiedTime,
                                        content
                                ));
                                totalFilesProcessed++;

                            }
                        } catch (IOException e) {
                            totalErrors++;
                            e.printStackTrace();
                        }
                    }else {
                        totalFilesSkipped++;  // Increment skipped file count
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    // Register each subdirectory with the WatchService using full paths
                    registerDirectory(dir.toAbsolutePath());
                    return FileVisitResult.CONTINUE;
                }
                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    totalErrors++;  // Count failed visits
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            totalErrors++;
            e.printStackTrace();
        }
        generateReport();
    }

    private void registerDirectory(Path dir) throws IOException {
        // Ensure we register the directory properly
        WatchKey key = dir.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
        watchKeys.put(key, dir);
        System.out.println("Registered directory for watching: " + dir);
    }

    public void startWatching() {
        new Thread(() -> {
            try {
                System.out.println("Watching folder and subdirectories: " + folderPath);

                TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

                while (true) {
                    // Wait for a key to be ready
                    WatchKey key;
                    try {
                        key = watchService.take();  // Blocks until an event is available
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }

                    Path dir = watchKeys.get(key);

                    if (dir == null) {
                        System.err.println("WatchKey not recognized!");
                        continue;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {
                        WatchEvent.Kind<?> kind = event.kind();
                        Path changedFile = dir.resolve((Path) event.context());

                        // Use full absolute path for everything
                        String fullPath = changedFile.toAbsolutePath().toString();

                        System.out.println("Event kind: " + kind);
                        System.out.println("Changed file: " + fullPath);

                        if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                            System.out.println("New file detected: " + fullPath);

                            // If the new entry is a directory, register it for watching
                            if (Files.isDirectory(changedFile)) {
                                registerDirectory(changedFile.toAbsolutePath());
                            }

                            crawlAndStore();
                        } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                            System.out.println("File deleted: " + fullPath);

                            // Ensure we're deleting the correct file
                            transactionTemplate.execute(status -> {
                                Optional<TextFile> fileToDelete = repository.findByFilePath(fullPath);
                                if (fileToDelete.isPresent()) {
                                    repository.delete(fileToDelete.get());
                                    System.out.println("✅ Deleted from DB: " + fullPath);
                                } else {
                                    System.out.println("❌ File not found in DB (Check Path Format!): " + fullPath);
                                }
                                return null;
                            });
                        } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                            System.out.println("File modified: " + fullPath);
                            crawlAndStore();
                        }
                    }

                    // Reset the key to allow for further monitoring
                    boolean valid = key.reset();
                    if (!valid) {
                        watchKeys.remove(key);
                        if (watchKeys.isEmpty()) {
                            break;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    private void generateReport() {
        // Create a report at the end of the indexing process
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
}