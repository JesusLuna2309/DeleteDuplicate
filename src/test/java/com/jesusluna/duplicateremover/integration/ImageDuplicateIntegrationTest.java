package com.jesusluna.duplicateremover.integration;

import com.jesusluna.duplicateremover.service.FileHashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify that images with identical pixels but different names/metadata
 * are properly grouped together using pixel-based hashing.
 */
public class ImageDuplicateIntegrationTest {

    private final FileHashService hashService = new FileHashService();

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
    public void testIdenticalImagesWithDifferentNamesGroupTogether(@TempDir Path tempDir) throws IOException {
        // Simulate the issue scenario: images with different naming patterns but identical content
        File trainingImage1 = createImage(tempDir, "training_12345.png", 50, 50, Color.RED);
        File trainingImage2 = createImage(tempDir, "training_67890.png", 50, 50, Color.RED);
        File imImage1 = createImage(tempDir, "im00001.png", 50, 50, Color.RED);
        File imImage2 = createImage(tempDir, "im00002.png", 50, 50, Color.RED);
        
        // Calculate hashes
        String hash1 = hashService.calculateHash(trainingImage1);
        String hash2 = hashService.calculateHash(trainingImage2);
        String hash3 = hashService.calculateHash(imImage1);
        String hash4 = hashService.calculateHash(imImage2);

        // All should have the same hash since they have identical pixels
        assertEquals(hash1, hash2, "training_12345.png and training_67890.png should have same hash");
        assertEquals(hash1, hash3, "training_12345.png and im00001.png should have same hash");
        assertEquals(hash1, hash4, "training_12345.png and im00002.png should have same hash");
    }

    @Test
    public void testDifferentImagesGetDifferentHashes(@TempDir Path tempDir) throws IOException {
        // Create images with different visual content
        File redImage = createImage(tempDir, "red_image.png", 50, 50, Color.RED);
        File blueImage = createImage(tempDir, "blue_image.png", 50, 50, Color.BLUE);
        File greenImage = createImage(tempDir, "green_image.png", 50, 50, Color.GREEN);

        String hashRed = hashService.calculateHash(redImage);
        String hashBlue = hashService.calculateHash(blueImage);
        String hashGreen = hashService.calculateHash(greenImage);

        // All should have different hashes
        assertNotEquals(hashRed, hashBlue, "Red and blue images should have different hashes");
        assertNotEquals(hashRed, hashGreen, "Red and green images should have different hashes");
        assertNotEquals(hashBlue, hashGreen, "Blue and green images should have different hashes");
    }

    @Test
    public void testMixedFilesImageAndNonImage(@TempDir Path tempDir) throws IOException {
        // Create a mix of image and non-image files
        File imageFile1 = createImage(tempDir, "photo1.png", 20, 20, Color.YELLOW);
        File imageFile2 = createImage(tempDir, "photo2.png", 20, 20, Color.YELLOW);
        
        File textFile1 = tempDir.resolve("document1.txt").toFile();
        File textFile2 = tempDir.resolve("document2.txt").toFile();
        
        String content = "Hello World";
        try (FileWriter writer1 = new FileWriter(textFile1);
             FileWriter writer2 = new FileWriter(textFile2)) {
            writer1.write(content);
            writer2.write(content);
        }

        // Images with same pixels should have same hash
        String imageHash1 = hashService.calculateHash(imageFile1);
        String imageHash2 = hashService.calculateHash(imageFile2);
        assertEquals(imageHash1, imageHash2, "Identical images should have same hash");

        // Text files with same content should have same hash
        String textHash1 = hashService.calculateHash(textFile1);
        String textHash2 = hashService.calculateHash(textFile2);
        assertEquals(textHash1, textHash2, "Identical text files should have same hash");

        // Images and text files should have different hashes
        assertNotEquals(imageHash1, textHash1, "Image and text file should have different hashes");
    }

    @Test
    public void testImageGroupingSimulation(@TempDir Path tempDir) throws IOException {
        // Simulate finding duplicates: create multiple files and group by hash
        Map<String, Integer> hashGroups = new HashMap<>();

        // Create multiple images with same content but different names
        File[] identicalImages = {
            createImage(tempDir, "training_001.png", 30, 30, Color.CYAN),
            createImage(tempDir, "training_002.png", 30, 30, Color.CYAN),
            createImage(tempDir, "im_001.png", 30, 30, Color.CYAN),
            createImage(tempDir, "im_002.png", 30, 30, Color.CYAN)
        };

        // Create some unique images
        File uniqueImage1 = createImage(tempDir, "unique1.png", 30, 30, Color.MAGENTA);
        File uniqueImage2 = createImage(tempDir, "unique2.png", 30, 30, Color.ORANGE);

        // Group by hash
        for (File file : identicalImages) {
            String hash = hashService.calculateHash(file);
            hashGroups.put(hash, hashGroups.getOrDefault(hash, 0) + 1);
        }

        String hashUnique1 = hashService.calculateHash(uniqueImage1);
        String hashUnique2 = hashService.calculateHash(uniqueImage2);
        hashGroups.put(hashUnique1, hashGroups.getOrDefault(hashUnique1, 0) + 1);
        hashGroups.put(hashUnique2, hashGroups.getOrDefault(hashUnique2, 0) + 1);

        // Verify grouping results
        assertEquals(3, hashGroups.size(), "Should have 3 unique groups");
        
        // One group should have 4 identical images
        long groupsWith4Files = hashGroups.values().stream().filter(count -> count == 4).count();
        assertEquals(1, groupsWith4Files, "Should have exactly one group with 4 files");

        // Two groups should have 1 file each (unique images)
        long groupsWith1File = hashGroups.values().stream().filter(count -> count == 1).count();
        assertEquals(2, groupsWith1File, "Should have exactly two groups with 1 file each");
    }

    @Test
    public void testImageInDifferentDirectories(@TempDir Path tempDir) throws IOException {
        // Simulate images in different directories (common scenario)
        Path dir1 = tempDir.resolve("folder1");
        Path dir2 = tempDir.resolve("folder2");
        dir1.toFile().mkdirs();
        dir2.toFile().mkdirs();

        // Same image content in different directories with different names
        File image1 = createImage(dir1, "photo_vacation.png", 40, 40, Color.PINK);
        File image2 = createImage(dir2, "IMG_12345.png", 40, 40, Color.PINK);

        String hash1 = hashService.calculateHash(image1);
        String hash2 = hashService.calculateHash(image2);

        assertEquals(hash1, hash2, "Images in different directories with same content should have same hash");
    }
}
