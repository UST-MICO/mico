package io.github.ust.mico.core.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.stereotype.Component;

import javax.validation.constraints.NotBlank;

@Component
@Configuration
@EnableRedisRepositories
@Getter
@Setter
@ConfigurationProperties(prefix = "mico-core.redis")
public class RedisConfiguration {
    @NotBlank
    private String host;
    @NotBlank
    private int port;

    @Bean
    LettuceConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }
}
