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
 * Test class for FileHashService
 */
public class FileHashServiceTest {

    private final FileHashService hashService = new FileHashService();

    /**
     * Helper method to create a simple test image
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
    public void testCalculateHashThrowsExceptionForNonExistentFile(@TempDir Path tempDir) {
        // Use platform-independent path that definitely doesn't exist
        File nonExistent = tempDir.resolve("this-file-does-not-exist-12345.txt").toFile();
        
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

    @Test
    public void testCalculateHashForImageUsesPixelHash(@TempDir Path tempDir) throws IOException {
        // Create two images with same visual content but as separate files
        File image1 = createTestImage(tempDir, "image1.png", 10, 10, Color.RED);
        File image2 = createTestImage(tempDir, "image2.png", 10, 10, Color.RED);

        String hash1 = hashService.calculateHash(image1);
        String hash2 = hashService.calculateHash(image2);

        // Both should produce the same hash since they have identical pixels
        assertEquals(hash1, hash2, "Visually identical images should have same hash");
    }

    @Test
    public void testCalculateHashForImageWithDifferentMetadata(@TempDir Path tempDir) throws IOException {
        // Create the same image content
        BufferedImage imageContent = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = imageContent.createGraphics();
        g2d.setColor(Color.BLUE);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();

        // Save with different filenames (simulating different metadata scenarios)
        File image1 = tempDir.resolve("training_12345.png").toFile();
        File image2 = tempDir.resolve("im67890.png").toFile();
        
        ImageIO.write(imageContent, "png", image1);
        ImageIO.write(imageContent, "png", image2);

        String hash1 = hashService.calculateHash(image1);
        String hash2 = hashService.calculateHash(image2);

        // Should have same hash regardless of filename
        assertEquals(hash1, hash2, "Images with same pixels but different names should have same hash");
    }

    @Test
    public void testCalculateHashForNonImageUsesFileHash(@TempDir Path tempDir) throws IOException {
        // Create two text files with same content
        File file1 = tempDir.resolve("file1.txt").toFile();
        File file2 = tempDir.resolve("file2.txt").toFile();
        
        String content = "Same content";
        try (FileWriter writer1 = new FileWriter(file1);
             FileWriter writer2 = new FileWriter(file2)) {
            writer1.write(content);
            writer2.write(content);
        }

        String hash1 = hashService.calculateHash(file1);
        String hash2 = hashService.calculateHash(file2);

        // Should still work with file-based hashing for non-images
        assertEquals(hash1, hash2, "Non-image files with same content should have same hash");
    }

    @Test
    public void testCalculateHashForDifferentImageTypes(@TempDir Path tempDir) throws IOException {
        // Create the same visual content using TYPE_INT_RGB for better format compatibility
        BufferedImage imageContent = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imageContent.createGraphics();
        g2d.setColor(Color.GREEN);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();

        // Save as PNG (lossless)
        File pngFile1 = tempDir.resolve("image1.png").toFile();
        File pngFile2 = tempDir.resolve("image2.png").toFile();
        
        ImageIO.write(imageContent, "png", pngFile1);
        ImageIO.write(imageContent, "png", pngFile2);

        String hashPng1 = hashService.calculateHash(pngFile1);
        String hashPng2 = hashService.calculateHash(pngFile2);

        // Should have same hash for same visual content saved twice
        assertEquals(hashPng1, hashPng2, "Same visual content should have same hash regardless of save order");
    }
}
