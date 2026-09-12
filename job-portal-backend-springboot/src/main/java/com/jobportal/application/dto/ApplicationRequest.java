package com.jobportal.application.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationRequest(

        @NotNull(message = "jobId is required")
        Long jobId,

        @Size(max = 5000, message = "coverLetter cannot exceed 5000 characters")
        String coverLetter
) {
}