package com.jesusluna.duplicateremover.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Represents a group of duplicate files with the same hash
 */
public class DuplicateGroup {
    
    private final String hash;
    private final List<File> files;
    
    public DuplicateGroup(String hash) {
        this.hash = hash;
        this.files = new ArrayList<>();
    }
    
    public String getHash() {
        return hash;
    }
    
    public List<File> getFiles() {
        return new ArrayList<>(files);
    }
    
    public void addFile(File file) {
        files.add(file);
    }
    
    public int getFileCount() {
        return files.size();
    }
    
    public boolean isDuplicate() {
        return files.size() > 1;
    }
    
    public long getTotalSize() {
        return files.stream()
                .mapToLong(File::length)
                .sum();
    }
    
    /**
     * Gets the original file in this duplicate group.
     * The original is determined by:
     * 1. Oldest modification date (file that was created/modified first)
     * 2. If dates are equal, lexicographically smallest path
     * 
     * @return the file considered as the original, or null if no files in group
     */
    public File getOriginalFile() {
        if (files.isEmpty()) {
            return null;
        }
        
        return files.stream()
                .min(Comparator
                    .comparingLong(File::lastModified)
                    .thenComparing(File::getAbsolutePath))
                .orElse(null);
    }
}
