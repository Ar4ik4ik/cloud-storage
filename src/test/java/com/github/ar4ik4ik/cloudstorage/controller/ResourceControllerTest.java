package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.TestcontainersConfiguration;
import com.github.ar4ik4ik.cloudstorage.dao.impl.MinioDaoImpl;
import com.github.ar4ik4ik.cloudstorage.service.impl.DirectoryDownloadStrategy;
import com.github.ar4ik4ik.cloudstorage.service.impl.FileDownloadStrategy;
import com.github.ar4ik4ik.cloudstorage.service.impl.StorageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


//@WebMvcTest(ResourceController.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles("test")
@Import(TestcontainersConfiguration.class)
@AutoConfigureMockMvc(addFilters = false)
class ResourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MinioDaoImpl dao;

    @Autowired
    private StorageServiceImpl service;

    @MockitoBean
    private DirectoryDownloadStrategy directoryDownloadStrategy;

    @MockitoBean
    private FileDownloadStrategy fileDownloadStrategy;

    private static final String TEST_FILE_PATH = "somefile.txt";
    private static final String TEST_DIRECTORY_PATH = "somedirectory/";

    @Test
    @DisplayName("Успешное скачивание существующего файла")
    void downloadResource_WhenFileExists_ReturnFileContentAndHeaders() throws Exception {
        String fileContent = "Some file content...";

        StreamingResponseBody mockResponseBody = outputStream -> {
            outputStream.write(fileContent.getBytes());
            outputStream.flush();
        };

        when(dao.isObjectExists(TEST_FILE_PATH)).thenReturn(true);
        when(fileDownloadStrategy.download(TEST_FILE_PATH))
                .thenReturn(mockResponseBody);

        mockMvc.perform(get("/api/resource/download")
                .queryParam("path", TEST_FILE_PATH))
                .andExpect(status().isOk())
                .andExpect(header()
                        .string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + TEST_FILE_PATH))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(fileContent.getBytes()));

        verify(dao, times(1)).isObjectExists(TEST_FILE_PATH);
        verify(fileDownloadStrategy, times(1)).download(TEST_FILE_PATH);
        verifyNoMoreInteractions(fileDownloadStrategy);
    }

    @Test
    @DisplayName("Успешное скачивание существующей папки")
    void downloadResource_WhenFolderExists_ReturnFileContentAndHeaders() throws Exception {
        String fileContent = "Some file content...";

        StreamingResponseBody mockResponseBody = outputStream -> {
            outputStream.write(fileContent.getBytes());
            outputStream.flush();
        };

        when(dao.isObjectExists(TEST_DIRECTORY_PATH)).thenReturn(true);
        when(directoryDownloadStrategy.download(TEST_DIRECTORY_PATH))
                .thenReturn(mockResponseBody);

        mockMvc.perform(get("/api/resource/download")
                        .queryParam("path", TEST_DIRECTORY_PATH))
                .andExpect(status().isOk())
                .andExpect(header()
                        .string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + TEST_DIRECTORY_PATH.substring(0, TEST_DIRECTORY_PATH.length() - 1) + ".zip"))
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM))
                .andExpect(content().bytes(fileContent.getBytes()));

        verify(dao, times(1)).isObjectExists(TEST_DIRECTORY_PATH);
        verify(directoryDownloadStrategy, times(1)).download(TEST_DIRECTORY_PATH);
        verifyNoMoreInteractions(directoryDownloadStrategy);
    }

    @Test
    @DisplayName("Ошибка при скачивании несуществующего ресурса")
    void downloadResource_WhenResourceMissing_ThrowsObjectNotFoundException() throws Exception {
        String fileContent = "Some file content...";

        StreamingResponseBody mockResponseBody = outputStream -> {
            outputStream.write(fileContent.getBytes());
            outputStream.flush();
        };

        when(dao.isObjectExists(TEST_DIRECTORY_PATH)).thenReturn(false);
        when(directoryDownloadStrategy.download(TEST_DIRECTORY_PATH))
                .thenReturn(mockResponseBody);

        mockMvc.perform(get("/api/resource/download")
                        .queryParam("path", TEST_DIRECTORY_PATH))
                .andExpect(status().isNotFound());

        verify(dao, times(1)).isObjectExists(TEST_DIRECTORY_PATH);
        verifyNoInteractions(directoryDownloadStrategy);
    }

}