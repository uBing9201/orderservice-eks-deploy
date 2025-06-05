package com.playdata.userservice.common.exception;

import com.playdata.userservice.common.dto.CommonErrorDto;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class CommonExceptionHandler {

    // Controller 단에서 발생하는 모든 예외를 일괄 처리하는 클래스
    // 실제 예외는 Service 계층에서 발생하지만, 따로 예외 처리가 없는 경우
    // 메서드를 호출한 상위 계층으로 전파됩니다.

    // 옳지 않은 입력값 전달 시 호출되는 메서드
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> illegalHandler(IllegalArgumentException e) {
        e.printStackTrace();
        CommonErrorDto errorDto
                = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.BAD_REQUEST);
    }

    // 엔터티를 찾지 못했을 때 예외가 발생할 것이고, 이 메서드가 호출될 것이다.
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> entityNotFountHandler(EntityNotFoundException e) {
        e.printStackTrace();
        CommonErrorDto errorDto
                = new CommonErrorDto(HttpStatus.NOT_FOUND, e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.NOT_FOUND);
    }

    // 특정 권한을 가지지 못한 사용자가 요청을 보냈을 때 내쫓는 메서드
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<?> authDeniedHandler(AuthorizationDeniedException e) {
        e.printStackTrace();
        CommonErrorDto errorDto
                = new CommonErrorDto(HttpStatus.FORBIDDEN, e.getMessage());
        return new ResponseEntity<>(errorDto, HttpStatus.FORBIDDEN);
    }

    // 미처 준비하지 못한 타입의 예외가 발생했을 시 처리할 메서드
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exceptionHandler(Exception e) {
        e.printStackTrace();
        CommonErrorDto errorDto
                = new CommonErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "server error");
        return new ResponseEntity<>(errorDto, HttpStatus.INTERNAL_SERVER_ERROR); // 500 에러
    }

}








