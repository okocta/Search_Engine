package com.example.demo;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface TextFileRepository extends JpaRepository<TextFile, Long> {
    Optional<TextFile> findByFilePath(String filePath);
}