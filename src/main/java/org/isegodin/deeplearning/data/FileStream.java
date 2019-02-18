package org.isegodin.deeplearning.data;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

/**
 * @author isegodin
 */
@AllArgsConstructor
public class FileStream implements FileStreamApi {

    private final File file;

    private final String contentType;

    @SneakyThrows
    @Override
    public InputStream getInputStream() {
        return new FileInputStream(file);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public String getName() {
        return file.getName();
    }
}

