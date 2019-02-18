package org.isegodin.deeplearning.data;

import org.springframework.core.io.InputStreamSource;

import java.io.InputStream;

/**
 * @author isegodin
 */
public interface FileStreamApi extends InputStreamSource {

    InputStream getInputStream();

    String getContentType();

    String getName();
}
