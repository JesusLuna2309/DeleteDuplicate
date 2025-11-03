package com.jesusluna.duplicateremover.integration;

import com.jesusluna.duplicateremover.model.DuplicateGroup;
import com.jesusluna.duplicateremover.service.FileHashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test simulating the complete workflow:
 * 1. Scan for duplicates
 * 2. Identify original files
 * 3. Verify that only duplicates (not originals) would be deletable
 * 
 * Note: This test uses FileHashService directly instead of DuplicateFileScanner
 * to avoid JavaFX toolkit initialization requirements in headless environment.
 */
public class WorkflowIntegrationTest {

    private final FileHashService hashService = new FileHashService();

    private File createImage(Path tempDir, String filename, Color color) throws IOException {
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();
        g2d.setColor(color);
        g2d.fillRect(0, 0, 100, 100);
        g2d.dispose();

        File imageFile = tempDir.resolve(filename).toFile();
        ImageIO.write(image, "png", imageFile);
        return imageFile;
    }

    @Test
    public void testCompleteWorkflowWithOriginalProtection(@TempDir Path tempDir) 
            throws IOException, InterruptedException {
        
        // Step 1: Create test data - Red duplicates
        File redOriginal = createImage(tempDir, "training_001.png", Color.RED);
        Thread.sleep(50);
        File redDup1 = createImage(tempDir, "training_002.png", Color.RED);
        Thread.sleep(50);
        File redDup2 = createImage(tempDir, "im_001.png", Color.RED);
        
        // Blue duplicates
        File blueOriginal = createImage(tempDir, "photo_a.png", Color.BLUE);
        Thread.sleep(50);
        File blueDup1 = createImage(tempDir, "photo_b.png", Color.BLUE);
        
        // Unique file
        File uniqueFile = createImage(tempDir, "unique.png", Color.GREEN);
        
        // Step 2: Manually scan for duplicates (simulate scanner behavior)
        File[] files = tempDir.toFile().listFiles();
        assertNotNull(files, "Files array should not be null");
        
        Map<String, DuplicateGroup> hashGroups = new HashMap<>();
        for (File file : files) {
            if (file.isFile()) {
                String hash = hashService.calculateHash(file);
                hashGroups.computeIfAbsent(hash, DuplicateGroup::new).addFile(file);
            }
        }
        
        // Filter only duplicate groups
        List<DuplicateGroup> duplicateGroups = hashGroups.values().stream()
            .filter(DuplicateGroup::isDuplicate)
            .toList();
        
        // Step 3: Verify scan results
        assertNotNull(duplicateGroups, "Duplicate groups should not be null");
        assertEquals(2, duplicateGroups.size(), "Should find 2 duplicate groups (red and blue)");
        
        // Step 4: Verify original file identification for each group
        for (DuplicateGroup group : duplicateGroups) {
            assertTrue(group.isDuplicate(), "Each group should have duplicates");
            
            File original = group.getOriginalFile();
            assertNotNull(original, "Each group should have an original file");
            
            // Verify original is the oldest file in the group
            for (File file : group.getFiles()) {
                if (!file.equals(original)) {
                    assertTrue(file.lastModified() >= original.lastModified(),
                        "Original should be oldest or equal in modification time");
                }
            }
            
            // Step 5: Simulate "Select All" behavior - only non-originals should be selectable
            List<File> selectableFiles = group.getFiles().stream()
                .filter(file -> !file.equals(original))
                .toList();
            
            assertEquals(group.getFileCount() - 1, selectableFiles.size(),
                "All files except original should be selectable");
        }
        
        // Step 6: Verify specific groups
        DuplicateGroup redGroup = duplicateGroups.stream()
            .filter(g -> g.getFileCount() == 3)
            .findFirst()
            .orElse(null);
        
        DuplicateGroup blueGroup = duplicateGroups.stream()
            .filter(g -> g.getFileCount() == 2)
            .findFirst()
            .orElse(null);
        
        assertNotNull(redGroup, "Should find red group with 3 files");
        assertNotNull(blueGroup, "Should find blue group with 2 files");
        
        // Verify red group original
        assertEquals(redOriginal, redGroup.getOriginalFile(),
            "Red group original should be training_001.png (oldest)");
        
        // Verify blue group original
        assertEquals(blueOriginal, blueGroup.getOriginalFile(),
            "Blue group original should be photo_a.png (oldest)");
        
        // Step 7: Verify protected files cannot be in deletion list
        List<File> allOriginals = duplicateGroups.stream()
            .map(DuplicateGroup::getOriginalFile)
            .toList();
        
        assertEquals(2, allOriginals.size(), "Should have 2 original files protected");
        assertTrue(allOriginals.contains(redOriginal), "Red original should be protected");
        assertTrue(allOriginals.contains(blueOriginal), "Blue original should be protected");
        
        // Step 8: Count deletable files (all duplicates minus originals)
        int totalFiles = duplicateGroups.stream()
            .mapToInt(DuplicateGroup::getFileCount)
            .sum();
        int deletableFiles = totalFiles - duplicateGroups.size(); // total - originals
        
        assertEquals(3, deletableFiles, "Should have 3 deletable duplicate files");
    }

    @Test
    public void testOriginalProtectionWithIdenticalModificationTimes(@TempDir Path tempDir) 
            throws IOException {
        
        // Create files with same content
        File fileC = createImage(tempDir, "c_image.png", Color.CYAN);
        File fileA = createImage(tempDir, "a_image.png", Color.CYAN);
        File fileB = createImage(tempDir, "b_image.png", Color.CYAN);
        
        // Set same modification time
        long time = System.currentTimeMillis();
        fileC.setLastModified(time);
        fileA.setLastModified(time);
        fileB.setLastModified(time);
        
        // Scan manually
        File[] files = tempDir.toFile().listFiles();
        assertNotNull(files, "Files array should not be null");
        
        Map<String, DuplicateGroup> hashGroups = new HashMap<>();
        for (File file : files) {
            if (file.isFile()) {
                String hash = hashService.calculateHash(file);
                hashGroups.computeIfAbsent(hash, DuplicateGroup::new).addFile(file);
            }
        }
        
        List<DuplicateGroup> groups = hashGroups.values().stream()
            .filter(DuplicateGroup::isDuplicate)
            .toList();
        
        assertEquals(1, groups.size(), "Should find 1 duplicate group");
        
        DuplicateGroup group = groups.get(0);
        File original = group.getOriginalFile();
        
        // When modification times are equal, lexicographically first file should be original
        assertEquals(fileA, original, "When times are equal, lexicographically first file (a_image.png) should be original");
    }
}
