package com.jesusluna.duplicateremover.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Service for calculating pixel-based hashes for image files
 * This allows grouping images that are visually identical even if they have
 * different filenames, paths, or metadata (EXIF, etc.)
 */
public class ImageHashService {

    private static final Logger logger = LoggerFactory.getLogger(ImageHashService.class);
    private static final String ALGORITHM = "SHA-256";

    /**
     * Calculates a hash based on the pixel data of an image file.
     * This hash will be identical for images with the same visual content,
     * regardless of filename, metadata, or other non-pixel attributes.
     *
     * @param imageFile the image file to hash
     * @return hex-encoded hash string based on pixel data
     * @throws IOException if the image cannot be read or is not a valid image format
     */
    public String calculatePixelHash(File imageFile) throws IOException {
        if (imageFile == null || !imageFile.exists() || !imageFile.isFile()) {
            throw new IllegalArgumentException("Invalid image file: " + imageFile);
        }

        logger.debug("Calculating pixel hash for: {}", imageFile.getAbsolutePath());

        try {
            BufferedImage image = ImageIO.read(imageFile);
            
            if (image == null) {
                throw new IOException("Unable to read image file (unsupported format or corrupted): " + imageFile.getName());
            }

            return hashPixelData(image);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(ALGORITHM + " algorithm not available", e);
        }
    }

    /**
     * Extracts pixel data from a BufferedImage and calculates its hash
     *
     * @param image the BufferedImage to hash
     * @return hex-encoded hash string
     * @throws NoSuchAlgorithmException if SHA-256 is not available
     */
    private String hashPixelData(BufferedImage image) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance(ALGORITHM);

        int width = image.getWidth();
        int height = image.getHeight();

        // Add image dimensions to hash to ensure different-sized images get different hashes
        digest.update((byte) (width >> 24));
        digest.update((byte) (width >> 16));
        digest.update((byte) (width >> 8));
        digest.update((byte) width);
        digest.update((byte) (height >> 24));
        digest.update((byte) (height >> 16));
        digest.update((byte) (height >> 8));
        digest.update((byte) height);

        // Extract pixel data row by row for consistent ordering
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = image.getRGB(x, y);
                
                // Extract ARGB components
                digest.update((byte) (rgb >> 24)); // Alpha
                digest.update((byte) (rgb >> 16)); // Red
                digest.update((byte) (rgb >> 8));  // Green
                digest.update((byte) rgb);         // Blue
            }
        }

        byte[] hashBytes = digest.digest();
        String hash = bytesToHex(hashBytes);
        
        logger.debug("Pixel hash calculated: {}", hash);
        return hash;
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
     * Checks if a file is a supported image format that can be processed by ImageIO
     *
     * @param file the file to check
     * @return true if the file is a supported image format
     */
    public boolean isSupportedImageFormat(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return false;
        }

        String name = file.getName().toLowerCase();
        // PNG, JPG/JPEG, BMP, GIF are well-supported by ImageIO
        return name.endsWith(".png") || 
               name.endsWith(".jpg") || 
               name.endsWith(".jpeg") || 
               name.endsWith(".bmp") || 
               name.endsWith(".gif");
    }
}
