package com.jesusluna.duplicateremover.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for DuplicateFileScanner
 * Note: Full Task tests are skipped as they require JavaFX toolkit initialization
 */
public class DuplicateFileScannerTest {

    @Test
    public void testIsImageFile(@TempDir Path tempDir) throws IOException {
        File jpgFile = tempDir.resolve("image.jpg").toFile();
        jpgFile.createNewFile();
        
        File pngFile = tempDir.resolve("image.png").toFile();
        pngFile.createNewFile();
        
        File txtFile = tempDir.resolve("text.txt").toFile();
        txtFile.createNewFile();
        
        assertTrue(DuplicateFileScanner.isImageFile(jpgFile));
        assertTrue(DuplicateFileScanner.isImageFile(pngFile));
        assertFalse(DuplicateFileScanner.isImageFile(txtFile));
    }
    
    @Test
    public void testIsImageFileWithDifferentExtensions(@TempDir Path tempDir) throws IOException {
        File gifFile = tempDir.resolve("animation.gif").toFile();
        gifFile.createNewFile();
        
        File bmpFile = tempDir.resolve("bitmap.bmp").toFile();
        bmpFile.createNewFile();
        
        File webpFile = tempDir.resolve("image.webp").toFile();
        webpFile.createNewFile();
        
        File tiffFile = tempDir.resolve("scan.tiff").toFile();
        tiffFile.createNewFile();
        
        assertTrue(DuplicateFileScanner.isImageFile(gifFile));
        assertTrue(DuplicateFileScanner.isImageFile(bmpFile));
        assertTrue(DuplicateFileScanner.isImageFile(webpFile));
        assertTrue(DuplicateFileScanner.isImageFile(tiffFile));
    }
    
    @Test
    public void testIsImageFileCaseInsensitive(@TempDir Path tempDir) throws IOException {
        File jpgUpperCase = tempDir.resolve("IMAGE.JPG").toFile();
        jpgUpperCase.createNewFile();
        
        File pngMixedCase = tempDir.resolve("Image.PnG").toFile();
        pngMixedCase.createNewFile();
        
        assertTrue(DuplicateFileScanner.isImageFile(jpgUpperCase));
        assertTrue(DuplicateFileScanner.isImageFile(pngMixedCase));
    }
}
