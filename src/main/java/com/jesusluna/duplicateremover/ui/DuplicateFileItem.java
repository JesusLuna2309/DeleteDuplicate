package com.jesusluna.duplicateremover.ui;

import com.jesusluna.duplicateremover.service.DuplicateFileScanner;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;

/**
 * UI component representing a single duplicate file with thumbnail and selection
 */
public class DuplicateFileItem extends HBox {
    
    private static final Logger logger = LoggerFactory.getLogger(DuplicateFileItem.class);
    private static final int THUMBNAIL_SIZE = 48;
    
    private final File file;
    private final CheckBox checkBox;
    private final boolean isOriginal;
    
    public DuplicateFileItem(File file, boolean isOriginal) {
        this.file = file;
        this.isOriginal = isOriginal;
        this.checkBox = new CheckBox();
        
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(5, 10, 5, 10));
        
        // Highlight original files with different style
        if (isOriginal) {
            setStyle("-fx-background-color: #2a2a2a; -fx-border-color: #0078d7; -fx-border-width: 2; -fx-border-radius: 4; -fx-background-radius: 4;");
        } else {
            setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-radius: 4; -fx-background-radius: 4;");
        }
        
        // Checkbox - disabled for original files
        checkBox.setStyle("-fx-text-fill: #cccccc;");
        if (isOriginal) {
            checkBox.setDisable(true);
            checkBox.setSelected(false);
        }
        
        // Thumbnail or icon
        ImageView thumbnail = createThumbnail(file);
        
        // File info
        VBox fileInfo = new VBox(3);
        fileInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fileInfo, Priority.ALWAYS);
        
        // Add [ORIGINAL] tag to filename for original files
        String displayName = isOriginal ? "[ORIGINAL] " + file.getName() : file.getName();
        Label fileNameLabel = new Label(displayName);
        fileNameLabel.setFont(Font.font("Segoe UI", 13));
        fileNameLabel.setTextFill(isOriginal ? Color.web("#00bfff") : Color.web("#e0e0e0"));
        
        Label filePathLabel = new Label(file.getAbsolutePath());
        filePathLabel.setFont(Font.font("Segoe UI", 10));
        filePathLabel.setTextFill(Color.web("#888888"));
        filePathLabel.setWrapText(false);
        filePathLabel.setMaxWidth(400);
        
        Label fileSizeLabel = new Label(formatFileSize(file.length()));
        fileSizeLabel.setFont(Font.font("Segoe UI", 10));
        fileSizeLabel.setTextFill(Color.web("#888888"));
        
        fileInfo.getChildren().addAll(fileNameLabel, filePathLabel, fileSizeLabel);
        
        getChildren().addAll(checkBox, thumbnail, fileInfo);
    }
    
    // Backwards compatibility constructor
    public DuplicateFileItem(File file) {
        this(file, false);
    }
    
    private ImageView createThumbnail(File file) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(THUMBNAIL_SIZE);
        imageView.setFitHeight(THUMBNAIL_SIZE);
        imageView.setPreserveRatio(true);
        
        // Only load thumbnails for image files under 50MB to prevent memory issues
        if (DuplicateFileScanner.isImageFile(file) && file.length() < 50_000_000) {
            try (FileInputStream fis = new FileInputStream(file)) {
                Image image = new Image(fis, THUMBNAIL_SIZE, THUMBNAIL_SIZE, true, true);
                imageView.setImage(image);
            } catch (Exception e) {
                logger.debug("Could not load thumbnail for: {}", file.getName());
                setDefaultIcon(imageView);
            }
        } else {
            setDefaultIcon(imageView);
        }
        
        return imageView;
    }
    
    private void setDefaultIcon(ImageView imageView) {
        try {
            Image icon = new Image(getClass().getResourceAsStream("/icons/carpeta.png"));
            imageView.setImage(icon);
        } catch (Exception e) {
            logger.debug("Could not load default icon");
        }
    }
    
    private String formatFileSize(long bytes) {
        return com.jesusluna.duplicateremover.util.FileUtils.formatFileSize(bytes);
    }
    
    public File getFile() {
        return file;
    }
    
    public boolean isSelected() {
        return checkBox.isSelected();
    }
    
    public void setSelected(boolean selected) {
        // Don't allow selecting original files
        if (!isOriginal) {
            checkBox.setSelected(selected);
        }
    }
    
    public boolean isOriginal() {
        return isOriginal;
    }
}
