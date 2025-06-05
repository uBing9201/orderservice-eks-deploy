package com.playdata.userservice.common.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Setter @Getter @ToString
@NoArgsConstructor
public class CommonResDto {

    private int statusCode;
    private String statusMessage;
    private Object result; // 요청에 따라 전달할 데이터가 그때그때 다르니까 Object 타입으로 선언함.

    public CommonResDto(HttpStatus httpStatus, String statusMessage, Object result) {
        this.statusCode = httpStatus.value();
        this.statusMessage = statusMessage;
        this.result = result;
    }
}
