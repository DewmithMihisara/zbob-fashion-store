package com.zbob.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Map;

/** JSON error body; matches ApiError in @zbob/shared-types. */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(int status, String code, String message, Map<String, String> fieldErrors) {

    public ApiErrorResponse(int status, String code, String message) {
        this(status, code, message, null);
    }
}
