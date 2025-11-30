package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.dto.ResourcePathRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.StorageUserDetails;
import com.github.ar4ik4ik.cloudstorage.model.dto.MessageDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.ResourceInfoResponseDto;
import com.github.ar4ik4ik.cloudstorage.service.impl.StorageServiceImpl;
import com.github.ar4ik4ik.cloudstorage.utils.PathUtils;
import com.github.ar4ik4ik.cloudstorage.validation.ValidFiles;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

import static com.github.ar4ik4ik.cloudstorage.utils.PathUtils.getParentPath;

@RequestMapping("/api/resource")
@RestController
@RequiredArgsConstructor
@Validated
@Tag(name = "Управление ресурсами", description = "Операции с файлами и папками в облачном хранилище")
@SecurityRequirement(name = "cookieAuth")
public class ResourceController {

    private final StorageServiceImpl service;

    @Operation(
            summary = "Получение информации о ресурсе",
            description = "Возвращает метаданные (путь, имя, размер, тип) файла или папки. " +
                    "Путь к папке должен заканчиваться на `/` для корректного различения.",
            parameters = {
                    @Parameter(name = "path", description = "Полный путь к ресурсу, URL-encoded. Путь к папке должен заканчиваться на '/'.",
                            required = true, example = "folder1/file.txt", schema = @Schema(implementation = String.class))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешное получение информации о ресурсе",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResourceInfoResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @GetMapping
    public ResponseEntity<ResourceInfoResponseDto> getResourceInfo(
            @RequestParam(name = "path") @Valid ResourcePathRequestDto path) {
        return ResponseEntity.ok(service.getResourceInfo(path.path()));
    }

    @Operation(
            summary = "Удаление ресурса",
            description = "Удаляет файл или пустую папку по указанному пути. Непустые папки не могут быть удалены напрямую.",
            parameters = {
                    @Parameter(name = "path", description = "Полный путь к ресурсу, URL-encoded. Путь к папке должен заканчиваться на '/'.",
                            required = true, example = "folder1/file.txt", schema = @Schema(implementation = String.class))
            },
            responses = {
                    @ApiResponse(responseCode = "204", description = "Ресурс успешно удален (без содержимого)"),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @DeleteMapping
    public ResponseEntity<?> deleteResource(@RequestParam(name = "path") @Valid ResourcePathRequestDto path) {
        service.deleteResource(path.path());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Скачивание ресурса",
            description = "Скачивает файл или папку. Папка архивируется в ZIP перед скачиванием.",
            parameters = {
                    @Parameter(name = "path", description = "Полный путь к ресурсу, URL-encoded. Путь к папке должен заканчиваться на '/'.",
                            required = true, example = "folder1/file.txt", schema = @Schema(implementation = String.class))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешное скачивание ресурса",
                            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM_VALUE)),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "404", description = "Ресурс не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @GetMapping(path = "download")
    public ResponseEntity<StreamingResponseBody> downloadResource(
            @RequestParam(name = "path") @Valid ResourcePathRequestDto path) {
        String filename = PathUtils.getFilenameForDownload(path.path());
        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''%s"
                        .formatted(filename))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(service.downloadResource(path.path()));
    }

    @Operation(
            summary = "Перемещение/переименование ресурса",
            description = "Перемещает файл или папку из одного места в другое. Если 'to' совпадает с родительской папкой 'from', происходит переименование.",
            parameters = {
                    @Parameter(name = "from", description = "Полный путь к исходному ресурсу, URL-encoded.",
                            required = true, example = "folder1/old_name.txt", schema = @Schema(implementation = String.class)),
                    @Parameter(name = "to", description = "Полный путь к целевому месту/имени ресурса, URL-encoded. Путь к папке должен заканчиваться на '/'.",
                            required = true, example = "new_folder/new_name.txt", schema = @Schema(implementation = String.class))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Ресурс успешно перемещен/переименован",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResourceInfoResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий путь",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "404", description = "Исходный ресурс не найден",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "409", description = "Ресурс, лежащий по пути 'to', уже существует",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @GetMapping(path = "move")
    public ResponseEntity<ResourceInfoResponseDto> moveResource(
            @RequestParam(name = "from") @Valid ResourcePathRequestDto sourcePath,
            @RequestParam(name = "to") @Valid ResourcePathRequestDto targetPath) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(service.moveResource(sourcePath.path(), targetPath.path()));
    }

    @Operation(
            summary = "Поиск ресурсов",
            description = "Ищет файлы и папки по части имени или полному совпадению в пределах директории пользователя.",
            parameters = {
                    @Parameter(name = "query", description = "Поисковый запрос (URL-encoded). Может быть частью имени файла/папки.",
                            required = true, example = "document", schema = @Schema(implementation = String.class))
            },
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешное получение списка найденных ресурсов",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResourceInfoResponseDto[].class))),
                    @ApiResponse(responseCode = "400", description = "Невалидный или отсутствующий поисковый запрос",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @GetMapping(path = "search")
    public ResponseEntity<?> searchResource(
            @RequestParam(name = "query") @Valid ResourcePathRequestDto searchQuery,
            @AuthenticationPrincipal StorageUserDetails userDetails) {
        return ResponseEntity.ok(service.searchResourcesByQuery(
                searchQuery.path(), userDetails.getUserRootDirectory()));
    }

    @Operation(
            summary = "Загрузка ресурсов",
            description = "Загружает один или несколько файлов в указанную папку. " +
                    "Если в имени файла из тела запроса присутствует поддиректория, она будет создана.",
            parameters = {
                    @Parameter(name = "path", description = "Путь к папке в облачном хранилище, куда будут загружены файлы. " +
                            "Должен заканчиваться на '/'.",
                            required = true, example = "uploads/", schema = @Schema(implementation = String.class))
            },
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Один или несколько файлов для загрузки",
                    required = true,
                    content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(type = "string", format = "binary"))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Ресурсы успешно загружены",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = ResourceInfoResponseDto[].class))),
                    @ApiResponse(responseCode = "400", description = "Невалидный путь, невалидное тело запроса или ошибка валидации файлов",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "409", description = "Файл с таким именем уже существует по указанному пути",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @PostMapping
    public ResponseEntity<List<ResourceInfoResponseDto>> uploadResource(
            @RequestParam(name = "path") @Valid ResourcePathRequestDto path,
            @RequestParam(name = "object") @ValidFiles MultipartFile[] files) {
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequestUri()
                .path("/api/resource")
                .queryParam("path", getParentPath(path.path(), true))
                .build().toUri();

        return ResponseEntity.created(location)
                .body(service.uploadResource(files, path.path()));
    }
}
