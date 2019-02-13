package io.github.ust.mico.core.exception;

import org.springframework.http.HttpStatus;

public class PrometheusRequestFailedException extends Exception{

    public static final long serialVersionUID = 381515548494367817L;

    private final HttpStatus httpStatus;
    private final String prometheusResponseStatus;

    public PrometheusRequestFailedException(String message,HttpStatus httpStatus,String prometheusResponseStatus){
        super(message);
        this.httpStatus = httpStatus;
        this.prometheusResponseStatus = prometheusResponseStatus;
    }
}
