package com.jesusluna.duplicateremover.model;

import java.io.File;
import java.util.ArrayList;
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
}
