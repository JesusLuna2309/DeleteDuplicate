package com.jesusluna.duplicateremover.ui;

import com.jesusluna.duplicateremover.model.DuplicateGroup;
import com.jesusluna.duplicateremover.service.DuplicateFileScanner;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Progress dialog that shows scanning progress and duplicate results
 * Uses MVVM pattern with Task for background processing
 */
public class ProgressDialog {
    
    private static final Logger logger = LoggerFactory.getLogger(ProgressDialog.class);
    
    private final Stage dialog;
    private final ResourceBundle messages;
    private final VBox mainContainer;
    private final ProgressBar progressBar;
    private final Label progressLabel;
    private final Button okButton;
    private final Button cancelButton;
    
    private DuplicateFileScanner scanner;
    private ScrollPane resultsPane;
    
    public ProgressDialog(Stage owner, ResourceBundle messages) {
        this.messages = messages;
        this.dialog = new Stage();
        
        dialog.initOwner(owner);
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.initStyle(StageStyle.UTILITY);
        dialog.setTitle(messages.getString("progress.title"));
        dialog.setResizable(false);
        
        // Main container
        mainContainer = new VBox(15);
        mainContainer.setAlignment(Pos.CENTER);
        mainContainer.setPadding(new Insets(20));
        mainContainer.setStyle("-fx-background-color: #121212;");
        
        // Progress section
        Label titleLabel = new Label(messages.getString("progress.scanning"));
        titleLabel.setFont(Font.font("Segoe UI Semibold", 18));
        titleLabel.setTextFill(Color.web("#00bfff"));
        
        progressBar = new ProgressBar(0);
        progressBar.setPrefWidth(400);
        progressBar.setStyle("-fx-accent: #0078d7;");
        
        progressLabel = new Label(messages.getString("progress.starting"));
        progressLabel.setFont(Font.font("Segoe UI", 12));
        progressLabel.setTextFill(Color.web("#cccccc"));
        
        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        okButton = new Button(messages.getString("button.ok"));
        okButton.setStyle(buttonStyle());
        okButton.setDisable(true);
        okButton.setOnAction(e -> dialog.close());
        
        cancelButton = new Button(messages.getString("button.cancel"));
        cancelButton.setStyle(buttonStyle());
        cancelButton.setOnAction(e -> cancelScan());
        
        buttonBox.getChildren().addAll(okButton, cancelButton);
        
        mainContainer.getChildren().addAll(titleLabel, progressBar, progressLabel, buttonBox);
        
        Scene scene = new Scene(mainContainer, 500, 200);
        dialog.setScene(scene);
    }
    
    public void startScan(File directory, boolean includeSubfolders) {
        scanner = new DuplicateFileScanner(directory, includeSubfolders);
        
        // Bind progress
        progressBar.progressProperty().bind(scanner.progressProperty());
        progressLabel.textProperty().bind(scanner.messageProperty());
        
        // Handle completion
        scanner.setOnSucceeded(e -> {
            List<DuplicateGroup> duplicates = scanner.getValue();
            showResults(duplicates);
        });
        
        scanner.setOnFailed(e -> {
            logger.error("Scan failed", scanner.getException());
            showError(messages.getString("error.scan.failed"));
        });
        
        scanner.setOnCancelled(e -> {
            progressLabel.setText(messages.getString("progress.cancelled"));
            okButton.setDisable(false);
        });
        
        // Start scanning in background thread
        Thread scanThread = new Thread(scanner);
        scanThread.setDaemon(true);
        scanThread.start();
        
        dialog.show();
    }
    
    private void showResults(List<DuplicateGroup> duplicates) {
        Platform.runLater(() -> {
            // Clear progress UI
            mainContainer.getChildren().clear();
            
            // Results header
            Label resultsTitle = new Label(messages.getString("results.title"));
            resultsTitle.setFont(Font.font("Segoe UI Semibold", 18));
            resultsTitle.setTextFill(Color.web("#00bfff"));
            
            Label resultsSubtitle = new Label(
                String.format(messages.getString("results.subtitle"), duplicates.size())
            );
            resultsSubtitle.setFont(Font.font("Segoe UI", 12));
            resultsSubtitle.setTextFill(Color.web("#cccccc"));
            
            if (duplicates.isEmpty()) {
                Label noDuplicates = new Label(messages.getString("results.none"));
                noDuplicates.setFont(Font.font("Segoe UI", 14));
                noDuplicates.setTextFill(Color.web("#888888"));
                
                mainContainer.getChildren().addAll(resultsTitle, noDuplicates);
            } else {
                // Create results view
                VBox resultsContainer = createResultsView(duplicates);
                
                resultsPane = new ScrollPane(resultsContainer);
                resultsPane.setFitToWidth(true);
                resultsPane.setPrefHeight(400);
                resultsPane.setStyle("-fx-background: #121212; -fx-background-color: #121212;");
                VBox.setVgrow(resultsPane, Priority.ALWAYS);
                
                // Action buttons
                HBox actionBox = new HBox(10);
                actionBox.setAlignment(Pos.CENTER);
                
                Button deleteButton = new Button(messages.getString("button.delete.selected"));
                deleteButton.setStyle(buttonStyle());
                deleteButton.setOnAction(e -> deleteSelectedFiles(resultsContainer));
                
                Button closeButton = new Button(messages.getString("button.close"));
                closeButton.setStyle(buttonStyle());
                closeButton.setOnAction(e -> dialog.close());
                
                actionBox.getChildren().addAll(deleteButton, closeButton);
                
                mainContainer.getChildren().addAll(resultsTitle, resultsSubtitle, resultsPane, actionBox);
            }
            
            // Enable close
            okButton.setDisable(false);
            cancelButton.setDisable(true);
            
            // Resize dialog
            dialog.setWidth(700);
            dialog.setHeight(600);
            dialog.centerOnScreen();
        });
    }
    
    private VBox createResultsView(List<DuplicateGroup> duplicates) {
        VBox container = new VBox(10);
        container.setPadding(new Insets(10));
        
        for (DuplicateGroup group : duplicates) {
            // Group header
            Label groupLabel = new Label(
                String.format(messages.getString("results.group.header"), 
                    group.getFileCount(), formatFileSize(group.getTotalSize()))
            );
            groupLabel.setFont(Font.font("Segoe UI Semibold", 13));
            groupLabel.setTextFill(Color.web("#00bfff"));
            groupLabel.setPadding(new Insets(10, 0, 5, 0));
            
            container.getChildren().add(groupLabel);
            
            // Files in group
            for (File file : group.getFiles()) {
                DuplicateFileItem item = new DuplicateFileItem(file);
                container.getChildren().add(item);
            }
            
            // Separator
            Separator separator = new Separator();
            separator.setStyle("-fx-background-color: #333;");
            container.getChildren().add(separator);
        }
        
        return container;
    }
    
    private void deleteSelectedFiles(VBox resultsContainer) {
        List<File> filesToDelete = new ArrayList<>();
        
        // Collect selected files
        for (var node : resultsContainer.getChildren()) {
            if (node instanceof DuplicateFileItem item) {
                if (item.isSelected()) {
                    filesToDelete.add(item.getFile());
                }
            }
        }
        
        if (filesToDelete.isEmpty()) {
            showError(messages.getString("error.no.selection"));
            return;
        }
        
        // Confirm deletion
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.initOwner(dialog);
        confirm.setTitle(messages.getString("delete.confirm.title"));
        confirm.setHeaderText(
            String.format(messages.getString("delete.confirm.header"), filesToDelete.size())
        );
        confirm.setContentText(messages.getString("delete.confirm.content"));
        
        if (confirm.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
            return;
        }
        
        // Delete files
        int deleted = 0;
        List<String> errors = new ArrayList<>();
        
        for (File file : filesToDelete) {
            if (file.delete()) {
                deleted++;
                logger.info("Deleted file: {}", file.getAbsolutePath());
            } else {
                errors.add(file.getName());
                logger.warn("Failed to delete file: {}", file.getAbsolutePath());
            }
        }
        
        // Show summary
        Alert summary = new Alert(Alert.AlertType.INFORMATION);
        summary.initOwner(dialog);
        summary.setTitle(messages.getString("delete.summary.title"));
        summary.setHeaderText(
            String.format(messages.getString("delete.summary.header"), deleted)
        );
        
        if (!errors.isEmpty()) {
            summary.setContentText(
                messages.getString("delete.summary.errors") + "\n" + String.join(", ", errors)
            );
        }
        
        summary.showAndWait();
        
        // Refresh view by removing deleted items
        resultsContainer.getChildren().removeIf(node -> 
            node instanceof DuplicateFileItem item && 
            item.isSelected() && 
            !item.getFile().exists()
        );
    }
    
    private void cancelScan() {
        if (scanner != null && scanner.isRunning()) {
            scanner.cancel();
            logger.info("Scan cancelled by user");
        }
    }
    
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(dialog);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private String buttonStyle() {
        return "-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-size: 14px; " +
               "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;";
    }
    
    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}
