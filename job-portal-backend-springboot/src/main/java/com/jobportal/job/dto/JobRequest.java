package com.jobportal.job.dto;

import com.jobportal.job.entity.EmploymentType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

public record JobRequest(
        @NotBlank(message = "title is required") String title,

        @NotBlank(message = "description is required") String description,

        @NotBlank(message = "location is required") String location,

        @NotNull(message = "employmentType is required") EmploymentType employmentType,

        @NotNull(message = "experienceMin is required")
        @Min(value = 0, message = "experienceMin cannot be negative") Integer experienceMin,

        @NotNull(message = "experienceMax is required")
        @Min(value = 0, message = "experienceMax cannot be negative") Integer experienceMax,

        @NotNull(message = "salaryMin is required")
        @Min(value = 0, message = "salaryMin cannot be negative") Integer salaryMin,

        @NotNull(message = "salaryMax is required")
        @Min(value = 0, message = "salaryMax cannot be negative") Integer salaryMax,

        String skills, // comma-separated, optional

        LocalDateTime applicationDeadline // optional
) {}
