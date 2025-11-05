package com.jesusluna.duplicateremover.service;

import com.jesusluna.duplicateremover.model.DuplicateGroup;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Service for scanning directories and finding duplicate files
 * Uses JavaFX Task for background processing with progress updates
 * Implements concurrent hash calculation for improved performance
 */
public class DuplicateFileScanner extends Task<List<DuplicateGroup>> {
    
    private static final Logger logger = LoggerFactory.getLogger(DuplicateFileScanner.class);
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff"
    );
    
    private final File directory;
    private final boolean includeSubfolders;
    private final int parallelism;
    
    /**
     * Creates a scanner with default parallelism based on available processors
     */
    public DuplicateFileScanner(File directory, boolean includeSubfolders) {
        this(directory, includeSubfolders, calculateDefaultParallelism());
    }
    
    /**
     * Creates a scanner with custom parallelism level
     */
    public DuplicateFileScanner(File directory, boolean includeSubfolders, int parallelism) {
        this.directory = directory;
        this.includeSubfolders = includeSubfolders;
        this.parallelism = Math.max(1, parallelism);
        logger.info("Scanner initialized with parallelism level: {}", this.parallelism);
    }
    
    /**
     * Calculates optimal parallelism based on available CPU cores
     * Uses all cores for SSD-optimized scanning
     */
    private static int calculateDefaultParallelism() {
        int cores = Runtime.getRuntime().availableProcessors();
        // Use all cores for optimal performance on modern systems with SSDs
        return cores;
    }
    
    @Override
    protected List<DuplicateGroup> call() throws Exception {
        updateMessage("Scanning directory...");
        updateProgress(0, 1);
        
        // Collect all files
        List<File> files = new ArrayList<>();
        collectFiles(directory, files, includeSubfolders);
        
        int totalFiles = files.size();
        logger.info("Found {} files to analyze", totalFiles);
        
        if (totalFiles == 0) {
            updateMessage("No files found");
            return new ArrayList<>();
        }
        
        // Group files by hash using concurrent calculation
        Map<String, DuplicateGroup> hashGroups = new HashMap<>();
        
        if (parallelism == 1 || totalFiles < 10) {
            // Sequential processing for small file sets or single thread
            hashGroups = processSequentially(files, totalFiles);
        } else {
            // Concurrent processing for better performance
            hashGroups = processConcurrently(files, totalFiles);
        }
        
        if (isCancelled()) {
            updateMessage("Cancelled");
            return new ArrayList<>();
        }
        
        // Filter only groups with duplicates
        List<DuplicateGroup> duplicates = hashGroups.values().stream()
                .filter(DuplicateGroup::isDuplicate)
                .sorted((g1, g2) -> Integer.compare(g2.getFileCount(), g1.getFileCount()))
                .toList();
        
        updateMessage(String.format("Found %d duplicate groups", duplicates.size()));
        updateProgress(1.0, 1.0);
        
        logger.info("Scan complete: {} duplicate groups found", duplicates.size());
        return duplicates;
    }
    
    /**
     * Process files sequentially (fallback for small sets or single thread)
     */
    private Map<String, DuplicateGroup> processSequentially(List<File> files, int totalFiles) {
        Map<String, DuplicateGroup> hashGroups = new HashMap<>();
        FileHashService hashService = new FileHashService();
        int processedFiles = 0;
        
        for (File file : files) {
            if (isCancelled()) {
                break;
            }
            
            try {
                String hash = hashService.calculateHash(file);
                hashGroups.computeIfAbsent(hash, DuplicateGroup::new).addFile(file);
            } catch (Exception e) {
                logger.warn("Error calculating hash for file: {}", file.getAbsolutePath(), e);
            }
            
            processedFiles++;
            updateProgressInfo(processedFiles, totalFiles);
        }
        
        return hashGroups;
    }
    
    /**
     * Process files concurrently using ExecutorService
     */
    private Map<String, DuplicateGroup> processConcurrently(List<File> files, int totalFiles) {
        Map<String, DuplicateGroup> hashGroups = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        CompletionService<HashResult> completionService = new ExecutorCompletionService<>(executor);
        
        try {
            // Submit all hash calculation tasks
            int submitted = 0;
            for (File file : files) {
                if (isCancelled()) {
                    break;
                }
                
                completionService.submit(() -> {
                    FileHashService hashService = new FileHashService();
                    try {
                        String hash = hashService.calculateHash(file);
                        return HashResult.success(file, hash);
                    } catch (Exception e) {
                        return HashResult.failure(file, e);
                    }
                });
                submitted++;
            }
            
            // Collect results as they complete
            int processedFiles = 0;
            for (int i = 0; i < submitted; i++) {
                if (isCancelled()) {
                    break;
                }
                
                try {
                    Future<HashResult> future = completionService.poll(5, TimeUnit.SECONDS);
                    if (future != null) {
                        HashResult result = future.get();
                        
                        if (result.isSuccess()) {
                            hashGroups.computeIfAbsent(result.getHash(), DuplicateGroup::new)
                                     .addFile(result.getFile());
                        } else {
                            logger.warn("Error calculating hash for file: {}", 
                                       result.getFile().getAbsolutePath(), result.getError());
                        }
                        
                        processedFiles++;
                        updateProgressInfo(processedFiles, totalFiles);
                    }
                } catch (InterruptedException e) {
                    logger.warn("Hash calculation interrupted", e);
                    Thread.currentThread().interrupt();
                    break;
                } catch (ExecutionException e) {
                    logger.error("Error processing hash result", e);
                }
            }
            
        } finally {
            // Shutdown executor gracefully
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        return hashGroups;
    }
    
    /**
     * Update progress information on the JavaFX thread
     */
    private void updateProgressInfo(int processedFiles, int totalFiles) {
        double progress = (double) processedFiles / totalFiles;
        updateProgress(progress, 1.0);
        updateMessage(String.format("Processing: %d/%d files (%.0f%%)", 
            processedFiles, totalFiles, progress * 100));
    }
    
    private void collectFiles(File dir, List<File> files, boolean recursive) {
        File[] entries = dir.listFiles();
        if (entries == null) {
            return;
        }
        
        for (File entry : entries) {
            if (entry.isFile()) {
                files.add(entry);
            } else if (entry.isDirectory() && recursive) {
                collectFiles(entry, files, true);
            }
        }
    }
    
    public static boolean isImageFile(File file) {
        String name = file.getName().toLowerCase();
        return IMAGE_EXTENSIONS.stream().anyMatch(name::endsWith);
    }
}
