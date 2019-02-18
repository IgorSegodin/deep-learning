package org.isegodin.deeplearning.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Path;

/**
 * @author isegodin
 * @deprecated Should not upload data in memory, no need for this util
 */
@Deprecated
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
        StreamUtil.readWrite(dataUrl.openStream(), outputStream);
    }
}
