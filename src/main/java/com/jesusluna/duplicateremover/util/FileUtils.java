package com.jesusluna.duplicateremover.util;

/**
 * Utility class for file operations
 */
public final class FileUtils {
    
    private FileUtils() {
        // Utility class, no instantiation
    }
    
    /**
     * Formats file size in human-readable format
     * @param bytes Size in bytes
     * @return Formatted string (e.g., "1.5 MB")
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = String.valueOf("KMGTPE".charAt(exp - 1));
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
