package com.jobportal.job.dto;

import com.jobportal.job.entity.EmploymentType;
import com.jobportal.job.entity.Job;
import com.jobportal.job.entity.JobStatus;

import java.time.LocalDateTime;

public record JobResponse(
        Long id,
        Long recruiterId,
        String recruiterName,
        String title,
        String description,
        String location,
        EmploymentType employmentType,
        Integer experienceMin,
        Integer experienceMax,
        Integer salaryMin,
        Integer salaryMax,
        String skills,
        JobStatus status,
        LocalDateTime applicationDeadline,
        LocalDateTime createdAt
) {
    public static JobResponse from(Job job) {
        return new JobResponse(
                job.getId(),
                job.getRecruiter().getId(),
                job.getRecruiter().getName(),
                job.getTitle(),
                job.getDescription(),
                job.getLocation(),
                job.getEmploymentType(),
                job.getExperienceMin(),
                job.getExperienceMax(),
                job.getSalaryMin(),
                job.getSalaryMax(),
                job.getSkills(),
                job.getStatus(),
                job.getApplicationDeadline(),
                job.getCreatedAt()
        );
    }
}
