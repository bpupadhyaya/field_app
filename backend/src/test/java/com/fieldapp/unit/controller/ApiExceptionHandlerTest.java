package com.fieldapp.unit.controller;

import com.fieldapp.controller.ApiExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.validation.BindingResult;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiExceptionHandlerTest {

    @Test
    void handlesValidationErrorsWithFieldMap() throws Exception {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        BeanPropertyBindingResult br = new BeanPropertyBindingResult(new Object(), "req");
        br.addError(new FieldError("req", "username", "must not be blank"));

        Method m = Dummy.class.getDeclaredMethod("method", String.class);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(new MethodParameter(m, 0), br);

        var response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        assertThat(body).containsEntry("error", "Validation failed");
    }

    @Test
    void handlesValidationErrorWithNullDefaultMessage() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        FieldError fieldError = mock(FieldError.class);
        when(fieldError.getField()).thenReturn("username");
        when(fieldError.getDefaultMessage()).thenReturn(null);
        BindingResult bindingResult = mock(BindingResult.class);
        when(bindingResult.getFieldErrors()).thenReturn(java.util.List.of(fieldError));
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        when(ex.getBindingResult()).thenReturn(bindingResult);

        var response = handler.handleValidation(ex);

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();
        @SuppressWarnings("unchecked")
        Map<String, String> fields = (Map<String, String>) body.get("fields");
        assertThat(fields).containsEntry("username", "Invalid value");
    }

    @Test
    void handlesBadRequest() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        var response = handler.handleBadRequest(new IllegalArgumentException("bad"));
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "bad");
    }

    @Test
    void handlesBadRequestWithNullMessage() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        var response = handler.handleBadRequest(new IllegalArgumentException());
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Bad request");
    }

    @Test
    void handlesNotFoundUnauthorizedAndUnexpected() {
        ApiExceptionHandler handler = new ApiExceptionHandler();
        assertThat(handler.handleNotFound(new NoSuchElementException()).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(handler.handleUnauthorized(new org.springframework.security.authentication.BadCredentialsException("x")).getStatusCode())
                .isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(handler.handleUnexpected(new RuntimeException("x")).getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    static class Dummy {
        @SuppressWarnings("unused")
        public void method(String value) {}
    }
}
