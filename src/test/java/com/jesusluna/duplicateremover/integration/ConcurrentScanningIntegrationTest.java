package com.jesusluna.duplicateremover.integration;

import com.jesusluna.duplicateremover.service.FileHashService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test to verify concurrent hash calculation works correctly
 * and produces same results as sequential processing
 */
public class ConcurrentScanningIntegrationTest {

    /**
     * Helper to create a test text file
     */
    private File createTextFile(Path tempDir, String filename, String content) throws IOException {
        File file = tempDir.resolve(filename).toFile();
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(content);
        }
        return file;
    }

    /**
     * Helper to create a test image
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
    public void testConcurrentHashingProducesSameResults(@TempDir Path tempDir) throws Exception {
        // Create a diverse set of files
        List<File> files = new ArrayList<>();
        
        // Text files with duplicates
        files.add(createTextFile(tempDir, "text1.txt", "duplicate text content"));
        files.add(createTextFile(tempDir, "text2.txt", "duplicate text content"));
        files.add(createTextFile(tempDir, "text3.txt", "unique text content"));
        
        // Image files with duplicates
        files.add(createImage(tempDir, "red1.png", 20, 20, Color.RED));
        files.add(createImage(tempDir, "red2.png", 20, 20, Color.RED));
        files.add(createImage(tempDir, "blue1.png", 20, 20, Color.BLUE));

        // Calculate hashes sequentially
        Map<String, List<File>> sequentialGroups = calculateHashesSequentially(files);
        
        // Calculate hashes concurrently
        Map<String, List<File>> concurrentGroups = calculateHashesConcurrently(files, 4);

        // Both methods should produce identical groupings
        assertEquals(sequentialGroups.size(), concurrentGroups.size(), 
                    "Sequential and concurrent should find same number of groups");
        
        // Verify each group has same files
        for (String hash : sequentialGroups.keySet()) {
            assertTrue(concurrentGroups.containsKey(hash), 
                      "Concurrent results should contain all sequential hashes");
            
            List<File> seqFiles = sequentialGroups.get(hash);
            List<File> concFiles = concurrentGroups.get(hash);
            
            assertEquals(seqFiles.size(), concFiles.size(), 
                        "Groups should have same number of files");
            assertTrue(new HashSet<>(seqFiles).equals(new HashSet<>(concFiles)),
                      "Groups should contain same files");
        }
    }

    @Test
    public void testConcurrentHashingPerformanceWithManyFiles(@TempDir Path tempDir) throws Exception {
        // Create many files
        List<File> files = new ArrayList<>();
        
        // Create 100 files with 10 different contents (10 files per group)
        for (int i = 0; i < 100; i++) {
            String content = "Content for group " + (i % 10);
            files.add(createTextFile(tempDir, "file" + i + ".txt", content));
        }

        // Calculate hashes concurrently
        long startTime = System.currentTimeMillis();
        Map<String, List<File>> groups = calculateHashesConcurrently(files, 4);
        long elapsedTime = System.currentTimeMillis() - startTime;

        // Verify results
        assertEquals(10, groups.size(), "Should find 10 unique groups");
        
        // Each group should have 10 files
        for (List<File> group : groups.values()) {
            assertEquals(10, group.size(), "Each group should have 10 files");
        }
        
        // Just verify it completes in reasonable time (not a strict performance test)
        assertTrue(elapsedTime < 10000, "Should complete within 10 seconds");
    }

    @Test
    public void testConcurrentHashingWithMixedFileTypes(@TempDir Path tempDir) throws Exception {
        List<File> files = new ArrayList<>();
        
        // Create mix of text and image duplicates
        files.add(createTextFile(tempDir, "doc1.txt", "same document"));
        files.add(createTextFile(tempDir, "doc2.txt", "same document"));
        files.add(createImage(tempDir, "photo1.png", 15, 15, Color.YELLOW));
        files.add(createImage(tempDir, "photo2.png", 15, 15, Color.YELLOW));
        files.add(createImage(tempDir, "photo3.png", 15, 15, Color.MAGENTA));

        Map<String, List<File>> groups = calculateHashesConcurrently(files, 2);

        // Should find 3 groups: 2 text files, 2 yellow images, 1 magenta image
        assertEquals(3, groups.size(), "Should find 3 unique groups");
        
        // Count groups by size
        long twoFileGroups = groups.values().stream()
                                   .filter(g -> g.size() == 2)
                                   .count();
        long oneFileGroups = groups.values().stream()
                                   .filter(g -> g.size() == 1)
                                   .count();
        
        assertEquals(2, twoFileGroups, "Should have 2 groups with 2 files");
        assertEquals(1, oneFileGroups, "Should have 1 group with 1 file");
    }

    /**
     * Calculate hashes sequentially for comparison
     */
    private Map<String, List<File>> calculateHashesSequentially(List<File> files) throws IOException {
        Map<String, List<File>> groups = new HashMap<>();
        FileHashService hashService = new FileHashService();
        
        for (File file : files) {
            String hash = hashService.calculateHash(file);
            groups.computeIfAbsent(hash, k -> new ArrayList<>()).add(file);
        }
        
        return groups;
    }

    /**
     * Calculate hashes concurrently (similar to DuplicateFileScanner implementation)
     */
    private Map<String, List<File>> calculateHashesConcurrently(List<File> files, int parallelism) 
            throws InterruptedException, ExecutionException {
        Map<String, List<File>> groups = new HashMap<>();
        ExecutorService executor = Executors.newFixedThreadPool(parallelism);
        CompletionService<Map.Entry<File, String>> completionService = 
            new ExecutorCompletionService<>(executor);
        
        try {
            // Submit all hash calculation tasks
            for (File file : files) {
                completionService.submit(() -> {
                    FileHashService hashService = new FileHashService();
                    String hash = hashService.calculateHash(file);
                    return Map.entry(file, hash);
                });
            }
            
            // Collect results
            for (int i = 0; i < files.size(); i++) {
                Future<Map.Entry<File, String>> future = completionService.take();
                Map.Entry<File, String> result = future.get();
                
                groups.computeIfAbsent(result.getValue(), k -> new ArrayList<>())
                      .add(result.getKey());
            }
        } finally {
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }
        
        return groups;
    }

    @Test
    public void testThreadSafetyOfFileHashService(@TempDir Path tempDir) throws Exception {
        // Create files
        List<File> files = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            files.add(createTextFile(tempDir, "file" + i + ".txt", "content " + (i % 5)));
        }

        // Run concurrent hashing multiple times to detect race conditions
        for (int run = 0; run < 5; run++) {
            Map<String, List<File>> groups = calculateHashesConcurrently(files, 4);
            
            // Should always find 5 groups (content 0-4)
            assertEquals(5, groups.size(), 
                        "Should find 5 groups in run " + run);
            
            // Each group should have 4 files
            for (List<File> group : groups.values()) {
                assertEquals(4, group.size(), 
                            "Each group should have 4 files in run " + run);
            }
        }
    }
}
