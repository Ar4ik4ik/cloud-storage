package com.github.ar4ik4ik.cloudstorage;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class CloudStorageApplicationTests {

	@Test
	@DisplayName("Контекст и конфиги успешно загружены")
	void contextLoads() {
	}
}
