package io.github.ust.mico.core.REST;

import org.springframework.http.HttpStatus;

public class PrometheusRequestFailedException extends Exception{

    private final HttpStatus httpStatus;
    private final String prometheusResponseStatus;

    public PrometheusRequestFailedException(String message,HttpStatus httpStatus,String prometheusResponseStatus){
        super(message);
        this.httpStatus = httpStatus;
        this.prometheusResponseStatus = prometheusResponseStatus;
    }
}
