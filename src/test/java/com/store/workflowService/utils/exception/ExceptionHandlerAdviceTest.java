package com.store.workflowService.utils.exception;

import com.store.workflowService.utils.exception.advice.ExceptionHandlerAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ExceptionHandlerAdviceTest {

    private ExceptionHandlerAdvice advice;

    @BeforeEach
    void setUp() {
        advice = new ExceptionHandlerAdvice();
    }

    @Test
    void handleNotFound_returnsNotFoundBody() {
        ResponseEntity<Map<String, Object>> res = advice.handleNotFound(new VideoNotFoundException("not found"));
        assertThat(res.getStatusCode()).isEqualTo(org.springframework.http.HttpStatus.NOT_FOUND);
        assertThat(res.getBody()).containsKeys("code", "message", "timestamp");
    }

}
