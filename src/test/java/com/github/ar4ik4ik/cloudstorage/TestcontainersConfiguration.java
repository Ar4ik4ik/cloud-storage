package com.github.ar4ik4ik.cloudstorage;

import com.redis.testcontainers.RedisContainer;
import io.minio.MinioClient;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

	@Bean
	@ServiceConnection
	PostgreSQLContainer<?> postgresContainer() {
		return new PostgreSQLContainer<>(DockerImageName.parse("postgres:18"));
	}

	@Bean
	@ServiceConnection(name = "redis")
	RedisContainer redisContainer() {
		return new RedisContainer(DockerImageName.parse("redis:8.2.1")).withExposedPorts(6379);
	}

	@Bean
	MinIOContainer minioContainer() {
		return new MinIOContainer(DockerImageName.parse("minio/minio:latest"))
//				.withExposedPorts(9000, 9090)
				.withEnv("MINIO_ROOT_USER", "minioadmin")
				.withEnv("MINIO_ROOT_PASSWORD", "minioadmin")
				.withCommand("server /data")
				.withReuse(true);
	}

	@Bean
	MinioClient minioTestClient(MinIOContainer minioContainer) {
		return MinioClient.builder()
				.endpoint(minioContainer.getS3URL())
				.credentials(minioContainer.getUserName(), minioContainer.getPassword())
				.build();
	}

	@Bean
	public DynamicPropertyRegistrar apiPropertiesRegistrar(MinIOContainer minioContainer) {

		return registry -> {
			registry.add("minio.url", minioContainer::getS3URL);
			registry.add("minio.accessKey", minioContainer::getUserName);
			registry.add("minio.secretKey", minioContainer::getPassword);
			registry.add("minio.bucket", () -> "user-files-test");
		};
	}
}
