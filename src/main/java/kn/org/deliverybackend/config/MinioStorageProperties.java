package kn.org.deliverybackend.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "minio.storage")
@Getter
@Setter
public class MinioStorageProperties {

    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String bucketName;

    private String region;

    private Long maxFileSize;

    private List<String> allowedExtensions;
}
