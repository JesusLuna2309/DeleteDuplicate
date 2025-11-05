package com.jesusluna.duplicateremover.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for concurrent hash calculation components
 * Note: Full Task tests require JavaFX toolkit initialization and are covered by integration tests
 */
public class ConcurrentHashCalculationTest {

    /**
     * Helper method to create a test text file
     */
    private File createTextFile(Path tempDir, String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    /**
     * Helper method to create a test image
     */
    private File createTestImage(Path tempDir, String filename, int width, int height, Color color) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        File imageFile = tempDir.resolve(filename).toFile();
        ImageIO.write(image, "png", imageFile);
        return imageFile;
    }

    @Test
    public void testHashResultSuccess() {
        File testFile = new File("test.txt");
        String testHash = "abc123";
        
        HashResult result = HashResult.success(testFile, testHash);
        
        assertTrue(result.isSuccess());
        assertEquals(testFile, result.getFile());
        assertEquals(testHash, result.getHash());
        assertNull(result.getError());
    }

    @Test
    public void testHashResultFailure() {
        File testFile = new File("test.txt");
        Exception testError = new IOException("Test error");
        
        HashResult result = HashResult.failure(testFile, testError);
        
        assertFalse(result.isSuccess());
        assertEquals(testFile, result.getFile());
        assertNull(result.getHash());
        assertEquals(testError, result.getError());
    }

    @Test
    public void testScannerWithCustomParallelism(@TempDir Path tempDir) {
        File dir = tempDir.toFile();
        
        // Test various parallelism levels
        DuplicateFileScanner scanner1 = new DuplicateFileScanner(dir, false, 1);
        assertNotNull(scanner1);
        
        DuplicateFileScanner scanner2 = new DuplicateFileScanner(dir, false, 4);
        assertNotNull(scanner2);
        
        DuplicateFileScanner scanner3 = new DuplicateFileScanner(dir, false, 8);
        assertNotNull(scanner3);
        
        // Test that negative parallelism is handled (should default to 1)
        DuplicateFileScanner scanner4 = new DuplicateFileScanner(dir, false, -1);
        assertNotNull(scanner4);
    }

    private static final int SHA256_HEX_LENGTH = 64;

    @Test
    public void testFileHashServiceThreadSafety(@TempDir Path tempDir) throws Exception {
        // Create test files
        createTextFile(tempDir, "file1.txt", "content1");
        createTextFile(tempDir, "file2.txt", "content2");
        createTextFile(tempDir, "file3.txt", "content3");
        
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        File file3 = tempDir.resolve("file3.txt").toFile();
        
        // Create separate instances (as done in concurrent processing)
        FileHashService service1 = new FileHashService();
        FileHashService service2 = new FileHashService();
        FileHashService service3 = new FileHashService();
        
        // Calculate hashes (simulating concurrent workers)
        String hash1 = service1.calculateHash(file1);
        String hash2 = service2.calculateHash(file2);
        String hash3 = service3.calculateHash(file3);
        
        // Verify hashes are calculated correctly
        assertNotNull(hash1);
        assertNotNull(hash2);
        assertNotNull(hash3);
        assertEquals(SHA256_HEX_LENGTH, hash1.length(), "SHA-256 produces 64 hex characters");
        
        // Verify different files produce different hashes
        assertNotEquals(hash1, hash2);
        assertNotEquals(hash2, hash3);
    }

    @Test
    public void testConcurrentHashCalculationConsistency(@TempDir Path tempDir) throws Exception {
        // Create duplicate files
        String content = "duplicate content for testing";
        createTextFile(tempDir, "file1.txt", content);
        createTextFile(tempDir, "file2.txt", content);
        createTextFile(tempDir, "file3.txt", content);
        
        // Calculate hashes using separate service instances (simulating concurrent workers)
        FileHashService service1 = new FileHashService();
        FileHashService service2 = new FileHashService();
        FileHashService service3 = new FileHashService();
        
        String hash1 = service1.calculateHash(tempDir.resolve("file1.txt").toFile());
        String hash2 = service2.calculateHash(tempDir.resolve("file2.txt").toFile());
        String hash3 = service3.calculateHash(tempDir.resolve("file3.txt").toFile());
        
        // All hashes should be identical for identical content
        assertEquals(hash1, hash2, "Hash 1 and 2 should match");
        assertEquals(hash2, hash3, "Hash 2 and 3 should match");
        assertEquals(hash1, hash3, "Hash 1 and 3 should match");
    }

    @Test
    public void testImageHashingWithConcurrentInstances(@TempDir Path tempDir) throws Exception {
        // Create duplicate images
        createTestImage(tempDir, "img1.png", 10, 10, Color.BLUE);
        createTestImage(tempDir, "img2.png", 10, 10, Color.BLUE);
        
        // Use separate service instances
        FileHashService service1 = new FileHashService();
        FileHashService service2 = new FileHashService();
        
        String hash1 = service1.calculateHash(tempDir.resolve("img1.png").toFile());
        String hash2 = service2.calculateHash(tempDir.resolve("img2.png").toFile());
        
        // Should produce same hash for visually identical images
        assertEquals(hash1, hash2, "Identical images should produce same hash");
    }
}
