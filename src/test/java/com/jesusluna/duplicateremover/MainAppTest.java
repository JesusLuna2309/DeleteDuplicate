package com.jesusluna.duplicateremover;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Basic test for MainApp class
 */
public class MainAppTest {

    @Test
    public void testMainAppInstantiation() {
        // Test that MainApp can be instantiated
        MainApp app = new MainApp();
        assertNotNull(app, "MainApp instance should not be null");
    }
}
