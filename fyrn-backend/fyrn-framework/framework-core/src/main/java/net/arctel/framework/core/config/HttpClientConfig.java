package net.arctel.framework.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .version(HttpClient.Version.HTTP_1_1)
                // 如需对齐 curl/postman 行为，可临时打开重定向跟随验证：
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}