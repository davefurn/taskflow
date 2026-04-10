package com.taskflow.api.controller;

import com.taskflow.api.dto.request.auth.SetupRequest;
import com.taskflow.api.dto.response.ApiResponse;
import com.taskflow.api.service.SetupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/setup")
@RequiredArgsConstructor
@Tag(name = "Setup", description = "First-launch only - disabled after first admin is created")
public class SetupController {

    private final SetupService setupService;

    @PostMapping("/init")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create first admin account",
            description = "One-time endpoint. Returns 409 if setup already completed."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "Admin account created"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Setup already completed"
            )
    })
    public ApiResponse init(@Valid @RequestBody SetupRequest request) {
        setupService.initialSetup(request);
        return ApiResponse.of("Admin account created. Check your email to verify.");
    }
}