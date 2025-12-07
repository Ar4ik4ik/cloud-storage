package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.dto.AuthResponseDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.MessageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@Tag(name = "Информация о пользователе", description = "API для получения информации о текущем аутентифицированном пользователе")
@SecurityRequirement(name = "cookieAuth")
public class UserController {

    @Operation(
            summary = "Получить информацию о текущем пользователе",
            description = "Возвращает информацию о текущем аутентифицированном пользователе, включая его имя.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Информация о пользователе успешно получена",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDto> getUser(Authentication authentication) {
        return ResponseEntity.ok((new AuthResponseDto(authentication.getName())));
    }
}
