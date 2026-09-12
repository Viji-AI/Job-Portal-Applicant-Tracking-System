package com.jobportal.application.entity;

import com.jobportal.auth.entity.User;
import com.jobportal.job.entity.Job;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "applications",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_application_job_applicant",
                        columnNames = {"job_id", "applicant_id"}
                )
        },
        indexes = {
                @Index(name = "idx_application_job", columnList = "job_id"),
                @Index(name = "idx_application_applicant", columnList = "applicant_id"),
                @Index(name = "idx_application_status", columnList = "status")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The job for which the applicant is applying.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_id", nullable = false)
    private Job job;

    /*
     * The user who applied for the job.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    /*
     * Path/name of the uploaded resume.
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String resume;

    /*
     * Optional cover letter submitted by the applicant.
     */
    @Column(columnDefinition = "TEXT")
    private String coverLetter;

    /*
     * Current stage of the application.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    /*
     * Date and time when the application was submitted.
     */
    @Builder.Default
    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    /*
     * Automatically updated whenever the application is modified.
     */
    private LocalDateTime updatedAt;

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}