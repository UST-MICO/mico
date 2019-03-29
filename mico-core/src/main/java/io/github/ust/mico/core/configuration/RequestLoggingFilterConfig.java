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

package io.github.ust.mico.core.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

import javax.servlet.Filter;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

@Configuration
public class RequestLoggingFilterConfig {

    private final static List<String> excludedEndpoints = Collections.singletonList("/actuator/**");

    @Bean
    public Filter logFilter() {
        MicoRequestLoggingFilter filter = new MicoRequestLoggingFilter();
        filter.setIncludeQueryString(true);
        filter.setIncludePayload(false);
        filter.setMaxPayloadLength(1000);
        filter.setIncludeHeaders(false);
        filter.setBeforeMessagePrefix("BEFORE REQUEST [");
        filter.setAfterMessagePrefix("AFTER REQUEST [");
        return filter;
    }

    public static class MicoRequestLoggingFilter extends CommonsRequestLoggingFilter {

        @Override
        protected boolean shouldNotFilter(HttpServletRequest request) {
            return excludedEndpoints.stream().anyMatch(e -> new AntPathMatcher().match(e, request.getServletPath()));
        }
    }
}
