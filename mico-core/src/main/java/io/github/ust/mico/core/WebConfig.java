package io.github.ust.mico.core;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class WebConfig {

    @Value("${cors-policy.allowed-origins}")
    String[] allowedOrigins;

    /**
     * Based on https://github.com/springfox/springfox/issues/2215#issuecomment-446178059
     * @return
     */
    @Bean
    // TODO: Check generic type arguments.
    public FilterRegistrationBean corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration().applyPermitDefaultValues();
        config.setAllowCredentials(false);
        config.setAllowedOrigins(Arrays.asList(allowedOrigins));
        source.registerCorsConfiguration("/**", config);

        FilterRegistrationBean bean = new FilterRegistrationBean(new CorsFilter(source));
        bean.setOrder(0);

        return bean;
    }
}
