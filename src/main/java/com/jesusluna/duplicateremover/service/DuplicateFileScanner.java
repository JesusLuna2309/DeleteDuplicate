package com.jesusluna.duplicateremover.service;

import com.jesusluna.duplicateremover.model.DuplicateGroup;
import javafx.concurrent.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Service for scanning directories and finding duplicate files
 * Uses JavaFX Task for background processing with progress updates
 */
public class DuplicateFileScanner extends Task<List<DuplicateGroup>> {
    
    private static final Logger logger = LoggerFactory.getLogger(DuplicateFileScanner.class);
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
        ".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff"
    );
    
    private final File directory;
    private final boolean includeSubfolders;
    private final FileHashService hashService;
    
    public DuplicateFileScanner(File directory, boolean includeSubfolders) {
        this.directory = directory;
        this.includeSubfolders = includeSubfolders;
        this.hashService = new FileHashService();
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
        
        // Group files by hash
        Map<String, DuplicateGroup> hashGroups = new HashMap<>();
        int processedFiles = 0;
        
        for (File file : files) {
            if (isCancelled()) {
                updateMessage("Cancelled");
                break;
            }
            
            try {
                String hash = hashService.calculateHash(file);
                
                hashGroups.computeIfAbsent(hash, DuplicateGroup::new).addFile(file);
                
                processedFiles++;
                double progress = (double) processedFiles / totalFiles;
                updateProgress(progress, 1.0);
                updateMessage(String.format("Processing: %d/%d files (%.0f%%)", 
                    processedFiles, totalFiles, progress * 100));
                
            } catch (IOException e) {
                logger.warn("Error calculating hash for file: {}", file.getAbsolutePath(), e);
            }
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
