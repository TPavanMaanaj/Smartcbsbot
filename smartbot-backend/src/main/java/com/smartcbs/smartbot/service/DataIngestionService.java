package com.smartcbs.smartbot.service;

import com.smartcbs.smartbot.entity.EmbeddingStatus;
import com.smartcbs.smartbot.entity.KnowledgeBaseFile;
import com.smartcbs.smartbot.entity.TrainingJobStatus;
import com.smartcbs.smartbot.entity.TrainingStatus;
import com.smartcbs.smartbot.repository.KnowledgeBaseFileRepository;
import com.smartcbs.smartbot.repository.TrainingStatusRepository;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.tika.Tika;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DataIngestionService {

    private final VectorStoreService vectorStoreService;
    private final KnowledgeBaseFileRepository fileRepository;
    private final TrainingStatusRepository trainingStatusRepository;
    private final Set<String> visitedUrls = ConcurrentHashMap.newKeySet();
    private final Tika tika = new Tika();

    @Value("${ingestion.smartcbs.urls}")
    private String defaultUrls;

    @Value("${ingestion.max-pages}")
    private int maxPages;

    @Value("${storage.uploads.path}")
    private String uploadsPath;


    @Autowired
    public DataIngestionService(VectorStoreService vectorStoreService,
                               KnowledgeBaseFileRepository fileRepository,
                               TrainingStatusRepository trainingStatusRepository) {
        this.vectorStoreService = vectorStoreService;
        this.fileRepository = fileRepository;
        this.trainingStatusRepository = trainingStatusRepository;
    }

    public Map<String, Object> ingestUrl(String url, String urlTitle) {
        long startTime = System.currentTimeMillis();
        
        log.info("Starting data ingestion from URL: {}", url);

        List<Document> documents = new ArrayList<>();
        
        try {
            List<Document> crawledDocs = crawlWebsite(url);
            documents.addAll(crawledDocs);
            
            // Calculate hash from URL for uniqueness
            String urlHash = calculateFileHash(url.getBytes());
            
            // Check if URL already exists
            Optional<KnowledgeBaseFile> existingFileOpt = fileRepository.findByUrl(url);
            if (existingFileOpt.isPresent()) {
                log.info("URL {} already exists, skipping", url);
                return Map.of(
                    "documentsProcessed", 0,
                    "chunksEmbedded", 0,
                    "skippedDuplicates", 1,
                    "durationMs", System.currentTimeMillis() - startTime
                );
            }
            
            // Generate unique file ID for URL
            String fileId = UUID.randomUUID().toString();
            
            // Create database record for URL
            KnowledgeBaseFile kbFile = KnowledgeBaseFile.builder()
                .id(fileId)
                .filename(urlTitle != null ? urlTitle : url)
                .url(url)
                .fileHash(urlHash)
                .sourceType("url")
                .uploadTimestamp(LocalDateTime.now()) // Explicitly set upload timestamp
                .embeddingStatus(EmbeddingStatus.PENDING)
                .chunkCount(0)
                .build();
            
            fileRepository.save(kbFile);
            
            // Embed the documents
            int chunksEmbedded = vectorStoreService.embedDocuments(documents);
            
            // Update database record
            kbFile.setChunkCount(chunksEmbedded);
            kbFile.setEmbeddingStatus(EmbeddingStatus.EMBEDDED);
            fileRepository.save(kbFile);
            
        } catch (Exception e) {
            log.error("Error ingesting URL {}: {}", url, e.getMessage());
            return Map.of(
                "documentsProcessed", 0,
                "chunksEmbedded", 0,
                "skippedDuplicates", 0,
                "durationMs", System.currentTimeMillis() - startTime,
                "error", e.getMessage()
            );
        }

        long duration = System.currentTimeMillis() - startTime;

        // Calculate actual chunks embedded by checking the KB file
        Optional<KnowledgeBaseFile> kbFileOpt = fileRepository.findByUrl(url);
        int chunksEmbedded = kbFileOpt.map(KnowledgeBaseFile::getChunkCount).orElse(0);

        Map<String, Object> result = new HashMap<>();
        result.put("documentsProcessed", documents.size());
        result.put("chunksEmbedded", chunksEmbedded);
        result.put("durationMs", duration);

        log.info("URL ingestion complete: {} documents, {} chunks in {}ms", documents.size(), chunksEmbedded, duration);

        return result;
    }
    
    public Map<String, Object> ingestData(List<String> urls, boolean includeDefaults) {
        long startTime = System.currentTimeMillis();
        visitedUrls.clear();

        List<String> allUrls = new ArrayList<>();
        if (includeDefaults) {
            allUrls.addAll(Arrays.asList(defaultUrls.split(",")));
        }
        if (urls != null && !urls.isEmpty()) {
            allUrls.addAll(urls);
        }

        log.info("Starting data ingestion from {} URLs", allUrls.size());

        List<Document> documents = new ArrayList<>();

        for (String url : allUrls) {
            if (visitedUrls.size() >= maxPages) {
                log.warn("Reached maximum page limit of {}", maxPages);
                break;
            }

            try {
                List<Document> crawledDocs = crawlWebsite(url.trim());
                documents.addAll(crawledDocs);
            } catch (Exception e) {
                log.error("Error crawling URL {}: {}", url, e.getMessage());
            }
        }

        int chunksEmbedded = 0;
        if (!documents.isEmpty()) {
            chunksEmbedded = vectorStoreService.embedDocuments(documents);
        }

        long duration = System.currentTimeMillis() - startTime;

        Map<String, Object> result = new HashMap<>();
        result.put("documentsProcessed", documents.size());
        result.put("chunksEmbedded", chunksEmbedded);
        result.put("durationMs", duration);

        log.info("Ingestion complete: {} documents, {} chunks in {}ms",
                documents.size(), chunksEmbedded, duration);

        return result;
    }

    @Async
    public void ingestDataAsync(String jobId, List<String> urls, boolean includeDefaults) {
        // Create initial training status
        TrainingStatus trainingStatus = TrainingStatus.builder()
                .jobId(jobId)
                .status(TrainingJobStatus.IN_PROGRESS)
                .description("Starting data ingestion")
                .documentsProcessed(0)
                .chunksEmbedded(0)
                .durationMs(0)
                .build();
        
        trainingStatusRepository.save(trainingStatus);
        
        try {
            long startTime = System.currentTimeMillis();
            visitedUrls.clear();

            List<String> allUrls = new ArrayList<>();
            if (includeDefaults) {
                allUrls.addAll(Arrays.asList(defaultUrls.split(",")));
            }
            if (urls != null && !urls.isEmpty()) {
                allUrls.addAll(urls);
            }

            log.info("Starting data ingestion from {} URLs", allUrls.size());

            List<Document> documents = new ArrayList<>();

            for (String url : allUrls) {
                if (visitedUrls.size() >= maxPages) {
                    log.warn("Reached maximum page limit of {}", maxPages);
                    break;
                }

                try {
                    List<Document> crawledDocs = crawlWebsite(url.trim());
                    documents.addAll(crawledDocs);
                } catch (Exception e) {
                    log.error("Error crawling URL {}: {}", url, e.getMessage());
                }
            }

            int chunksEmbedded = 0;
            if (!documents.isEmpty()) {
                chunksEmbedded = vectorStoreService.embedDocuments(documents);
            }

            long duration = System.currentTimeMillis() - startTime;

            // Update training status
            trainingStatus.setStatus(TrainingJobStatus.COMPLETED);
            trainingStatus.setDescription("Data ingestion completed successfully");
            trainingStatus.setDocumentsProcessed(documents.size());
            trainingStatus.setChunksEmbedded(chunksEmbedded);
            trainingStatus.setDurationMs(duration);
            trainingStatusRepository.save(trainingStatus);

            log.info("Ingestion complete: {} documents, {} chunks in {}ms",
                    documents.size(), chunksEmbedded, duration);

        } catch (Exception e) {
            log.error("Async training failed: {}", e.getMessage(), e);
            
            // Update training status
            trainingStatus.setStatus(TrainingJobStatus.FAILED);
            trainingStatus.setDescription("Training failed: " + e.getMessage());
            trainingStatusRepository.save(trainingStatus);
        }
    }


    private List<Document> crawlWebsite(String startUrl) {
        List<Document> documents = new ArrayList<>();
        Queue<String> urlQueue = new LinkedList<>();
        urlQueue.add(startUrl);

        while (!urlQueue.isEmpty() && visitedUrls.size() < maxPages) {
            String currentUrl = urlQueue.poll();

            if (visitedUrls.contains(currentUrl)) {
                continue;
            }

            try {
                visitedUrls.add(currentUrl);
                log.info("Crawling: {}", currentUrl);

                if (currentUrl.toLowerCase().endsWith(".pdf")) {
                    Document pdfDoc = extractPdfContent(currentUrl);
                    if (pdfDoc != null) {
                        documents.add(pdfDoc);
                    }
                } else {
                    org.jsoup.nodes.Document webPage = Jsoup.connect(currentUrl)
                            .userAgent("Mozilla/5.0 SmartBot/1.0")
                            .timeout(10000)
                            .get();

                    String cleanText = extractCleanText(webPage);

                    if (!cleanText.isEmpty()) {
                        Metadata metadata = Metadata.from(Map.of(
                                "source", currentUrl,
                                "title", webPage.title()
                        ));
                        Document doc = Document.from(cleanText, metadata);
                        documents.add(doc);
                    }

                    String baseDomain = extractBaseDomain(startUrl);
                    Elements links = webPage.select("a[href]");
                    for (Element link : links) {
                        String absUrl = link.absUrl("href");
                        if (isValidUrl(absUrl, baseDomain) && !visitedUrls.contains(absUrl)) {
                            urlQueue.add(absUrl);
                        }
                    }
                }

            } catch (Exception e) {
                log.warn("Error processing URL {}: {}", currentUrl, e.getMessage());
            }
        }

        return documents;
    }

    private String extractCleanText(org.jsoup.nodes.Document webPage) {
        webPage.select("script, style, nav, footer, header, aside").remove();

        Elements mainContent = webPage.select("main, article, .content, #content, .main");
        String text;
        if (!mainContent.isEmpty()) {
            text = mainContent.text();
        } else {
            text = webPage.body().text();
        }

        text = text.replaceAll("\\s+", " ").trim();

        return text;
    }

    private Document extractPdfContent(String pdfUrl) {
        try {
            URL url = new URL(pdfUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 SmartBot/1.0");

            try (InputStream inputStream = connection.getInputStream()) {
                String content = tika.parseToString(inputStream);

                Metadata metadata = Metadata.from(Map.of(
                        "source", pdfUrl,
                        "type", "pdf"
                ));
                return Document.from(content, metadata);
            }
        } catch (Exception e) {
            log.error("Error extracting PDF content from {}: {}", pdfUrl, e.getMessage());
            return null;
        }
    }

    private String extractBaseDomain(String url) {
        try {
            URL urlObj = new URL(url);
            return urlObj.getProtocol() + "://" + urlObj.getHost();
        } catch (Exception e) {
            return url;
        }
    }

    private boolean isValidUrl(String url, String baseDomain) {
        if (url == null || url.isEmpty()) {
            return false;
        }

        if (!url.startsWith("http")) {
            return false;
        }

        if (url.contains("#") || url.contains("javascript:")) {
            return false;
        }

        if (url.matches(".*\\.(jpg|jpeg|png|gif|css|js|ico|svg|woff|woff2|ttf)$")) {
            return false;
        }

        return url.startsWith(baseDomain) || url.endsWith(".pdf");
    }

    private String calculateFileHash(byte[] content) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(content);
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            log.error("Error calculating file hash: {}", e.getMessage());
            return UUID.randomUUID().toString();
        }
    }

    public Map<String, Object> ingestUploadedFiles(List<MultipartFile> files, boolean saveToStorage) {
        long startTime = System.currentTimeMillis();
        int documentsProcessed = 0;
        int chunksEmbedded = 0;
        int skippedDuplicates = 0;

        // Ensure uploads directory
        try {
            Path dir = Paths.get(uploadsPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }
        } catch (IOException e) {
            log.warn("Unable to create uploads directory: {}", e.getMessage());
        }

        for (MultipartFile file : files) {
            try {
                String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "uploaded-file");
                byte[] contentBytes = file.getBytes();
                
                // Calculate file hash
                String fileHash = calculateFileHash(contentBytes);
                
                // Check if file already exists
                if (fileRepository.existsByFileHash(fileHash)) {
                    log.info("File {} already exists (duplicate hash), skipping", originalFilename);
                    skippedDuplicates++;
                    continue;
                }
                
                // Generate unique file ID and storage path
                String fileId = UUID.randomUUID().toString();
                String uniqueFilename = fileId + "_" + originalFilename;
                Path storagePath = Paths.get(uploadsPath, uniqueFilename);
                
                // Save file to storage
                Files.write(storagePath, contentBytes);
                
                // Create database record
                KnowledgeBaseFile kbFile = KnowledgeBaseFile.builder()
                        .id(fileId)
                        .filename(originalFilename)
                        .storagePath(storagePath.toString())
                        .fileHash(fileHash)
                        .uploadTimestamp(LocalDateTime.now()) // Explicitly set upload timestamp
                        .embeddingStatus(EmbeddingStatus.PENDING)
                        .chunkCount(0)
                        .build();
                
                fileRepository.save(kbFile);
                
                // Parse and embed
                try (InputStream is = new ByteArrayInputStream(contentBytes)) {
                    String content = tika.parseToString(is);
                    
                    // Create document with file ID in metadata
                    Metadata metadata = Metadata.from(Map.of(
                            "source", "upload:" + originalFilename,
                            "filename", originalFilename,
                            "fileId", fileId
                    ));
                    Document doc = Document.from(content, metadata);
                    
                    // Embed document
                    int chunks = vectorStoreService.embedDocuments(List.of(doc));
                    chunksEmbedded += chunks;
                    
                    // Update database record
                    kbFile.setChunkCount(chunks);
                    kbFile.setEmbeddingStatus(EmbeddingStatus.EMBEDDED);
                    fileRepository.save(kbFile);
                    
                    documentsProcessed++;
                }
            } catch (Exception e) {
                log.error("Error processing uploaded file {}: {}", file.getOriginalFilename(), e.getMessage());
            }
        }

        long duration = System.currentTimeMillis() - startTime;
        Map<String, Object> result = new HashMap<>();
        result.put("documentsProcessed", documentsProcessed);
        result.put("chunksEmbedded", chunksEmbedded);
        result.put("skippedDuplicates", skippedDuplicates);
        result.put("durationMs", duration);
        log.info("Upload ingestion complete: {} documents, {} chunks, {} duplicates skipped in {}ms", 
                documentsProcessed, chunksEmbedded, skippedDuplicates, duration);
        return result;
    }

    @Async
    public void ingestUploadedFilesAsync(String jobId, List<MultipartFile> files, boolean saveToStorage) {
        // Create initial training status
        TrainingStatus trainingStatus = TrainingStatus.builder()
                .jobId(jobId)
                .status(TrainingJobStatus.IN_PROGRESS)
                .description("Starting file upload ingestion")
                .documentsProcessed(0)
                .chunksEmbedded(0)
                .durationMs(0)
                .build();
        
        trainingStatusRepository.save(trainingStatus);
        
        try {
            long startTime = System.currentTimeMillis();
            int documentsProcessed = 0;
            int chunksEmbedded = 0;
            int skippedDuplicates = 0;

            // Ensure uploads directory
            try {
                Path dir = Paths.get(uploadsPath);
                if (!Files.exists(dir)) {
                    Files.createDirectories(dir);
                }
            } catch (IOException e) {
                log.warn("Unable to create uploads directory: {}", e.getMessage());
            }

            for (MultipartFile file : files) {
                try {
                    String originalFilename = Objects.requireNonNullElse(file.getOriginalFilename(), "uploaded-file");
                    byte[] contentBytes = file.getBytes();
                    
                    // Calculate file hash
                    String fileHash = calculateFileHash(contentBytes);
                    
                    // Check if file already exists
                    if (fileRepository.existsByFileHash(fileHash)) {
                        log.info("File {} already exists (duplicate hash), skipping", originalFilename);
                        skippedDuplicates++;
                        continue;
                    }
                    
                    // Generate unique file ID and storage path
                    String fileId = UUID.randomUUID().toString();
                    String uniqueFilename = fileId + "_" + originalFilename;
                    Path storagePath = Paths.get(uploadsPath, uniqueFilename);
                    
                    // Save file to storage
                    Files.write(storagePath, contentBytes);
                    
                    // Create database record
                    KnowledgeBaseFile kbFile = KnowledgeBaseFile.builder()
                            .id(fileId)
                            .filename(originalFilename)
                            .storagePath(storagePath.toString())
                            .fileHash(fileHash)
                            .uploadTimestamp(LocalDateTime.now()) // Explicitly set upload timestamp
                            .embeddingStatus(EmbeddingStatus.PENDING)
                            .chunkCount(0)
                            .build();
                    
                    fileRepository.save(kbFile);
                    
                    // Parse and embed
                    try (InputStream is = new ByteArrayInputStream(contentBytes)) {
                        String content = tika.parseToString(is);
                        
                        // Create document with file ID in metadata
                        Metadata metadata = Metadata.from(Map.of(
                                "source", "upload:" + originalFilename,
                                "filename", originalFilename,
                                "fileId", fileId
                        ));
                        Document doc = Document.from(content, metadata);
                        
                        // Embed document
                        int chunks = vectorStoreService.embedDocuments(List.of(doc));
                        chunksEmbedded += chunks;
                        
                        // Update database record
                        kbFile.setChunkCount(chunks);
                        kbFile.setEmbeddingStatus(EmbeddingStatus.EMBEDDED);
                        fileRepository.save(kbFile);
                        
                        documentsProcessed++;
                    }
                } catch (Exception e) {
                    log.error("Error processing uploaded file {}: {}", file.getOriginalFilename(), e.getMessage());
                }
            }

            long duration = System.currentTimeMillis() - startTime;
            
            // Update training status
            trainingStatus.setStatus(TrainingJobStatus.COMPLETED);
            trainingStatus.setDescription("File upload ingestion completed successfully");
            trainingStatus.setDocumentsProcessed(documentsProcessed);
            trainingStatus.setChunksEmbedded(chunksEmbedded);
            trainingStatus.setDurationMs(duration);
            trainingStatusRepository.save(trainingStatus);

            log.info("Upload ingestion complete: {} documents, {} chunks, {} duplicates skipped in {}ms", 
                    documentsProcessed, chunksEmbedded, skippedDuplicates, duration);
                    
        } catch (Exception e) {
            log.error("Async upload training failed: {}", e.getMessage(), e);
            
            // Update training status
            trainingStatus.setStatus(TrainingJobStatus.FAILED);
            trainingStatus.setDescription("Upload training failed: " + e.getMessage());
            trainingStatusRepository.save(trainingStatus);
        }
    }

    public boolean deleteUrl(String urlId) {
        try {
            Optional<KnowledgeBaseFile> fileOpt = fileRepository.findById(urlId);
            if (fileOpt.isEmpty()) {
                log.warn("URL with ID {} not found in database", urlId);
                return false;
            }
            
            KnowledgeBaseFile kbFile = fileOpt.get();
            
            // Remove embeddings first
            vectorStoreService.removeEmbeddingsByFileId(urlId);
            
            // Permanently delete database record
            fileRepository.delete(kbFile);
            log.info("Permanently deleted URL record from database: {}", kbFile.getUrl());
            
            return true;
        } catch (Exception e) {
            log.error("Error deleting URL {}: {}", urlId, e.getMessage());
            return false;
        }
    }
    
    public List<Map<String, Object>> listUploadedFiles() {
        try {
            List<KnowledgeBaseFile> files = fileRepository.findAll();
            return files.stream()
                    .map(f -> {
                        Map<String, Object> info = new HashMap<>();
                        info.put("id", f.getId());
                        info.put("filename", f.getFilename());
                        info.put("uploadTimestamp", f.getUploadTimestamp() != null ? f.getUploadTimestamp().toString() : "");
                        info.put("embeddingStatus", f.getEmbeddingStatus().toString());
                        info.put("chunkCount", f.getChunkCount());
                        info.put("sourceType", f.getSourceType()); // 'file' or 'url'
                        
                        // Add file size if file exists and it's a file (not a URL)
                        if (f.getSourceType() != null && f.getSourceType().equals("file") && f.getStoragePath() != null) {
                            try {
                                Path path = Paths.get(f.getStoragePath());
                                if (Files.exists(path)) {
                                    info.put("size", Files.size(path));
                                }
                            } catch (IOException e) {
                                log.warn("Unable to get size for file {}: {}", f.getFilename(), e.getMessage());
                            }
                        }
                        
                        // Add URL if it's a URL type
                        if (f.getSourceType() != null && f.getSourceType().equals("url") && f.getUrl() != null) {
                            info.put("url", f.getUrl());
                        }
                        
                        return info;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error listing files: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public boolean deleteUploadedFile(String fileId) {
        KnowledgeBaseFile kbFile = null;

        try {
            Optional<KnowledgeBaseFile> fileOpt = fileRepository.findById(fileId);
            if (fileOpt.isEmpty()) {
                log.warn("File with ID {} not found in database", fileId);
                return false;
            }

            kbFile = fileOpt.get();

            // Remove embeddings first
            vectorStoreService.removeEmbeddingsByFileId(fileId);

            if ("url".equals(kbFile.getSourceType())) {
                log.info("Removed URL entry from database: {}", kbFile.getUrl());
            } else {
                if (kbFile.getStoragePath() != null) {
                    Path filePath = Paths.get(kbFile.getStoragePath());
                    if (Files.exists(filePath)) {
                        Files.delete(filePath);
                        log.info("Deleted physical file: {}", filePath);
                    }
                }
            }

            fileRepository.delete(kbFile);
            return true;

        } catch (Exception e) {
            log.error(
                "Error deleting fileId={} sourceType={} : {}",
                fileId,
                kbFile != null ? kbFile.getSourceType() : "unknown",
                e.getMessage(),
                e
            );
            return false;
        }
    }

    
    @Transactional
    public boolean permanentlyDeleteFile(String fileId) {
        KnowledgeBaseFile kbFile = null;

        try {
            Optional<KnowledgeBaseFile> fileOpt = fileRepository.findById(fileId);
            if (fileOpt.isEmpty()) {
                log.warn("File with ID {} not found in database", fileId);
                return false;
            }

            kbFile = fileOpt.get();

            vectorStoreService.removeEmbeddingsByFileId(fileId);

            if (!"url".equals(kbFile.getSourceType()) && kbFile.getStoragePath() != null) {
                Path filePath = Paths.get(kbFile.getStoragePath());
                if (Files.exists(filePath)) {
                    Files.delete(filePath);
                }
            }

            fileRepository.delete(kbFile);
            return true;

        } catch (Exception e) {
            log.error(
                "Error permanently deleting fileId={} sourceType={} : {}",
                fileId,
                kbFile != null ? kbFile.getSourceType() : "unknown",
                e.getMessage(),
                e
            );
            return false;
        }
    }

    public Map<String, Object> ingestStoredFiles(boolean clearExistingEmbeddings) {
        long startTime = System.currentTimeMillis();
        int documentsProcessed = 0;
        int chunksEmbedded = 0;
        int skippedDuplicates = 0;

        try {
            Path dir = Paths.get(uploadsPath);
            if (!Files.exists(dir)) {
                Files.createDirectories(dir);
            }

            List<Path> files = Files.list(dir)
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());

            for (Path p : files) {
                try {
                    // Extract original filename from the UUID prefixed filename
                    String fileName = p.getFileName().toString();
                    String originalFilename = fileName;
                    if (fileName.contains("_")) {
                        originalFilename = fileName.substring(fileName.indexOf("_") + 1);
                    }
                    
                    byte[] contentBytes = Files.readAllBytes(p);
                    
                    // Calculate file hash
                    String fileHash = calculateFileHash(contentBytes);
                    
                    // Check if file already exists
                    if (fileRepository.existsByFileHash(fileHash)) {
                        log.info("File {} already exists (duplicate hash), skipping", originalFilename);
                        skippedDuplicates++;
                        continue;
                    }
                    
                    // Extract file ID from filename (everything before the first underscore)
                    String fileId = fileName.contains("_") ? fileName.substring(0, fileName.indexOf("_")) : UUID.randomUUID().toString();
                    
                    try (InputStream is = new ByteArrayInputStream(contentBytes)) {
                        String content = tika.parseToString(is);
                        Metadata metadata = Metadata.from(Map.of(
                                "source", "upload:" + originalFilename,
                                "filename", originalFilename,
                                "fileId", fileId
                        ));
                        Document doc = Document.from(content, metadata);
                        
                        // Embed document
                        int chunks = vectorStoreService.embedDocuments(List.of(doc));
                        chunksEmbedded += chunks;
                        
                        // Create/update database record
                        KnowledgeBaseFile kbFile = KnowledgeBaseFile.builder()
                                .id(fileId)
                                .filename(originalFilename)
                                .storagePath(p.toString())
                                .fileHash(fileHash)
                                .uploadTimestamp(LocalDateTime.now()) // Explicitly set upload timestamp
                                .embeddingStatus(EmbeddingStatus.EMBEDDED)
                                .chunkCount(chunks)
                                .build();
                        
                        fileRepository.save(kbFile);
                        
                        documentsProcessed++;
                    }
                } catch (Exception e) {
                    log.error("Error processing stored file {}: {}", p.getFileName(), e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error reading uploads directory: {}", e.getMessage());
        }

        if (clearExistingEmbeddings) {
            vectorStoreService.clearStore();
        }

        long duration = System.currentTimeMillis() - startTime;
        Map<String, Object> result = new HashMap<>();
        result.put("documentsProcessed", documentsProcessed);
        result.put("chunksEmbedded", chunksEmbedded);
        result.put("skippedDuplicates", skippedDuplicates);
        result.put("durationMs", duration);
        log.info("Stored-file ingestion complete: {} documents, {} chunks, {} duplicates skipped in {}ms", documentsProcessed, chunksEmbedded, skippedDuplicates, duration);
        return result;
    }
}