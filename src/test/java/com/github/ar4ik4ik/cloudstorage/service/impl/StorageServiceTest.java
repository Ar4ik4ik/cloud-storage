package com.github.ar4ik4ik.cloudstorage.service.impl;

import com.github.ar4ik4ik.cloudstorage.TestcontainersConfiguration;
import com.github.ar4ik4ik.cloudstorage.dao.impl.MinioDaoImpl;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectAlreadyExistException;
import com.github.ar4ik4ik.cloudstorage.exception.ObjectNotFoundException;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ActiveProfiles("test")
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class StorageServiceTest {

    @Autowired
    private StorageServiceImpl storageService;

    @Autowired
    private MinioDaoImpl s3Dao;
    private String baseUserUploadPath;

    @BeforeEach
    void setUp() throws Exception {
        int i = Random.from(RandomGenerator.getDefault()).nextInt(55, 1000000);
        baseUserUploadPath = String.format("user-%s-files/", i);

        storageService.createRootDirectoryForUser(i);
        String testFolderResourcePath = "test_upload_folder";
        String formFieldName = "files";
        List<MultipartFile> filesToUpload = createMockMultipartFilesFromFolder(testFolderResourcePath, formFieldName);

        storageService.uploadResource(
                filesToUpload.toArray(new MultipartFile[0]),
                baseUserUploadPath);

        log.info("--- Setup: MinIO initialized with data into {} ---", baseUserUploadPath);
    }

    @Test
    @DisplayName("Проверка корректной установки данных окружения")
    void setUp_Success() {
        // given
        List<ResourceInfoResponseDto> directoryInfo = storageService.getDirectoryInfo(baseUserUploadPath);
        Set<String> expectedNames = Set.of(
                ".obsidian/", "Java/", "10.10.25 Фин.статусы.md", "Без названия.md", "ТЕСТЫ.md"
        );
        List<ResourceInfoResponseDto> javaDirectoryInfo = storageService.getDirectoryInfo(baseUserUploadPath + "Java/");

        // then
        assertThat(directoryInfo.stream().map(ResourceInfoResponseDto::name)
                .collect(Collectors.toSet()))
                .containsExactlyInAnyOrderElementsOf(expectedNames);

        assertThat(javaDirectoryInfo.stream().map(ResourceInfoResponseDto::name)
                .collect(Collectors.toSet()))
                .containsExactlyInAnyOrder("Spring/", "Шорткаты IDEA.md");
    }

    @Test
    @DisplayName("Базовая проверка загрузки файла")
    void uploadResource_UploadSimpleTxtFile_Success() {
        // given
        String newFileName = "testDocument.txt";
        String newFilePath = baseUserUploadPath + newFileName;

        MockMultipartFile newFile = new MockMultipartFile(
                "files",
                newFileName,
                "text/plain",
                "New test file".getBytes(StandardCharsets.UTF_8)
        );

        // when
        storageService.uploadResource(new MultipartFile[]{newFile}, baseUserUploadPath);
        ResourceInfoResponseDto uploadedFileInfo = storageService.getResourceInfo(newFilePath);
        List<ResourceInfoResponseDto> directoryInfo = storageService.getDirectoryInfo(baseUserUploadPath);

        // then
        assertThat(uploadedFileInfo.name()).isEqualTo(newFileName);
        assertThat(directoryInfo.stream().map(ResourceInfoResponseDto::name)).contains(newFileName);
    }

    @Test
    @DisplayName("Успешное удаление существующего файла")
    void deleteResource_ExistFile_Success() {
        //given
        String fileToDelete = baseUserUploadPath + "ТЕСТЫ.md";
        Assertions.assertTrue(s3Dao.isObjectExists(fileToDelete));

        // when
        storageService.deleteResource(fileToDelete);

        // then
        assertThat(s3Dao.isObjectExists(fileToDelete)).isFalse();
    }

    @Test
    @DisplayName("Успешное удаление существующей директории")
    void deleteResource_ExistDirectory_Success() {
        // given
        String fileToDelete = baseUserUploadPath + "Java/Spring/Boot/";
        Assertions.assertTrue(s3Dao.isObjectExists(fileToDelete));

        // when
        storageService.deleteResource(fileToDelete);

        // then
        assertThat(s3Dao.isObjectExists(fileToDelete)).isFalse();
    }

    @Test
    @DisplayName("Ошибка при удалении несуществующего ресурса")
    void deleteResource_MissingResource_ThrowObjectNotFoundException() {
        // given
        String fileToDelete = baseUserUploadPath + "SomeFileThatNotExists.test";

        // then
        assertThatThrownBy(() -> storageService.deleteResource(fileToDelete))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    @DisplayName("Ошибка при получении данных по несуществующей директории")
    void getDirectoryInfo_MissingDirectory_ThrowObjectNotFoundException() {
        // given
        String directory = baseUserUploadPath + "notExistentDirectory";

        // then
        assertThatThrownBy(() -> storageService.getDirectoryInfo(directory))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    @DisplayName("Успешное создание пустой папки у которой существует родительская папка")
    void createDirectory_ParentDirectoryExist_Success() {
        // given
        String directory = baseUserUploadPath + "Java/NewDirectoryFromTest/";

        // when
        storageService.createDirectory(directory);

        // then
        assertThat(s3Dao.isObjectExists(directory)).isTrue();
    }

    @Test
    @DisplayName("Ошибка при создании пустой папки у которой отсутствует родительская папка")
    void createDirectory_ParentDirectoryMissing_ThrowsObjectNotFoundException() {
        // given
        String directory = baseUserUploadPath + "MissingDirectory/NewTestDirectory/";

        // then
        assertThatThrownBy(() -> storageService.createDirectory(directory))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    @DisplayName("Успешный поиск файла по части названия игнорируя регистр")
    void searchResourcesByQuery_ExistFileInDirectoriesNotMatchingCase_ReturnsResource() {
        // given
        String existingResource = "Proxy.md";
        String searchQuery = "PROX";

        // when
        List<ResourceInfoResponseDto> searchQueryResult = storageService.searchResourcesByQuery(
                searchQuery, baseUserUploadPath
        );

        // then
        assertThat(searchQueryResult.stream().map(ResourceInfoResponseDto::name).toList())
                .contains(existingResource);
    }

    @Test
    @DisplayName("Успешный поиск папки по части названия игнорируя регистр")
    void searchResourcesByQuery_ExistDirectoryInDirectoriesNotMatchingCase_ReturnsDirectory() {
        // given
        String existingResource = "Security/";
        String searchQuery = "cuRI";

        // when
        List<ResourceInfoResponseDto> searchQueryResult = storageService.searchResourcesByQuery(
                searchQuery, baseUserUploadPath
        );

        // then
        assertThat(searchQueryResult.stream().map(ResourceInfoResponseDto::name).toList())
                .contains(existingResource);
    }

    @Test
    @DisplayName("Ошибка при перемещении несуществующего ресурса")
    void moveResource_MissingResource_ThrowsObjectNotFoundException() {
        // given
        String missingResource = baseUserUploadPath + "MissingDirectory/NewTestDirectory/";
        String targetLocation = baseUserUploadPath + "Java/NewTestDirectory/";
        // then
        assertThatThrownBy(() -> storageService.moveResource(missingResource, targetLocation))
                .isInstanceOf(ObjectNotFoundException.class);
    }

    @Test
    @DisplayName("Ошибка при перемещении существующего ресурса в папку, где ресурс с таким именем уже есть")
    void moveResource_TargetFileInDirectoryAlreadyExists_ThrowsObjectAlreadyExistsException() {
        // given
        String existsResource = baseUserUploadPath + "Java/Шорткаты IDEA.md";
        String targetLocation = baseUserUploadPath + "Java/Spring/Boot/Шорткаты IDEA.md";

        // then
        assertThatThrownBy(() -> storageService.moveResource(existsResource, targetLocation))
                .isInstanceOf(ObjectAlreadyExistException.class);
    }

    @Test
    @DisplayName("Успешное перемещение существующего файла")
    void moveResource_FileExists_Success() {
        // given
        String existsResource = baseUserUploadPath + "Java/Шорткаты IDEA.md";
        String targetLocation = baseUserUploadPath + "Java/Spring/Шорткаты IDEA.md";

        // when
        ResourceInfoResponseDto responseDtoBeforeFileMoving = storageService.moveResource(existsResource, targetLocation);

        // then
        assertThat(s3Dao.isObjectExists(targetLocation)).isTrue();
        ResourceInfoResponseDto responseDtoAfterFileMoving = storageService.getResourceInfo(targetLocation);
        assertThat(responseDtoBeforeFileMoving)
                .usingRecursiveComparison().ignoringFields("path")
                .isEqualTo(responseDtoAfterFileMoving);
    }

    @Test
    @DisplayName("Успешное перемещение существующей папки")
    void moveResource_DirectoryExists_Success() {
        // given
        String existsResource = baseUserUploadPath + ".obsidian/";
        String targetLocation = baseUserUploadPath + "NewDirectory/.obsidian/";

        // when
        ResourceInfoResponseDto responseDtoBeforeFileMoving = storageService.moveResource(existsResource, targetLocation);

        // then
        assertThat(s3Dao.isObjectExists(targetLocation)).isTrue();
        ResourceInfoResponseDto responseDtoAfterFileMoving = storageService.getResourceInfo(targetLocation);
        assertThat(responseDtoBeforeFileMoving)
                .usingRecursiveComparison().ignoringFields("path")
                .isEqualTo(responseDtoAfterFileMoving);
    }

    @Test
    @DisplayName("Успешное переименование существующей папки")
    void moveResource_DirectoryExistsAndShouldHaveNewName_Success() {
        // given
        String newName = "NewCore/";
        String existsResource = baseUserUploadPath + "Java/Spring/Core/";
        String targetLocation = baseUserUploadPath + "Java/Spring/" + newName;

        // when
        ResourceInfoResponseDto responseDtoBeforeFileMoving = storageService.moveResource(existsResource, targetLocation);

        // then
        assertThat(s3Dao.isObjectExists(targetLocation)).isTrue();
        ResourceInfoResponseDto responseDtoAfterFileMoving = storageService.getResourceInfo(targetLocation);
        assertThat(responseDtoBeforeFileMoving)
                .usingRecursiveComparison().ignoringFields("path", "name")
                .isEqualTo(responseDtoAfterFileMoving);
        assertThat(responseDtoAfterFileMoving.name()).isEqualTo(newName);
    }

    private List<MultipartFile> createMockMultipartFilesFromFolder(String folderResourcePath, String formFieldName) throws IOException {
        Path rootFolderPath = new ClassPathResource(folderResourcePath).getFile().toPath();
        List<MultipartFile> mockFiles = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(rootFolderPath)) {
            paths.filter(Files::isRegularFile)
                    .forEach(filePath -> {
                        try {
                            String relativePath = rootFolderPath.relativize(filePath).toString();
                            String contentType = Files.probeContentType(filePath);
                            if (contentType == null) {
                                contentType = "application/octet-stream";
                            }
                            byte[] content = Files.readAllBytes(filePath);
                            mockFiles.add(new MockMultipartFile(
                                    formFieldName,
                                    relativePath,
                                    contentType,
                                    content
                            ));
                        } catch (IOException e) {
                            throw new RuntimeException("Failed to create mock file for " + filePath, e);
                        }
                    });
        }
        return mockFiles;
    }
}
