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
    
    public DuplicateFileItem(File file) {
        this.file = file;
        this.checkBox = new CheckBox();
        
        setAlignment(Pos.CENTER_LEFT);
        setSpacing(10);
        setPadding(new Insets(5, 10, 5, 10));
        setStyle("-fx-background-color: #1e1e1e; -fx-border-color: #333; -fx-border-radius: 4; -fx-background-radius: 4;");
        
        // Checkbox
        checkBox.setStyle("-fx-text-fill: #cccccc;");
        
        // Thumbnail or icon
        ImageView thumbnail = createThumbnail(file);
        
        // File info
        VBox fileInfo = new VBox(3);
        fileInfo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(fileInfo, Priority.ALWAYS);
        
        Label fileNameLabel = new Label(file.getName());
        fileNameLabel.setFont(Font.font("Segoe UI", 13));
        fileNameLabel.setTextFill(Color.web("#e0e0e0"));
        
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
    
    private ImageView createThumbnail(File file) {
        ImageView imageView = new ImageView();
        imageView.setFitWidth(THUMBNAIL_SIZE);
        imageView.setFitHeight(THUMBNAIL_SIZE);
        imageView.setPreserveRatio(true);
        
        if (DuplicateFileScanner.isImageFile(file)) {
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
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    public File getFile() {
        return file;
    }
    
    public boolean isSelected() {
        return checkBox.isSelected();
    }
    
    public void setSelected(boolean selected) {
        checkBox.setSelected(selected);
    }
}
