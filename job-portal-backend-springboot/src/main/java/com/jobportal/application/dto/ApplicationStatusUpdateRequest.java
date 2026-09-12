package com.jobportal.application.dto;

import com.jobportal.application.entity.ApplicationStatus;
import jakarta.validation.constraints.NotNull;

public record ApplicationStatusUpdateRequest(

        @NotNull(message = "status is required")
        ApplicationStatus status

) {
}