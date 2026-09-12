package com.jobportal.application.dto;

import com.jobportal.application.entity.Application;
import com.jobportal.application.entity.ApplicationStatus;
import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long jobId,
        String jobTitle,
        Long applicantId,
        String applicantName,
        String applicantEmail,
        String resume,
        String coverLetter,
        ApplicationStatus status,
        LocalDateTime appliedAt,
        LocalDateTime updatedAt
) {

    public static ApplicationResponse from(Application application) {

        return new ApplicationResponse(
                application.getId(),

                application.getJob().getId(),
                application.getJob().getTitle(),

                application.getApplicant().getId(),
                application.getApplicant().getName(),
                application.getApplicant().getEmail(),

                application.getResume(),
                application.getCoverLetter(),

                application.getStatus(),
                application.getAppliedAt(),
                application.getUpdatedAt()
        );
    }
}