package com.jesusluna.duplicateremover.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DuplicateGroup
 */
public class DuplicateGroupTest {

    @Test
    public void testDuplicateGroupCreation() {
        String hash = "abc123";
        DuplicateGroup group = new DuplicateGroup(hash);
        
        assertEquals(hash, group.getHash());
        assertEquals(0, group.getFileCount());
        assertFalse(group.isDuplicate());
    }

    @Test
    public void testAddFile(@TempDir Path tempDir) throws IOException {
        DuplicateGroup group = new DuplicateGroup("test-hash");
        
        File file1 = tempDir.resolve("file1.txt").toFile();
        try (FileWriter writer = new FileWriter(file1)) {
            writer.write("content");
        }
        
        group.addFile(file1);
        
        assertEquals(1, group.getFileCount());
        assertFalse(group.isDuplicate());
    }

    @Test
    public void testIsDuplicateWithMultipleFiles(@TempDir Path tempDir) throws IOException {
        DuplicateGroup group = new DuplicateGroup("test-hash");
        
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write("content");
            writer2.write("content");
        }
        
        group.addFile(file1);
        group.addFile(file2);
        
        assertEquals(2, group.getFileCount());
        assertTrue(group.isDuplicate());
    }

    @Test
    public void testGetTotalSize(@TempDir Path tempDir) throws IOException {
        DuplicateGroup group = new DuplicateGroup("test-hash");
        
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write("12345"); // 5 bytes
            writer2.write("123");   // 3 bytes
        }
        
        group.addFile(file1);
        group.addFile(file2);
        
        assertEquals(8, group.getTotalSize());
    }

    @Test
    public void testGetFilesReturnsDefensiveCopy(@TempDir Path tempDir) throws IOException {
        DuplicateGroup group = new DuplicateGroup("test-hash");
        
        File file1 = tempDir.resolve("file1.txt").toFile();
        try (FileWriter writer = new FileWriter(file1)) {
            writer.write("content");
        }
        
        group.addFile(file1);
        
        // Get files and modify the returned list
        var files = group.getFiles();
        files.clear();
        
        // Original group should still have the file
        assertEquals(1, group.getFileCount());
    }

    @Test
    public void testGetOriginalFileEmpty() {
        DuplicateGroup group = new DuplicateGroup("test-hash");
        assertNull(group.getOriginalFile());
    }

    @Test
    public void testGetOriginalFileSingleFile(@TempDir Path tempDir) throws IOException {
        DuplicateGroup group = new DuplicateGroup("test-hash");
        
        File file1 = tempDir.resolve("file1.txt").toFile();
        try (FileWriter writer = new FileWriter(file1)) {
            writer.write("content");
        }
        
        group.addFile(file1);
        assertEquals(file1, group.getOriginalFile());
    }

    @Test
    public void testGetOriginalFileByModificationDate(@TempDir Path tempDir) throws IOException, InterruptedException {
        DuplicateGroup group = new DuplicateGroup("test-hash");
        
        // Create first file
        File file1 = tempDir.resolve("file1.txt").toFile();
        try (FileWriter writer = new FileWriter(file1)) {
            writer.write("content");
        }
        
        // Wait to ensure different modification time
        Thread.sleep(100);
        
        // Create second file
        File file2 = tempDir.resolve("file2.txt").toFile();
        try (FileWriter writer = new FileWriter(file2)) {
            writer.write("content");
        }
        
        group.addFile(file2);
        group.addFile(file1);
        
        // file1 should be original as it's older
        assertEquals(file1, group.getOriginalFile());
    }

    @Test
    public void testGetOriginalFileByPathWhenSameModificationDate(@TempDir Path tempDir) throws IOException {
        DuplicateGroup group = new DuplicateGroup("test-hash");
        
        File fileB = tempDir.resolve("b_file.txt").toFile();
        File fileA = tempDir.resolve("a_file.txt").toFile();
        
        try (FileWriter writer1 = new FileWriter(fileB);
             FileWriter writer2 = new FileWriter(fileA)) {
            writer1.write("content");
            writer2.write("content");
        }
        
        // Set same modification time
        long time = System.currentTimeMillis();
        fileB.setLastModified(time);
        fileA.setLastModified(time);
        
        group.addFile(fileB);
        group.addFile(fileA);
        
        // fileA should be original as it's lexicographically first
        assertEquals(fileA, group.getOriginalFile());
    }
}
