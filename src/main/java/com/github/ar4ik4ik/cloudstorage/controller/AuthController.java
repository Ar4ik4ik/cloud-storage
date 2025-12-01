package com.github.ar4ik4ik.cloudstorage.controller;

import com.github.ar4ik4ik.cloudstorage.model.dto.AuthResponseDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.MessageDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignInRequestDto;
import com.github.ar4ik4ik.cloudstorage.model.dto.SignUpRequestDto;
import com.github.ar4ik4ik.cloudstorage.service.AuthenticationService;
import com.github.ar4ik4ik.cloudstorage.service.LoginService;
import com.github.ar4ik4ik.cloudstorage.service.RegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Аутентификация", description = "API для регистрации, входа и выхода пользователей из системы")
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final AuthenticationService authenticationService;

    @Operation(
            summary = "Вход пользователя",
            description = "Аутентифицирует пользователя по имени пользователя и паролю, возвращает токен аутентификации.",
            requestBody = @RequestBody(
                    description = "Данные для входа пользователя",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignInRequestDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Успешная аутентификация",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "401", description = "Неверные учетные данные (пользователь не найден или пароль не совпадает)",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @PostMapping(path = "/sign-in")
    public ResponseEntity<AuthResponseDto> signIn(@Valid @org.springframework.web.bind.annotation.RequestBody SignInRequestDto requestDto) {
        loginService.processLogin(requestDto);
        return ResponseEntity.ok(new AuthResponseDto(requestDto.username()));
    }

    @Operation(
            summary = "Регистрация нового пользователя",
            description = "Регистрирует нового пользователя в системе. Возвращает токен аутентификации.",
            requestBody = @RequestBody(
                    description = "Данные для регистрации нового пользователя",
                    required = true,
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = SignUpRequestDto.class))
            ),
            responses = {
                    @ApiResponse(responseCode = "201", description = "Пользователь успешно зарегистрирован",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = AuthResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "Неверный формат запроса, ошибка валидации",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "409", description = "Пользователь с таким именем/email уже существует",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @PostMapping(path = "/sign-up")
    public ResponseEntity<AuthResponseDto> signUp(@Valid @org.springframework.web.bind.annotation.RequestBody SignUpRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        registrationService.registerUser(requestDto);
        authenticationService.authenticateDirectly(
                new SignInRequestDto(requestDto.username(), requestDto.password()), request, response
        );
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(new AuthResponseDto(requestDto.username()));
    }

    @Operation(
            summary = "Выход пользователя",
            description = "Выход текущего авторизованного пользователя из системы. Требует наличия JWT токена.",

            security = @SecurityRequirement(name = "cookieAuth"),
            responses = {
                    @ApiResponse(responseCode = "204", description = "Успешный выход (контент отсутствует)"),
                    @ApiResponse(responseCode = "401", description = "Неавторизованный доступ (токен отсутствует или недействителен)",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class))),
                    @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера",
                            content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                                    schema = @Schema(implementation = MessageDto.class)))
            }
    )
    @PostMapping(path = "/sign-out")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            new SecurityContextLogoutHandler().logout(request, response, auth);
        }
        return ResponseEntity.noContent().build();
    }
}
