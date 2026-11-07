package com.zbob.modules.storage;

import java.io.IOException;
import java.io.InputStream;

/** Stores uploaded files outside the database. */
public interface StorageService {

    /** Saves the content under the key and returns its public URL. */
    String store(String key, InputStream content, long size, String contentType) throws IOException;

    /** Removes the file. Removing a key that does not exist is not an error. */
    void delete(String key) throws IOException;
}
