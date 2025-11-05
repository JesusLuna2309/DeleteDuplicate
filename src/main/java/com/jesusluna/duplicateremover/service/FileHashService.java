package com.jesusluna.duplicateremover.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service for calculating file hashes
 * Uses SHA-256 algorithm for secure and reliable duplicate detection
 * For image files, uses pixel-based hashing to group visually identical images
 */
public class FileHashService {

    private static final Logger logger = LoggerFactory.getLogger(FileHashService.class);
    private static final String ALGORITHM = "SHA-256";
    private static final int BUFFER_SIZE = 8192; // 8KB buffer
    
    private final ImageHashService imageHashService;
    private final boolean useAdvancedImageDetection;

    public FileHashService() {
        this(true); // Default to advanced image detection for backward compatibility
    }
    
    public FileHashService(boolean useAdvancedImageDetection) {
        this.imageHashService = new ImageHashService();
        this.useAdvancedImageDetection = useAdvancedImageDetection;
    }

    /**
     * Calculates the hash of a file.
     * For supported image formats (PNG, JPG, BMP, GIF), calculates a pixel-based hash
     * to group visually identical images regardless of metadata or filename.
     * For other files, calculates a standard file content hash.
     *
     * @param file the file to hash
     * @return hex-encoded hash string
     * @throws IllegalArgumentException if file is null, doesn't exist, or is not a regular file
     * @throws IOException if file cannot be read
     */
    public String calculateHash(File file) throws IOException {
        if (file == null || !file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid file: " + file);
        }

        // Try pixel-based hashing for supported image formats (only if enabled)
        if (useAdvancedImageDetection && imageHashService.isSupportedImageFormat(file)) {
            try {
                logger.debug("Calculating pixel-based hash for image: {}", file.getAbsolutePath());
                return imageHashService.calculatePixelHash(file);
            } catch (IOException e) {
                logger.warn("Failed to calculate pixel hash for {}, falling back to file hash: {}", 
                           file.getName(), e.getMessage());
                // Fall through to regular file hashing
            }
        }

        // Regular file content hashing for non-images or when image hashing fails
        logger.debug("Calculating file hash for: {} (size: {} bytes)", 
                     file.getAbsolutePath(), file.length());

        try (InputStream input = new FileInputStream(file)) {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            String hash = bytesToHex(hashBytes);
            
            logger.debug("Hash calculated: {}", hash);
            return hash;

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(ALGORITHM + " algorithm not available", e);
        }
    }

    /**
     * Converts byte array to hexadecimal string
     *
     * @param bytes byte array to convert
     * @return hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * Checks if two files have the same hash
     *
     * @param file1 first file
     * @param file2 second file
     * @return true if hashes match
     * @throws IOException if files cannot be read
     */
    public boolean areFilesIdentical(File file1, File file2) throws IOException {
        // Quick check: if sizes differ, files are different
        if (file1.length() != file2.length()) {
            logger.debug("Files have different sizes, skipping hash calculation");
            return false;
        }

        String hash1 = calculateHash(file1);
        String hash2 = calculateHash(file2);
        
        boolean identical = hash1.equals(hash2);
        logger.debug("Files {} identical", identical ? "are" : "are not");
        
        return identical;
    }
}
