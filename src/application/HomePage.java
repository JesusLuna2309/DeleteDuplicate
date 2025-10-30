package application;

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

import java.io.File;
import java.io.InputStream;

public class HomePage extends Application {

    private CheckBox includeSubfolders;
    private TextField directoryField;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Dataset Cleaner Pro");

        // --- Icono de la ventana ---
        Image windowIcon = new Image(getClass().getResourceAsStream("/resources/icons/carpeta.png"));
        stage.getIcons().add(windowIcon);

        // --- Logo dentro de la UI ---
        ImageView logo = new ImageView(windowIcon);
        logo.setFitWidth(60);
        logo.setFitHeight(60);
        logo.setPreserveRatio(true);

        // --- Título con logo ---
        Label title = new Label("Dataset Cleaner Pro");
        title.setFont(Font.font("Segoe UI Semibold", 28));
        title.setTextFill(Color.web("#00bfff"));

        HBox titleBox = new HBox(15, logo, title);
        titleBox.setAlignment(Pos.CENTER);

        Label subtitle = new Label("Limpia tus duplicados con precisión quirúrgica");
        subtitle.setFont(Font.font("Segoe UI", 14));
        subtitle.setTextFill(Color.web("#aaaaaa"));

        // --- Campo de directorio ---
        HBox dirBox = new HBox(10);
        dirBox.setAlignment(Pos.CENTER);

        directoryField = new TextField();
        directoryField.setPromptText("Selecciona la carpeta con tus imágenes...");
        directoryField.setPrefWidth(400);
        directoryField.setStyle("-fx-background-color: #1e1e1e; -fx-text-fill: #e0e0e0; -fx-border-color: #333; -fx-border-radius: 6; -fx-background-radius: 6;");

        Button browseButton = new Button("📁 Elegir carpeta");
        browseButton.setStyle(buttonStyle());
        browseButton.setOnAction(e -> openDirectoryChooser(stage));

        dirBox.getChildren().addAll(directoryField, browseButton);

        // --- Checkbox ---
        includeSubfolders = new CheckBox("Permitir actuar sobre subcarpetas");
        includeSubfolders.setTextFill(Color.web("#cccccc"));

        // --- Botón iniciar ---
        Button startButton = new Button("🚀 Empezar limpieza");
        startButton.setStyle(buttonStyle());
        startButton.setOnAction(e -> confirmAndStart(stage));

        // --- Contenedor principal ---
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(50));
        root.setStyle("-fx-background-color: #121212;");
        root.getChildren().addAll(titleBox, subtitle, dirBox, includeSubfolders, startButton);

        // --- Animación Fade in ---
        FadeTransition fade = new FadeTransition(Duration.millis(1200), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        Scene scene = new Scene(root, 750, 500);
        stage.setScene(scene);
        stage.show();
    }

    private String buttonStyle() {
        return "-fx-background-color: #0078d7; -fx-text-fill: white; -fx-font-size: 14px; " +
               "-fx-background-radius: 8; -fx-cursor: hand; -fx-padding: 10 20 10 20;";
    }

    private void openDirectoryChooser(Stage stage) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Seleccionar carpeta de imágenes");
        InputStream dummy;
        File selected = chooser.showDialog(stage);
        if (selected != null) {
            directoryField.setText(selected.getAbsolutePath());
        }
    }

    private void confirmAndStart(Stage stage) {
        String dir = directoryField.getText();
        if (dir.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Por favor, selecciona una carpeta antes de continuar.");
            return;
        }

        if (includeSubfolders.isSelected()) {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirmación requerida");
            confirm.setHeaderText("¿Seguro que quieres incluir subcarpetas?");
            confirm.setContentText("Esto puede aumentar significativamente el tiempo de análisis.");
            if (confirm.showAndWait().get() != ButtonType.OK) return;
        }

        showAlert(Alert.AlertType.INFORMATION, "🔍 Iniciando análisis de duplicados en:\n" + dir);
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.initStyle(StageStyle.UTILITY);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
