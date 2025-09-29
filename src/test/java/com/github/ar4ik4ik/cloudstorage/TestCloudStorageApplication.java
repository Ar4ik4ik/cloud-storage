package com.github.ar4ik4ik.cloudstorage;

import org.springframework.boot.SpringApplication;

public class TestCloudStorageApplication {

	public static void main(String[] args) {
		SpringApplication.from(CloudStorageApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
