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
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for automatic deletion workflow:
 * 1. Scan for duplicates
 * 2. Automatically delete all non-original files
 * 3. Verify only originals remain
 */
public class AutomaticDeletionIntegrationTest {

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
    
    /**
     * Helper method to scan directory and find duplicate groups
     */
    private java.util.List<DuplicateGroup> scanForDuplicates(Path tempDir) throws IOException {
        File[] files = tempDir.toFile().listFiles();
        assertNotNull(files, "Files array should not be null");
        
        Map<String, DuplicateGroup> hashGroups = new HashMap<>();
        for (File file : files) {
            if (file.isFile()) {
                String hash = hashService.calculateHash(file);
                hashGroups.computeIfAbsent(hash, DuplicateGroup::new).addFile(file);
            }
        }
        
        return hashGroups.values().stream()
            .filter(DuplicateGroup::isDuplicate)
            .toList();
    }

    @Test
    public void testAutomaticDeletionWorkflow(@TempDir Path tempDir) 
            throws IOException, InterruptedException {
        
        // Step 1: Create test data with known modification times
        File redOriginal = createImage(tempDir, "red_original.png", Color.RED);
        Thread.sleep(50);
        File redDup1 = createImage(tempDir, "red_dup1.png", Color.RED);
        Thread.sleep(50);
        File redDup2 = createImage(tempDir, "red_dup2.png", Color.RED);
        
        File blueOriginal = createImage(tempDir, "blue_original.png", Color.BLUE);
        Thread.sleep(50);
        File blueDup1 = createImage(tempDir, "blue_dup1.png", Color.BLUE);
        
        File uniqueFile = createImage(tempDir, "unique.png", Color.GREEN);
        
        // Step 2: Scan for duplicates
        java.util.List<DuplicateGroup> duplicateGroups = scanForDuplicates(tempDir);
        assertEquals(2, duplicateGroups.size(), "Should find 2 duplicate groups");
        
        // Step 3: Count files to delete (simulate automatic deletion logic)
        int filesToDelete = 0;
        for (DuplicateGroup group : duplicateGroups) {
            filesToDelete += group.getFileCount() - 1; // All except original
        }
        
        assertEquals(3, filesToDelete, "Should plan to delete 3 files");
        
        // Step 4: Simulate automatic deletion (delete all non-originals)
        int deleted = 0;
        java.util.List<String> errors = new ArrayList<>();
        
        for (DuplicateGroup group : duplicateGroups) {
            File original = group.getOriginalFile();
            
            for (File file : group.getFiles()) {
                if (!file.equals(original)) {
                    if (file.delete()) {
                        deleted++;
                    } else {
                        errors.add(file.getName());
                    }
                }
            }
        }
        
        assertEquals(3, deleted, "Should have deleted 3 files");
        assertTrue(errors.isEmpty(), "Should have no deletion errors");
        
        // Step 5: Verify only originals and unique files remain
        assertTrue(redOriginal.exists(), "Red original should still exist");
        assertFalse(redDup1.exists(), "Red duplicate 1 should be deleted");
        assertFalse(redDup2.exists(), "Red duplicate 2 should be deleted");
        
        assertTrue(blueOriginal.exists(), "Blue original should still exist");
        assertFalse(blueDup1.exists(), "Blue duplicate should be deleted");
        
        assertTrue(uniqueFile.exists(), "Unique file should still exist");
        
        // Step 6: Verify remaining files count
        File[] remainingFiles = tempDir.toFile().listFiles();
        assertNotNull(remainingFiles, "Remaining files should not be null");
        assertEquals(3, remainingFiles.length, "Should have 3 files remaining (2 originals + 1 unique)");
    }

    @Test
    public void testAutomaticDeletionWithNoDuplicates(@TempDir Path tempDir) 
            throws IOException {
        
        // Create only unique files
        createImage(tempDir, "file1.png", Color.RED);
        createImage(tempDir, "file2.png", Color.BLUE);
        createImage(tempDir, "file3.png", Color.GREEN);
        
        // Scan for duplicates
        java.util.List<DuplicateGroup> duplicateGroups = scanForDuplicates(tempDir);
        assertTrue(duplicateGroups.isEmpty(), "Should find no duplicate groups");
        
        // Count files to delete (should be 0)
        int filesToDelete = 0;
        for (DuplicateGroup group : duplicateGroups) {
            filesToDelete += group.getFileCount() - 1;
        }
        
        assertEquals(0, filesToDelete, "Should have 0 files to delete");
        
        // Verify all files still exist
        File[] remainingFiles = tempDir.toFile().listFiles();
        assertNotNull(remainingFiles, "Remaining files should not be null");
        assertEquals(3, remainingFiles.length, "All 3 unique files should remain");
    }

    @Test
    public void testAutomaticDeletionPreservesOldestFile(@TempDir Path tempDir) 
            throws IOException, InterruptedException {
        
        // Create files with specific ordering
        File file3 = createImage(tempDir, "file_3.png", Color.MAGENTA);
        Thread.sleep(50);
        File file1 = createImage(tempDir, "file_1.png", Color.MAGENTA);
        Thread.sleep(50);
        File file2 = createImage(tempDir, "file_2.png", Color.MAGENTA);
        
        // Scan for duplicates
        java.util.List<DuplicateGroup> duplicateGroups = scanForDuplicates(tempDir);
        assertEquals(1, duplicateGroups.size(), "Should find 1 duplicate group");
        
        DuplicateGroup group = duplicateGroups.get(0);
        File original = group.getOriginalFile();
        
        // The oldest file should be file_3.png
        assertEquals(file3, original, "Oldest file (file_3.png) should be the original");
        
        // Simulate automatic deletion
        for (File file : group.getFiles()) {
            if (!file.equals(original)) {
                assertTrue(file.delete(), "Should delete non-original file: " + file.getName());
            }
        }
        
        // Verify only original remains
        assertTrue(file3.exists(), "Original (file_3.png) should remain");
        assertFalse(file1.exists(), "file_1.png should be deleted");
        assertFalse(file2.exists(), "file_2.png should be deleted");
    }
}
