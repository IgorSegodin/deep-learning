package org.isegodin.deeplearning.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.isegodin.deeplearning.data.FileStreamApi;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Helper for apache fileupload servlet
 *
 * @author isegodin
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileStreamUtil {

    public static MultipartDataProcessor initDataProcessor(HttpServletRequest request) {
        return new MultipartDataProcessor(request);
    }

    public static class MultipartDataProcessor {

        private final HttpServletRequest request;
        private final Map<String, Consumer<String>> paramConsumerMap = new HashMap<>();
        private final Map<String, Consumer<FileStreamApi>> fileParamConsumerMap = new HashMap<>();

        private MultipartDataProcessor(HttpServletRequest request) {
            this.request = request;
        }

        public MultipartDataProcessor paramConsumer(String name, Consumer<String> consumer) {
            paramConsumerMap.put(name, consumer);
            return this;
        }

        public MultipartDataProcessor fileConsumer(String name, Consumer<FileStreamApi> consumer) {
            fileParamConsumerMap.put(name, consumer);
            return this;
        }

        @SneakyThrows
        public void process() {
            ServletFileUpload servletFileUpload = new ServletFileUpload();
            FileItemIterator iterator = servletFileUpload.getItemIterator(request);
            while (iterator.hasNext()) {
                FileItemStream item = iterator.next();

                if (item.isFormField()) {
                    // TODO
//                    Optional.ofNullable(paramConsumerMap.get(item.getFieldName()))
//                            .ifPresent(c -> c.accept(item.getString()));
                } else {
                    Optional.ofNullable(fileParamConsumerMap.get(item.getFieldName()))
                            .ifPresent(c -> c.accept(new FileStream(item)));
                }
            }
        }

    }

    private static class FileStream implements FileStreamApi {

        private final FileItemStream source;

        public FileStream(FileItemStream source) {
            this.source = source;
        }

        @SneakyThrows
        @Override
        public InputStream getInputStream() {
            return source.openStream();
        }

        @Override
        public String getContentType() {
            return source.getContentType();
        }

        @Override
        public String getName() {
            return source.getName();
        }
    }
}
