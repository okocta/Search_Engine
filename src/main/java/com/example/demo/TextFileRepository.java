package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

interface TextFileRepository extends JpaRepository<TextFile, Long> {
    List<TextFile> findByFilenameContainingIgnoreCase(String filename);
    @Query(value = "SELECT * FROM text_file WHERE content_tsv @@ to_tsquery('english', :query)", nativeQuery = true)
    List<TextFile> searchByContent(@Param("query") String query);
    Optional<TextFile> findByFilePath(String filePath);
}