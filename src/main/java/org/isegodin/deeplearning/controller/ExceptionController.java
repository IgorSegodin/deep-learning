package org.isegodin.deeplearning.controller;

import lombok.extern.slf4j.Slf4j;
import org.isegodin.deeplearning.data.response.ResponseWrapper;
import org.isegodin.deeplearning.exception.RestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletResponse;

/**
 * @author isegodin
 */
@Slf4j(topic = "org.isegodin.deeplearning.rest.exception")
@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(RestException.class)
    @ResponseBody
    public ResponseWrapper<String> restException(HttpServletResponse response, HandlerMethod handlerMethod, RestException restException) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.value() <= restException.getHttpStatus()) {
            log.warn("Rest exception [handler=\"" + handlerMethod.toString() + "\"]", restException);
        } else if (HttpStatus.BAD_REQUEST.value() <= restException.getHttpStatus()) {
            log.debug("Rest exception [httpStatus={}, message=\"{}\", handler=\"{}\"]", restException.getHttpStatus(), restException.getMessage(), handlerMethod.toString());
        } else {
            log.warn("Rest exception with non-error status [httpStatus={}, message=\"{}\", handler=\"{}\"]", restException.getHttpStatus(), restException.getMessage(), handlerMethod.toString());
        }

        response.setStatus(restException.getHttpStatus());
        return ResponseWrapper.<String>builder()
                .status(ResponseWrapper.Status.ERROR)
                .data(restException.getMessage())
                .build();
    }
}
