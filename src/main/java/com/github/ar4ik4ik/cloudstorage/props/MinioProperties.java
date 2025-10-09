package com.github.ar4ik4ik.cloudstorage.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    private String bucket;
    private String url;
    private String accessKey;
    private String secretKey;
}
