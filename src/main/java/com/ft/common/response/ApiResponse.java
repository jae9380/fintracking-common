package com.ft.common.response;

import static com.ft.common.response.ApiResultType.*;

public record ApiResponse<T>(
        int statusCode,
        String message,
        ApiResultType resultType,
        T data
) {

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "OK", SUCCESS, data);
    }

    public static <T> ApiResponse<T> created(T data) {
        return new ApiResponse<>(201, "Created", CREATED, data);
    }

    public static ApiResponse<Void> noContent() {
        return new ApiResponse<>(204, "No Content", NO_CONTENT, null);
    }

    public static ApiResponse<Void> error(int status, String message) {
        return new ApiResponse<>(status, message, ERROR, null);
    }
}
