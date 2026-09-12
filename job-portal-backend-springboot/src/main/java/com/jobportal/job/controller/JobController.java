package com.jobportal.job.controller;

import com.jobportal.job.dto.*;
import com.jobportal.job.entity.EmploymentType;
import com.jobportal.job.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    // ---- Public: search/filter/paginate published jobs ----
    // No auth required — matches SecurityConfig permitAll on GET /api/v1/jobs/**
    @GetMapping
    public ResponseEntity<PagedResponse<JobResponse>> searchJobs(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) EmploymentType employmentType,
            @RequestParam(required = false) Integer minSalary,
            @RequestParam(required = false) Integer maxSalary,
            @RequestParam(required = false) Integer minExperience,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(jobService.searchPublishedJobs(
                search, location, employmentType, minSalary, maxSalary, minExperience,
                page, size, sortBy, sortDir
        ));
    }

    // ---- Recruiter: view own jobs, any status ----
    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<PagedResponse<JobResponse>> getMyJobs(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir
    ) {
        return ResponseEntity.ok(jobService.getMyJobs(authentication.getName(), page, size, sortBy, sortDir));
    }

    // ---- Get single job (public if published; owner-only if draft/closed) ----
    @GetMapping("/{id}")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id, Authentication authentication) {
        // Unauthenticated (public) requests arrive as AnonymousAuthenticationToken,
        // not null — treat both cases as "no logged-in user".
        boolean isAuthenticated = authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
        String requesterEmail = isAuthenticated ? authentication.getName() : null;
        return ResponseEntity.ok(jobService.getJobById(id, requesterEmail));
    }

    // ---- Create (recruiter only) ----
    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request, Authentication authentication) {
        JobResponse response = jobService.createJob(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ---- Update (recruiter, owner only) ----
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> updateJob(
            @PathVariable Long id, @Valid @RequestBody JobRequest request, Authentication authentication
    ) {
        return ResponseEntity.ok(jobService.updateJob(id, request, authentication.getName()));
    }

    // ---- Publish / close (recruiter, owner only) ----
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobResponse> updateStatus(
            @PathVariable Long id, @Valid @RequestBody JobStatusUpdateRequest request, Authentication authentication
    ) {
        return ResponseEntity.ok(jobService.updateStatus(id, request, authentication.getName()));
    }

    // ---- Delete (recruiter, owner only) ----
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, Authentication authentication) {
        jobService.deleteJob(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
