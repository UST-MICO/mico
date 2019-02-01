package io.github.ust.mico.core;

import java.util.Arrays;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@Slf4j
public class WebConfig extends WebMvcConfigurerAdapter {

    @Autowired
    CorsConfig corsConfig;

    /**
     * Based on https://github.com/springfox/springfox/issues/2215#issuecomment-446178059
     * @return
     */
    @Bean
    // TODO: Check generic type arguments.
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.addAllowedMethod(HttpMethod.DELETE);
        config.addAllowedMethod(HttpMethod.PUT);
        for (String additionalAllowedMethod : corsConfig.getAdditionalAllowedMethods()) {
            log.info("Adding additional method to CORS config:"+ additionalAllowedMethod);
            config.addAllowedMethod(additionalAllowedMethod);
        }
        config.setAllowCredentials(false);
        List<String> allowedOrigins = corsConfig.getAllowedOrigins();
        allowedOrigins.forEach(allowedOrigin -> log.info("Adding additional allowed origin:"+allowedOrigin));
        config.setAllowedOrigins(allowedOrigins);
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);

        return bean;
    }
}
