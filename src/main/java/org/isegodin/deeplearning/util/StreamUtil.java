package org.isegodin.deeplearning.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author isegodin
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class StreamUtil {

    @SneakyThrows
    public static void readWrite(InputStream source, OutputStream target) {
        try (InputStream is = source) {
            try (OutputStream os = target) {
                readWriteUnsafe(is, os);
            }
        }
    }

    @SneakyThrows
    public static void readWriteUnsafe(InputStream source, OutputStream target) {
        int read;
        byte[] buffer = new byte[1024];
        while ((read = source.read(buffer)) != -1) {
            target.write(buffer, 0, read);
        }
    }
}
