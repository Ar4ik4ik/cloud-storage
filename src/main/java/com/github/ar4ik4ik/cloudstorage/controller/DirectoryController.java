package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.ResourcePath;
import com.github.ar4ik4ik.cloudstorage.model.dto.MessageDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/directory")
@Validated
@Slf4j
@Tag(name = "Управление директориями", description = "API для просмотра и создания папок в облачном хранилище")
@SecurityRequirement(name = "cookieAuth")
public class DirectoryController {

    private final StorageService service;

    @Operation(
            summary = "Получение информации о содержимом папки",
            description = "Возвращает список ресурсов (файлов и папок), находящихся непосредственно в указанной папке (без рекурсии).",
            parameters = {
                    @Parameter(name = "path", description = "Полный путь к папке, URL-encoded. Обязательно должен заканчиваться на '/'.",
                            required = true, example = "my_folder/", schema = @Schema(implementation = String.class),
                            allowEmptyValue = true)
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешное получение содержимого папки",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResourceInfoResponseDto[].class))),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "404", description = "Папка не существует",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @GetMapping
    public ResponseEntity<List<ResourceInfoResponseDto>> getDirectoryInfo(
            @RequestParam(name = "path") ResourcePath path) {
        log.info("DirectoryController.getDirectoryInfo called. Resolved path: '{}'", path.path());
        return ResponseEntity.ok(service.getDirectoryInfo(path.path()));
    }

    @Operation(
            summary = "Создание новой папки",
            description = "Создает пустую папку по указанному пути. Родительская папка должна существовать.",
            parameters = {
                    @Parameter(name = "path", description = "Полный путь к новой папке, URL-encoded. Обязательно должен заканчиваться на '/'.",
                            required = true, example = "my_folder/new_subfolder/", schema = @Schema(implementation = String.class))
            },
            responses = {
                    @ApiResponse(responseCode = "201", description = "Папка успешно создана",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResourceInfoResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь к новой папке",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "404", description = "Родительская папка не существует",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "409", description = "Папка с таким именем уже существует по указанному пути",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @PostMapping
    public ResponseEntity<ResourceInfoResponseDto> createDirectory(
            @RequestParam(name = "path") @Valid ResourcePath path) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/api/directory")
                .queryParam("path", path.path())
                .build().toUri();
        return ResponseEntity.created(location)
                .body(service.createDirectory(path.path()));
    }
}
