# Search Engine Architecture

## Documentation
This document describes the architecture of the search engine, a web app designed to crawl local content, filter unwanted data, store it in a PostgreSQL database, and process search queries.

## C4 Model Overview
The architecture follows the C4 Model, which consists of various levels of abstraction:

1. **System Context** - Describes how users interact with the search engine.
2. **Container Diagram** - Breaks the system down into main components.
3. **Component Diagram** - Details the internal structure of the search engine and its components.

---

## 1. System Context Diagram

![System Context](Diagrams/level1.drawio.png)

- **User** - The user interacts with the search engine via a web interface.
- **Search Engine**:
    - Handles search queries submitted by users.
    - Reads and indexes content from the local file system.
    - Stores the indexed data in a PostgreSQL database for efficient search operations.

---

## 2. Container Diagram

![Container Diagram](Diagrams/level2.png)

- **User** – A person or system interacting with the search engine.
- **Query Handler** – Receives search queries, processes them, and returns the search results.
- **Indexing Module** – Crawls and processes files from the file system to prepare the data for indexing.
- **Crawl Module** – Scans the local file system and collects content for indexing.
- **PostgreSQL Database** – Stores the indexed data, including metadata (file names, paths, content snippets) and full-text indexed data.
- **File System** – The raw storage system where files are located before being indexed.

---

## 3. Component Diagram

![Component Diagram](Diagrams/level3.png)

- **User** - Initiates search requests through a web interface or API.
- **Search API** - Exposes RESTful endpoints (using Spring Boot) to handle search queries.
- **Query Handler** - Responsible for handling search logic, including multi-word searches using PostgreSQL full-text search.
- **Index Manager** - Manages and retrieves indexed content from the PostgreSQL database.
- **Text Extractor** - Extracts meaningful text (e.g., the first few lines) from `.txt` files.
- **File Crawler** - A Spring Boot service that reads the local file system and extracts metadata and content from text files.
- **PostgreSQL Database** - Stores extracted content, file metadata, and full-text indexed data.
- **File System** - The directory (`folder_test`) where raw text files are located.

---

## Key Components:

1. **Search API (Spring Boot REST Controller)**
- Exposes endpoints to interact with the search engine, such as querying files by name or content.
- Endpoints:
    - `/files/search`: For searching by filename.
    - `/files/search/content`: For searching within the content using PostgreSQL's full-text search.
    - `/files`: For listing all indexed files.

2. **Query Handler**
- Handles the logic of processing search queries and retrieving results from the database.
- Supports multi-word searches using the full-text search capabilities of PostgreSQL.

3. **Indexing Module**
- Responsible for crawling the local file system (`folder_test`), reading `.txt` files, extracting metadata (e.g., file names, first three lines, etc.), and storing the information in the PostgreSQL database.
- The **File Crawler** module scans the file system for new, modified, or deleted files and updates the database accordingly.

4. **Database (PostgreSQL)**
- Stores the metadata of files and their indexed content.
- Utilizes PostgreSQL's full-text search features for efficient search query execution.

5. **File System**
- The raw storage of files (`folder_test`), where text files are crawled and indexed. The root folder for crawling can be configured dynamically via `application.properties`.

---
