package org.isegodin.deeplearning.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author isegodin
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZipUtil {

    @SneakyThrows
    public static void unzip(File sourceFile, File targetFolder) {
        if (!targetFolder.exists()) {
            targetFolder.mkdirs();
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceFile))) {
            ZipEntry nextEntry;
            while ((nextEntry = zis.getNextEntry()) != null) {
                File entryFile = new File(targetFolder, nextEntry.getName());
                if (entryFile.exists()) {
                    continue;
                }
                if (nextEntry.isDirectory()) {
                    entryFile.mkdir();
                } else {
                    try (OutputStream os = new FileOutputStream(entryFile)) {
                        StreamUtil.readWriteUnsafe(zis, os);
                    }
                }
            }
        }
    }
}
