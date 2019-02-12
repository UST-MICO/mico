package io.github.ust.mico.core.configuration;

import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    CorsConfig corsUserConfig;

    /**
     * Based on https://github.com/springfox/springfox/issues/2215#issuecomment-446178059
     * @return
     */
    @Bean
    // TODO: Check generic type arguments.
    public FilterRegistrationBean<CorsFilter> corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfiguration = new CorsConfiguration().applyPermitDefaultValues(); // Allows GET, HEAD, and POST
        corsConfiguration.addAllowedMethod(HttpMethod.DELETE);
        corsConfiguration.addAllowedMethod(HttpMethod.PUT);
        for (String additionalAllowedMethod : corsUserConfig.getAdditionalAllowedMethods()) {
            log.info("Adding additional method to CORS config:"+ additionalAllowedMethod);
            corsConfiguration.addAllowedMethod(additionalAllowedMethod);
        }
        corsConfiguration.setAllowCredentials(false);
        List<String> allowedOrigins = corsUserConfig.getAllowedOrigins();
        allowedOrigins.forEach(allowedOrigin -> log.info("Adding additional allowed origin:"+allowedOrigin));
        corsConfiguration.setAllowedOrigins(allowedOrigins);
        source.registerCorsConfiguration("/**", corsConfiguration);

        FilterRegistrationBean<CorsFilter> bean = new FilterRegistrationBean<>(new CorsFilter(source));
        bean.setOrder(0);

        return bean;
    }
}
