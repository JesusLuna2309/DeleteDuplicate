package com.jesusluna.duplicateremover.integration;

import com.jesusluna.duplicateremover.service.FileHashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Manual verification test that demonstrates the complete scenario from the issue:
 * Images with different naming patterns (training_XXXXX.png vs imXXXXX.png) but
 * identical visual content should be grouped together.
 */
public class ManualVerificationTest {

    private final FileHashService hashService = new FileHashService();

    private File createImage(Path dir, String filename, int width, int height, Color color) throws IOException {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, width, height);
        g2d.dispose();

        File imageFile = dir.resolve(filename).toFile();
        ImageIO.write(image, "png", imageFile);
        return imageFile;
    }

    @Test
    public void testCompleteScenarioFromIssue(@TempDir Path tempDir) throws IOException {
        // Create folder structure
        Path trainingFolder = tempDir.resolve("training");
        Path imagesFolder = tempDir.resolve("images");
        trainingFolder.toFile().mkdirs();
        imagesFolder.toFile().mkdirs();

        // SCENARIO: 4 red images with different names but identical content
        createImage(trainingFolder, "training_12345.png", 100, 100, Color.RED);
        createImage(trainingFolder, "training_67890.png", 100, 100, Color.RED);
        createImage(imagesFolder, "im00001.png", 100, 100, Color.RED);
        createImage(imagesFolder, "im00002.png", 100, 100, Color.RED);

        // SCENARIO: 2 blue images with different names but identical content
        createImage(trainingFolder, "training_11111.png", 100, 100, Color.BLUE);
        createImage(imagesFolder, "im00003.png", 100, 100, Color.BLUE);

        // SCENARIO: 1 unique green image
        createImage(imagesFolder, "unique_green.png", 100, 100, Color.GREEN);

        // Simulate the duplicate scanner: group files by hash
        Map<String, List<File>> groups = new HashMap<>();
        scanDirectory(tempDir.toFile(), groups);

        // Verify results
        System.out.println("\n=== VERIFICATION RESULTS ===");
        System.out.println("Total unique hashes: " + groups.size());

        // Should have exactly 3 unique groups (red, blue, green)
        assertEquals(3, groups.size(), "Should have 3 unique image groups");

        // Find the red group (should have 4 files)
        List<File> redGroup = groups.values().stream()
            .filter(list -> list.size() == 4)
            .findFirst()
            .orElse(null);
        assertNotNull(redGroup, "Should have a group with 4 red images");
        System.out.println("\nRED GROUP (4 duplicates):");
        redGroup.forEach(f -> System.out.println("  - " + f.getName()));

        // Verify it contains files from both folders and both naming patterns
        assertTrue(redGroup.stream().anyMatch(f -> f.getName().startsWith("training_")),
            "Red group should contain training_ files");
        assertTrue(redGroup.stream().anyMatch(f -> f.getName().startsWith("im")),
            "Red group should contain im files");

        // Find the blue group (should have 2 files)
        List<File> blueGroup = groups.values().stream()
            .filter(list -> list.size() == 2)
            .findFirst()
            .orElse(null);
        assertNotNull(blueGroup, "Should have a group with 2 blue images");
        System.out.println("\nBLUE GROUP (2 duplicates):");
        blueGroup.forEach(f -> System.out.println("  - " + f.getName()));

        // Find the green group (should have 1 file - unique)
        List<File> greenGroup = groups.values().stream()
            .filter(list -> list.size() == 1)
            .findFirst()
            .orElse(null);
        assertNotNull(greenGroup, "Should have a unique green image");
        System.out.println("\nGREEN GROUP (unique):");
        greenGroup.forEach(f -> System.out.println("  - " + f.getName()));

        System.out.println("\n✅ SUCCESS: Images with identical pixels are correctly grouped together");
        System.out.println("   regardless of their filename or folder location!");
    }

    private void scanDirectory(File dir, Map<String, List<File>> groups) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, groups);
            } else if (file.getName().endsWith(".png")) {
                String hash = hashService.calculateHash(file);
                groups.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
            }
        }
    }
}
