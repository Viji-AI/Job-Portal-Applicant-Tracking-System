package com.jobportal.job.dto;

import com.jobportal.job.entity.JobStatus;
import jakarta.validation.constraints.NotNull;

public record JobStatusUpdateRequest(
        @NotNull(message = "status is required") JobStatus status
) {}
