module com.jesusluna.duplicateremover {
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.fxml;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires ch.qos.logback.core;
    
    opens com.jesusluna.duplicateremover to javafx.graphics, javafx.fxml;
    
    exports com.jesusluna.duplicateremover;
}
