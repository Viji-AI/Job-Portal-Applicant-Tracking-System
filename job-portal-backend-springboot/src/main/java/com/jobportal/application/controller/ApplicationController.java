package com.jobportal.application.controller;

import com.jobportal.application.dto.ApplicationRequest;
import com.jobportal.application.dto.ApplicationResponse;
import com.jobportal.application.dto.ApplicationStatusUpdateRequest;
import com.jobportal.application.entity.ApplicationStatus;
import com.jobportal.application.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    /*
     * ---------------------------------------------------------
     * JOB SEEKER - APPLY FOR A JOB
     * ---------------------------------------------------------
     * Fields:
     * jobId
     * coverLetter
     * resume
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('JOBSEEKER')")
    public ResponseEntity<ApplicationResponse> apply(
            @RequestParam Long jobId,
            @RequestParam(required = false) String coverLetter,
            @RequestPart("resume") MultipartFile resume,
            org.springframework.security.core.Authentication authentication
    ) {

        ApplicationRequest request =
                new ApplicationRequest(jobId, coverLetter);

        ApplicationResponse response =
                applicationService.apply(
                        request,
                        resume,
                        authentication.getName()
                );

        return ResponseEntity
                .status(201)
                .body(response);
    }

    /*
     * ---------------------------------------------------------
     * JOB SEEKER - VIEW MY APPLICATIONS
     * ---------------------------------------------------------
     *
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('JOBSEEKER')")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            org.springframework.security.core.Authentication authentication,

            @RequestParam(required = false)
            ApplicationStatus status,

            @RequestParam(required = false)
            String search,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "appliedAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir
    ) {

        Page<ApplicationResponse> applications =
                applicationService.getMyApplications(
                        authentication.getName(),
                        status,
                        search,
                        page,
                        size,
                        sortBy,
                        sortDir
                );

        return ResponseEntity.ok(applications);
    }

    /*
     * ---------------------------------------------------------
     * RECRUITER - VIEW APPLICANTS FOR A JOB
     * ---------------------------------------------------------
     *
     */
    @GetMapping("/job/{jobId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<ApplicationResponse>> getApplicationsForJob(
            @PathVariable Long jobId,

            org.springframework.security.core.Authentication authentication,

            @RequestParam(required = false)
            ApplicationStatus status,

            @RequestParam(required = false)
            String search,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "appliedAt")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir
    ) {

        Page<ApplicationResponse> applications =
                applicationService.getApplicationsForJob(
                        jobId,
                        authentication.getName(),
                        status,
                        search,
                        page,
                        size,
                        sortBy,
                        sortDir
                );

        return ResponseEntity.ok(applications);
    }

    /*
     * ---------------------------------------------------------
     * VIEW SINGLE APPLICATION
     * ---------------------------------------------------------
     *
     * The applicant can view their own application.
     * The recruiter can view an application belonging to
     * their own job.
     *
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('JOBSEEKER', 'RECRUITER')")
    public ResponseEntity<ApplicationResponse> getApplication(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication
    ) {

        return ResponseEntity.ok(
                applicationService.getApplicationById(
                        id,
                        authentication.getName()
                )
        );
    }

    /*
     * ---------------------------------------------------------
     * RECRUITER - UPDATE APPLICATION STATUS
     * ---------------------------------------------------------
     *
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,

            @Valid
            @RequestBody ApplicationStatusUpdateRequest request,

            org.springframework.security.core.Authentication authentication
    ) {

        return ResponseEntity.ok(
                applicationService.updateStatus(
                        id,
                        request,
                        authentication.getName()
                )
        );
    }

    /*
     * ---------------------------------------------------------
     * VIEW / DOWNLOAD RESUME
     * ---------------------------------------------------------
     *
     * Only:
     * - the applicant
     * - recruiter who owns the job
     *
     * can access the resume.
     */
    @GetMapping("/{id}/resume")
    @PreAuthorize("hasAnyRole('JOBSEEKER', 'RECRUITER')")
    public ResponseEntity<Resource> getResume(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication
    ) {

        Path resumePath =
                applicationService.getResumePath(
                        id,
                        authentication.getName()
                );

        try {

            Resource resource =
                    new UrlResource(
                            resumePath.toUri()
                    );

            if (!resource.exists() ||
                    !resource.isReadable()) {

                return ResponseEntity.notFound().build();
            }

            String filename =
                    resumePath.getFileName().toString();

            MediaType mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;

            if (filename.toLowerCase().endsWith(".pdf")) {
                mediaType = MediaType.APPLICATION_PDF;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .header(
                            HttpHeaders.CONTENT_DISPOSITION,
                            ContentDisposition
                                    .inline()
                                    .filename(filename)
                                    .build()
                                    .toString()
                    )
                    .body(resource);

        } catch (Exception e) {

            return ResponseEntity
                    .internalServerError()
                    .build();
        }
    }
}
