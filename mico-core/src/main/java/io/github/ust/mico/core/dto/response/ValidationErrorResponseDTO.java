/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.github.ust.mico.core.dto.response;

import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Value
public class ValidationErrorResponseDTO {

    /**
     * HTTP status code that is used for validation errors.
     * <p>
     * "422 Unprocessable Entity" is used instead of "400 Bad Request" because the request was syntactically correct,
     * but semantically incorrect.
     */
    public final static HttpStatus HTTP_STATUS = UNPROCESSABLE_ENTITY;
    /**
     * The {@link HttpStatus#value()}
     */
    private final int status;
    /**
     * The {@link HttpStatus#getReasonPhrase()}
     */
    private final String error;
    /**
     * Error message statically set to 'Validation Error'
     */
    private final String message = "Validation Error";
    /**
     * Contains the validation error messages of all fields
     */
    private final List<String> fieldErrors;
    /**
     * The path of the endpoint
     */
    private final String path;

    public ValidationErrorResponseDTO(List<FieldError> fieldErrorList) {
        this.fieldErrors = fieldErrorList.stream()
            .map(fieldError -> "Field '" + fieldError.getField() + "' " + fieldError.getDefaultMessage())
            .collect(Collectors.toList());

        this.status = HTTP_STATUS.value();
        this.error = HTTP_STATUS.getReasonPhrase();

        HttpServletRequest currentRequest =
            ((ServletRequestAttributes) RequestContextHolder.
                currentRequestAttributes()).getRequest();

        if (currentRequest != null) {
            this.path = currentRequest.getPathInfo();
        } else {
            this.path = null;
        }
    }
}
