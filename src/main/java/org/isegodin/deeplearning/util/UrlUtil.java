package org.isegodin.deeplearning.util;

import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author isegodin
 */
public class UrlUtil {

    @SneakyThrows
    public static File loadToFile(URL dataUrl, Path targetPath) {
        File file = targetPath.toFile();
        if (file.exists()) {
            return file;
        }

        try (OutputStream os = new FileOutputStream(file)) {
            load(dataUrl, os);
        }

        return file;
    }

    public static byte[] loadToBytes(URL dataUrl) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        load(dataUrl, out);
        return out.toByteArray();
    }

    @SneakyThrows
    private static void load(URL dataUrl, OutputStream outputStream) {
        try (InputStream is = dataUrl.openStream()) {
            try (OutputStream os = outputStream) {
                int read;
                byte[] buffer = new byte[1024];
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        }
    }
}
