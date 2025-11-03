package com.jesusluna.duplicateremover;

import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.*;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Main application class for Duplicate File Remover
 * Provides a modern dark-themed UI for detecting and removing duplicate files
 */
public class MainApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(MainApp.class);
    
    private CheckBox includeSubfolders;
    private TextField directoryField;
    private ResourceBundle messages;

    @Override
    public void start(Stage stage) {
        try {
            // Initialize i18n
            initializeLocale();
            
            stage.setTitle(messages.getString("app.title"));

            // Window icon
            Image windowIcon = new Image(getClass().getResourceAsStream("/icons/carpeta.png"));
            stage.getIcons().add(windowIcon);

            // Logo inside UI
            ImageView logo = new ImageView(windowIcon);
            logo.setFitWidth(60);
            logo.setFitHeight(60);
            logo.setPreserveRatio(true);

            // Title with logo
            Label title = new Label(messages.getString("app.title"));
            title.setFont(Font.font("Segoe UI Semibold", 28));
            title.setTextFill(Color.web("#00bfff"));

            HBox titleBox = new HBox(15, logo, title);
            titleBox.setAlignment(Pos.CENTER);

            Label subtitle = new Label(messages.getString("app.subtitle"));
            subtitle.setFont(Font.font("Segoe UI", 14));
            subtitle.setTextFill(Color.web("#aaaaaa"));

            // Directory field
            HBox dirBox = new HBox(10);
            dirBox.setAlignment(Pos.CENTER);

            directoryField = new TextField();
            directoryField.setPromptText(messages.getString("field.directory.prompt"));
            directoryField.setPrefWidth(400);
            directoryField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0; -fx-border-color: #333; -fx-border-radius: 6; -fx-background-radius: 6;");

            Button browseButton = new Button(messages.getString("button.browse"));
            browseButton.setStyle(buttonStyle());
            browseButton.setOnAction(e -> openDirectoryChooser(stage));

            dirBox.getChildren().addAll(directoryField, browseButton);

            // Checkbox
            includeSubfolders = new CheckBox(messages.getString("checkbox.subfolders"));
            includeSubfolders.setTextFill(Color.web("#cccccc"));

            // Start button
            Button startButton = new Button(messages.getString("button.start"));
            startButton.setStyle(buttonStyle());
            startButton.setOnAction(e -> confirmAndStart(stage));

            // Main container
            VBox root = new VBox(25);
            root.setAlignment(Pos.CENTER);
            root.setPadding(new Insets(50));
            root.setStyle("-fx-background-color: #121212;");
            root.getChildren().addAll(titleBox, subtitle, dirBox, includeSubfolders, startButton);

            // Fade in animation
            FadeTransition fade = new FadeTransition(Duration.millis(1200), root);
            fade.setFromValue(0);
            fade.setToValue(1);
            fade.play();

            Scene scene = new Scene(root, 750, 500);
            stage.setScene(scene);
            stage.show();
            
            logger.info("Application started successfully");
        } catch (Exception e) {
            logger.error("Error starting application", e);
            throw new RuntimeException("Failed to start application", e);
        }
    }

    private void initializeLocale() {
        // Default to Spanish, can be configured later
        Locale locale = new Locale("es", "ES");
        try {
            messages = ResourceBundle.getBundle("i18n.messages", locale);
        } catch (Exception e) {
            logger.warn("Could not load messages bundle for locale {}, using default", locale);
            // Fallback to default
            messages = ResourceBundle.getBundle("i18n.messages");
        }
    }

    private String buttonStyle() {
        return "-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-size: 14px; " +
               "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;";
    }

    private void openDirectoryChooser(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(messages.getString("dialog.selectFolder.title"));
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            directoryField.setText(selected.getAbsolutePath());
            logger.info("Selected directory: {}", selected.getAbsolutePath());
        }
    }

    private void confirmAndStart(Stage stage) {
        String dir = directoryField.getText();
        if (dir.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, messages.getString("alert.noDirectory"));
            return;
        }
        
        File directory = new File(dir);
        if (!directory.exists() || !directory.isDirectory()) {
            showAlert(Alert.AlertType.WARNING, messages.getString("alert.noDirectory"));
            return;
        }

        if (includeSubfolders.isSelected()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle(messages.getString("dialog.confirm.title"));
            confirm.setHeaderText(messages.getString("dialog.confirm.subfolders.header"));
            confirm.setContentText(messages.getString("dialog.confirm.subfolders.content"));
            if (confirm.showAndWait().get() != ButtonType.OK) {
                logger.info("User cancelled subfolder scanning");
                return;
            }
        }

        logger.info("Starting duplicate analysis in: {}", dir);
        
        // Open progress dialog and start scan
        com.jesusluna.duplicateremover.ui.ProgressDialog progressDialog = 
            new com.jesusluna.duplicateremover.ui.ProgressDialog(stage, messages);
        progressDialog.startScan(directory, includeSubfolders.isSelected());
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        logger.info("Launching Duplicate File Remover application");
        launch(args);
    }
}
