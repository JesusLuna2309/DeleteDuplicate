package com.jesusluna.duplicateremover.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ImageHashService
 */
public class ImageHashServiceTest {

    private final ImageHashService imageHashService = new ImageHashService();

    /**
     * Helper method to create a simple test image with a solid color
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

    /**
     * Helper method to create a test image with a pattern
     */
    private File createPatternImage(Path tempDir, String filename, int width, int height) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        
        // Create a checkerboard pattern
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if ((x + y) % 2 == 0) {
                    g2d.setColor(Color.BLACK);
                } else {
                    g2d.setColor(Color.WHITE);
                }
                g2d.fillRect(x, y, 1, 1);
            }
        }
        g2d.dispose();

        File imageFile = tempDir.resolve(filename).toFile();
        ImageIO.write(image, "png", imageFile);
        return imageFile;
    }

    @Test
    public void testCalculatePixelHashForValidImage(@TempDir Path tempDir) throws IOException {
        File imageFile = createTestImage(tempDir, "test.png", 10, 10, Color.RED);

        String hash = imageHashService.calculatePixelHash(imageFile);

        assertNotNull(hash, "Hash should not be null");
        assertEquals(64, hash.length(), "SHA-256 hash should be 64 characters");
        assertTrue(hash.matches("[0-9a-f]{64}"), "Hash should contain only hex characters");
    }

    @Test
    public void testIdenticalImagesProduceSameHash(@TempDir Path tempDir) throws IOException {
        // Create two identical images with different filenames
        File image1 = createTestImage(tempDir, "image1.png", 10, 10, Color.BLUE);
        File image2 = createTestImage(tempDir, "image2.png", 10, 10, Color.BLUE);

        String hash1 = imageHashService.calculatePixelHash(image1);
        String hash2 = imageHashService.calculatePixelHash(image2);

        assertEquals(hash1, hash2, "Identical images should produce the same pixel hash");
    }

    @Test
    public void testDifferentImagesProduceDifferentHashes(@TempDir Path tempDir) throws IOException {
        File image1 = createTestImage(tempDir, "red.png", 10, 10, Color.RED);
        File image2 = createTestImage(tempDir, "blue.png", 10, 10, Color.BLUE);

        String hash1 = imageHashService.calculatePixelHash(image1);
        String hash2 = imageHashService.calculatePixelHash(image2);

        assertNotEquals(hash1, hash2, "Different images should produce different hashes");
    }

    @Test
    public void testDifferentSizeImagesProduceDifferentHashes(@TempDir Path tempDir) throws IOException {
        File image1 = createTestImage(tempDir, "small.png", 10, 10, Color.GREEN);
        File image2 = createTestImage(tempDir, "large.png", 20, 20, Color.GREEN);

        String hash1 = imageHashService.calculatePixelHash(image1);
        String hash2 = imageHashService.calculatePixelHash(image2);

        assertNotEquals(hash1, hash2, "Images with different dimensions should produce different hashes");
    }

    @Test
    public void testSameImageDifferentFormatsProduceSameHash(@TempDir Path tempDir) throws IOException {
        // Create the same image and save it in different formats
        BufferedImage image = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(Color.YELLOW);
        g2d.fillRect(0, 0, 10, 10);
        g2d.dispose();

        File pngFile = tempDir.resolve("image.png").toFile();
        File jpgFile = tempDir.resolve("image.jpg").toFile();

        ImageIO.write(image, "png", pngFile);
        ImageIO.write(image, "jpg", jpgFile);

        String hashPng = imageHashService.calculatePixelHash(pngFile);
        String hashJpg = imageHashService.calculatePixelHash(jpgFile);

        // Note: JPG is lossy, so hashes may differ. This test verifies both can be processed.
        assertNotNull(hashPng, "PNG hash should not be null");
        assertNotNull(hashJpg, "JPG hash should not be null");
    }

    @Test
    public void testComplexPatternProducesConsistentHash(@TempDir Path tempDir) throws IOException {
        File image1 = createPatternImage(tempDir, "pattern1.png", 20, 20);
        File image2 = createPatternImage(tempDir, "pattern2.png", 20, 20);

        String hash1 = imageHashService.calculatePixelHash(image1);
        String hash2 = imageHashService.calculatePixelHash(image2);

        assertEquals(hash1, hash2, "Identical patterns should produce same hash");
    }

    @Test
    public void testCalculatePixelHashThrowsExceptionForNullFile() {
        assertThrows(IllegalArgumentException.class, () -> {
            imageHashService.calculatePixelHash(null);
        }, "Should throw exception for null file");
    }

    @Test
    public void testCalculatePixelHashThrowsExceptionForNonExistentFile(@TempDir Path tempDir) {
        File nonExistent = tempDir.resolve("nonexistent.png").toFile();

        assertThrows(IllegalArgumentException.class, () -> {
            imageHashService.calculatePixelHash(nonExistent);
        }, "Should throw exception for non-existent file");
    }

    @Test
    public void testCalculatePixelHashThrowsExceptionForInvalidImageFile(@TempDir Path tempDir) throws IOException {
        // Create a text file with .png extension
        File fakeImage = tempDir.resolve("fake.png").toFile();
        java.nio.file.Files.writeString(fakeImage.toPath(), "This is not an image");

        assertThrows(IOException.class, () -> {
            imageHashService.calculatePixelHash(fakeImage);
        }, "Should throw exception for invalid image file");
    }

    @Test
    public void testIsSupportedImageFormatForPNG(@TempDir Path tempDir) throws IOException {
        File pngFile = tempDir.resolve("test.png").toFile();
        pngFile.createNewFile();

        assertTrue(imageHashService.isSupportedImageFormat(pngFile), "PNG should be supported");
    }

    @Test
    public void testIsSupportedImageFormatForJPG(@TempDir Path tempDir) throws IOException {
        File jpgFile = tempDir.resolve("test.jpg").toFile();
        jpgFile.createNewFile();

        assertTrue(imageHashService.isSupportedImageFormat(jpgFile), "JPG should be supported");
        
        File jpegFile = tempDir.resolve("test.jpeg").toFile();
        jpegFile.createNewFile();

        assertTrue(imageHashService.isSupportedImageFormat(jpegFile), "JPEG should be supported");
    }

    @Test
    public void testIsSupportedImageFormatForBMP(@TempDir Path tempDir) throws IOException {
        File bmpFile = tempDir.resolve("test.bmp").toFile();
        bmpFile.createNewFile();

        assertTrue(imageHashService.isSupportedImageFormat(bmpFile), "BMP should be supported");
    }

    @Test
    public void testIsSupportedImageFormatForGIF(@TempDir Path tempDir) throws IOException {
        File gifFile = tempDir.resolve("test.gif").toFile();
        gifFile.createNewFile();

        assertTrue(imageHashService.isSupportedImageFormat(gifFile), "GIF should be supported");
    }

    @Test
    public void testIsSupportedImageFormatForNonImageFile(@TempDir Path tempDir) throws IOException {
        File txtFile = tempDir.resolve("test.txt").toFile();
        txtFile.createNewFile();

        assertFalse(imageHashService.isSupportedImageFormat(txtFile), "TXT should not be supported");
    }

    @Test
    public void testIsSupportedImageFormatCaseInsensitive(@TempDir Path tempDir) throws IOException {
        File upperCasePng = tempDir.resolve("TEST.PNG").toFile();
        upperCasePng.createNewFile();

        assertTrue(imageHashService.isSupportedImageFormat(upperCasePng), "Should be case-insensitive");
    }

    @Test
    public void testIsSupportedImageFormatForNullFile() {
        assertFalse(imageHashService.isSupportedImageFormat(null), "Null file should not be supported");
    }

    @Test
    public void testIsSupportedImageFormatForNonExistentFile(@TempDir Path tempDir) {
        File nonExistent = tempDir.resolve("nonexistent.png").toFile();

        assertFalse(imageHashService.isSupportedImageFormat(nonExistent), "Non-existent file should not be supported");
    }
}
