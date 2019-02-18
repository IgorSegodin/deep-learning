package org.isegodin.deeplearning.service;

import lombok.SneakyThrows;
import org.isegodin.deeplearning.util.StreamUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Paths;

/**
 * @author isegodin
 */
@Component
public class FileService {

    @Value("${deep-learning.download.folder}")
    private String downloadFolderPath;

    public void addFile(String name, InputStream source) {
        File folder = getRootFolder();

        File file = new File(folder, name);

        try {
            StreamUtil.readWrite(source, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Can not save file: " + file.getAbsolutePath());
        }
    }

    public File getFile(String name) {
        File folder = getRootFolder();
        File file = new File(folder, name);

        return file;

    }

    @SneakyThrows
    private String resolveContentType(File file) {
        try (InputStream is = new FileInputStream(file)) {
            return URLConnection.guessContentTypeFromStream(is);
        }
    }

    private File getRootFolder() {
        File folder = Paths.get(downloadFolderPath).toFile();
        if (!folder.exists()) {
            folder.mkdirs();
        }

        if (!folder.isDirectory()) {
            throw new RuntimeException("Path should point to a directory: " + downloadFolderPath);
        }
        return folder;
    }
}
