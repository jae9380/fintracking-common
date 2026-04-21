package com.ft.common.exception;

import com.ft.common.response.ApiResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleCustomException_whenAccountNotFound_returns404() {
        CustomException e = new CustomException(ErrorCode.ACCOUNT_NOT_FOUND);

        ResponseEntity<ApiResponse<Void>> response = handler.handleCustomException(e);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody().statusCode()).isEqualTo(404);
        assertThat(response.getBody().message()).isEqualTo("계좌를 찾을 수 없습니다.");
    }

    @Test
    void handleCustomException_whenUnauthorized_returns401() {
        CustomException e = new CustomException(ErrorCode.AUTH_INVALID_TOKEN);

        ResponseEntity<ApiResponse<Void>> response = handler.handleCustomException(e);

        assertThat(response.getStatusCode().value()).isEqualTo(401);
        assertThat(response.getBody().statusCode()).isEqualTo(401);
    }

    @Test
    void handleValidation_returnsFirstFieldErrorMessage() throws Exception {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "object");
        bindingResult.addError(new FieldError("object", "email", "이메일은 비어있을 수 없습니다."));
        MethodArgumentNotValidException e = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ApiResponse<Void>> response = handler.handleValidation(e);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        assertThat(response.getBody().message()).isEqualTo("이메일은 비어있을 수 없습니다.");
    }

    @Test
    void handleException_whenUnexpectedError_returns500() {
        Exception e = new RuntimeException("예상치 못한 오류");

        ResponseEntity<ApiResponse<Void>> response = handler.handleException(e);

        assertThat(response.getStatusCode().value()).isEqualTo(500);
        assertThat(response.getBody().statusCode()).isEqualTo(500);
    }
}
