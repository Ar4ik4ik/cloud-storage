package com.github.ar4ik4ik.cloudstorage.configuration;

import com.github.ar4ik4ik.cloudstorage.props.MinioProperties;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.DefaultCookieSerializer;

@Configuration
@RequiredArgsConstructor (onConstructor = @__(@Lazy))
public class ApplicationConfig {

    private final MinioProperties minioProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .endpoint(minioProperties.getUrl())
                .build();
    }

    @Bean
    public CookieSerializer cookieSerializer() {
        DefaultCookieSerializer serializer = new DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setCookiePath("/");
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(false);
        serializer.setSameSite("Lax");
        return serializer;
    }
}
