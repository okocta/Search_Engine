package com.example.demo;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Column;
import java.time.ZonedDateTime;
@Entity
public class TextFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String filePath;
    private String filename;
    private String extension;
    private String firstThreeLines;
    private ZonedDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String content;

    public TextFile(String filePath, String filename, String extension, String firstThreeLines, ZonedDateTime timestamp, String content) {
        this.filePath = filePath;
        this.filename = filename;
        this.extension = extension;
        this.firstThreeLines = firstThreeLines;
        this.timestamp = timestamp;
        this.content = content;
    }

    public TextFile() {}
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public void setTimestamp(ZonedDateTime timestamp) { this.timestamp = timestamp; }
    public void setFirstThreeLines(String firstThreeLines) { this.firstThreeLines = firstThreeLines; }

}

