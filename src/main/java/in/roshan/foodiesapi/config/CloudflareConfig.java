package in.roshan.foodiesapi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration
public class CloudflareConfig {
    @Value("${cloudflare.access.key}")
    private String accessKey;
    @Value("${cloudflare.secret.key}")
    private String secretKey;
    @Value("${cloudflare.endpoint}")
    private String endpoint;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(java.net.URI.create(endpoint))
                .region(Region.US_EAST_1) // R2 uses us-east-1 for all regions
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }
}
