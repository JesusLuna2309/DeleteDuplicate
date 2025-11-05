package com.jesusluna.duplicateremover.service;

import java.io.File;

/**
 * Immutable result class for hash calculation tasks
 * Thread-safe by design (all fields are final)
 */
public class HashResult {
    private final File file;
    private final String hash;
    private final Exception error;

    private HashResult(File file, String hash, Exception error) {
        this.file = file;
        this.hash = hash;
        this.error = error;
    }

    /**
     * Creates a successful hash result
     */
    public static HashResult success(File file, String hash) {
        return new HashResult(file, hash, null);
    }

    /**
     * Creates a failed hash result
     */
    public static HashResult failure(File file, Exception error) {
        return new HashResult(file, null, error);
    }

    public File getFile() {
        return file;
    }

    public String getHash() {
        return hash;
    }

    public Exception getError() {
        return error;
    }

    public boolean isSuccess() {
        return error == null;
    }
}
