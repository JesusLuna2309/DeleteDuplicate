package com.jesusluna.duplicateremover.integration;

import com.jesusluna.duplicateremover.model.DuplicateGroup;
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
 * Integration test to verify that original files are correctly identified
 * and protected from deletion in duplicate groups.
 */
public class OriginalFileProtectionTest {

    /**
     * Helper method to create a test image
     */
    private File createImage(Path tempDir, String filename, int width, int height, Color color) throws IOException {
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
    public void testOriginalFileIsOldestByModificationDate(@TempDir Path tempDir) throws IOException, InterruptedException {
        DuplicateGroup group = new DuplicateGroup("test-hash");

        // Create first file (oldest)
        File file1 = tempDir.resolve("oldest.txt").toFile();
        try (FileWriter writer = new FileWriter(file1)) {
            writer.write("content");
        }

        // Wait to ensure different modification times
        Thread.sleep(100);

        // Create second file
        File file2 = tempDir.resolve("middle.txt").toFile();
        try (FileWriter writer = new FileWriter(file2)) {
            writer.write("content");
        }

        Thread.sleep(100);

        // Create third file (newest)
        File file3 = tempDir.resolve("newest.txt").toFile();
        try (FileWriter writer = new FileWriter(file3)) {
            writer.write("content");
        }

        // Add files in random order
        group.addFile(file2);
        group.addFile(file3);
        group.addFile(file1);

        // Verify file1 is identified as original (oldest)
        File original = group.getOriginalFile();
        assertEquals(file1, original, "The oldest file should be identified as original");
    }

    @Test
    public void testOriginalFileUsesLexicographicOrderAsTieBreaker(@TempDir Path tempDir) throws IOException {
        DuplicateGroup group = new DuplicateGroup("test-hash");

        File fileC = tempDir.resolve("c_file.txt").toFile();
        File fileA = tempDir.resolve("a_file.txt").toFile();
        File fileB = tempDir.resolve("b_file.txt").toFile();

        // Create all files
        try (FileWriter writer1 = new FileWriter(fileC);
             FileWriter writer2 = new FileWriter(fileA);
             FileWriter writer3 = new FileWriter(fileB)) {
            writer1.write("content");
            writer2.write("content");
            writer3.write("content");
        }

        // Set same modification time for all files
        long time = System.currentTimeMillis();
        fileC.setLastModified(time);
        fileA.setLastModified(time);
        fileB.setLastModified(time);

        // Add files in random order
        group.addFile(fileC);
        group.addFile(fileB);
        group.addFile(fileA);

        // Verify fileA is identified as original (lexicographically first)
        File original = group.getOriginalFile();
        assertEquals(fileA, original, "The lexicographically first file should be original when dates are equal");
    }

    @Test
    public void testOriginalProtectionForImageDuplicates(@TempDir Path tempDir) throws IOException, InterruptedException {
        DuplicateGroup group = new DuplicateGroup("image-hash");

        // Create images with same content but different names
        File trainingImage = createImage(tempDir, "training_12345.png", 50, 50, Color.RED);
        
        Thread.sleep(100); // Ensure different modification times
        
        File imImage1 = createImage(tempDir, "im00001.png", 50, 50, Color.RED);
        
        Thread.sleep(100);
        
        File imImage2 = createImage(tempDir, "im00002.png", 50, 50, Color.RED);

        // Add to group
        group.addFile(imImage1);
        group.addFile(trainingImage);
        group.addFile(imImage2);

        // Verify training image is original (oldest)
        File original = group.getOriginalFile();
        assertEquals(trainingImage, original, "The oldest image should be marked as original");
        
        // Verify the group has correct structure
        assertEquals(3, group.getFileCount());
        assertTrue(group.isDuplicate());
    }

    @Test
    public void testMultipleGroupsEachHaveTheirOwnOriginal(@TempDir Path tempDir) throws IOException, InterruptedException {
        // Create two groups of duplicates
        DuplicateGroup redGroup = new DuplicateGroup("red-hash");
        DuplicateGroup blueGroup = new DuplicateGroup("blue-hash");

        // Red group files
        File redFile1 = createImage(tempDir, "red1.png", 30, 30, Color.RED);
        Thread.sleep(50);
        File redFile2 = createImage(tempDir, "red2.png", 30, 30, Color.RED);
        Thread.sleep(50);
        File redFile3 = createImage(tempDir, "red3.png", 30, 30, Color.RED);

        redGroup.addFile(redFile2);
        redGroup.addFile(redFile3);
        redGroup.addFile(redFile1);

        // Blue group files
        File blueFile1 = createImage(tempDir, "blue1.png", 30, 30, Color.BLUE);
        Thread.sleep(50);
        File blueFile2 = createImage(tempDir, "blue2.png", 30, 30, Color.BLUE);

        blueGroup.addFile(blueFile2);
        blueGroup.addFile(blueFile1);

        // Verify each group has its own original
        assertEquals(redFile1, redGroup.getOriginalFile(), "Red group should have red1 as original");
        assertEquals(blueFile1, blueGroup.getOriginalFile(), "Blue group should have blue1 as original");

        // Verify originals are different
        assertNotEquals(redGroup.getOriginalFile(), blueGroup.getOriginalFile(),
                "Each group should have its own unique original file");
    }

    @Test
    public void testOriginalFileInDifferentDirectories(@TempDir Path tempDir) throws IOException, InterruptedException {
        // Create subdirectories
        Path dir1 = tempDir.resolve("folder1");
        Path dir2 = tempDir.resolve("folder2");
        dir1.toFile().mkdirs();
        dir2.toFile().mkdirs();

        DuplicateGroup group = new DuplicateGroup("multi-dir-hash");

        // Create identical images in different directories
        File image1 = createImage(dir1, "photo.png", 40, 40, Color.GREEN);
        Thread.sleep(100);
        File image2 = createImage(dir2, "photo.png", 40, 40, Color.GREEN);

        group.addFile(image2);
        group.addFile(image1);

        // Verify the oldest file is original, regardless of directory
        File original = group.getOriginalFile();
        assertEquals(image1, original, "The oldest file should be original, regardless of directory");
    }
}
