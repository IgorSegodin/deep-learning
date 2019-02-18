package org.isegodin.deeplearning.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

import javax.validation.constraints.NotNull;

/**
 * @author isegodin
 */
@Getter
@Setter
public class RestException extends RuntimeException {

    @NotNull
    private final int httpStatus;

    @Builder
    private RestException(String message, Throwable cause, int httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

    public static class RestExceptionBuilder {
        public RestExceptionBuilder httpStatus(HttpStatus status) {
            this.httpStatus = status.value();
            return this;
        }
    }
}
