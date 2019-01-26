package io.github.ust.mico.core.REST;

import org.springframework.http.HttpStatus;

public class PrometheusRequestFailedException extends Exception{

    private final HttpStatus httpStatus;
    private final String responseStatus;

    public PrometheusRequestFailedException(String message,HttpStatus httpStatus,String responseStatus){
        super(message);
        this.httpStatus = httpStatus;
        this.responseStatus = responseStatus;
    }
}
