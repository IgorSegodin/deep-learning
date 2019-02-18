package org.isegodin.deeplearning.data.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author isegodin
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseWrapper<T> {

    @Builder.Default
    private Status status = Status.OK;

    private T data;

    public enum Status {
        OK, ERROR;
    }

}
