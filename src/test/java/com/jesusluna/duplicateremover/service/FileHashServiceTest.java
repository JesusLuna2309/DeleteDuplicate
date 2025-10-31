package com.jesusluna.duplicateremover.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for FileHashService
 */
public class FileHashServiceTest {

    private final FileHashService hashService = new FileHashService();

    @Test
    public void testCalculateHashForValidFile(@TempDir Path tempDir) throws IOException {
        // Create a test file
        File testFile = tempDir.resolve("test.txt").toFile();
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write("Hello, World!");
        }

        // Calculate hash
        String hash = hashService.calculateHash(testFile);

        // Verify hash is not null and has expected length (64 hex chars for SHA-256)
        assertNotNull(hash, "Hash should not be null");
        assertEquals(64, hash.length(), "SHA-256 hash should be 64 characters");
        
        // Verify it's all hex characters
        assertTrue(hash.matches("[0-9a-f]{64}"), "Hash should contain only hex characters");
    }

    @Test
    public void testCalculateHashForSameContentProducesSameHash(@TempDir Path tempDir) throws IOException {
        // Create two files with same content
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        String content = "This is identical content";
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write(content);
            writer2.write(content);
        }

        // Calculate hashes
        String hash1 = hashService.calculateHash(file1);
        String hash2 = hashService.calculateHash(file2);

        // Verify hashes are identical
        assertEquals(hash1, hash2, "Files with same content should have same hash");
    }

    @Test
    public void testCalculateHashForDifferentContentProducesDifferentHash(@TempDir Path tempDir) throws IOException {
        // Create two files with different content
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write("Content A");
            writer2.write("Content B");
        }

        // Calculate hashes
        String hash1 = hashService.calculateHash(file1);
        String hash2 = hashService.calculateHash(file2);

        // Verify hashes are different
        assertNotEquals(hash1, hash2, "Files with different content should have different hashes");
    }

    @Test
    public void testCalculateHashThrowsExceptionForNullFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            hashService.calculateHash(null);
        }, "Should throw exception for null file");
    }

    @Test
    public void testCalculateHashThrowsExceptionForNonExistentFile() {
        File nonExistent = new File("/tmp/this-file-does-not-exist-12345.txt");
        
        assertThrows(IllegalArgumentException.class, () -> {
            hashService.calculateHash(nonExistent);
        }, "Should throw exception for non-existent file");
    }

    @Test
    public void testAreFilesIdenticalForSameContent(@TempDir Path tempDir) throws IOException {
        // Create two files with same content
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        String content = "Identical content";
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write(content);
            writer2.write(content);
        }

        // Check if files are identical
        assertTrue(hashService.areFilesIdentical(file1, file2), 
                   "Files with same content should be identical");
    }

    @Test
    public void testAreFilesIdenticalForDifferentSizes(@TempDir Path tempDir) throws IOException {
        // Create two files with different sizes
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write("Short");
            writer2.write("Much longer content");
        }

        // Check if files are identical (should be false without calculating hash)
        assertFalse(hashService.areFilesIdentical(file1, file2), 
                    "Files with different sizes should not be identical");
    }

    @Test
    public void testAreFilesIdenticalForDifferentContent(@TempDir Path tempDir) throws IOException {
        // Create two files with same size but different content
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write("AAAAA");
            writer2.write("BBBBB");
        }

        // Check if files are identical
        assertFalse(hashService.areFilesIdentical(file1, file2), 
                    "Files with different content should not be identical");
    }
}
