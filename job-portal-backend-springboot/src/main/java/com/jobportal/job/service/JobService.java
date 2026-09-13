package com.jobportal.job.service;

import com.jobportal.auth.entity.User;
import com.jobportal.auth.exception.AuthException;
import com.jobportal.auth.repository.UserRepository;
import com.jobportal.job.dto.*;
import com.jobportal.job.entity.EmploymentType;
import com.jobportal.job.entity.Job;
import com.jobportal.job.entity.JobStatus;
import com.jobportal.job.repository.JobRepository;
import com.jobportal.job.specification.JobSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    // ---- Public search for jobs

    public PagedResponse<JobResponse> searchPublishedJobs(
            String search, String location, EmploymentType employmentType,
            Integer minSalary, Integer maxSalary, Integer minExperience,
            int page, int size, String sortBy, String sortDir
    ) {
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        var spec = JobSpecification.filterBy(
                search, location, employmentType, minSalary, maxSalary,
                minExperience, JobStatus.PUBLISHED, null
        );

        Page<Job> jobs = jobRepository.findAll(spec, pageable);
        return PagedResponse.from(jobs.map(JobResponse::from));
    }

    // ---- Recruiter's own jobs 

    public PagedResponse<JobResponse> getMyJobs(String recruiterEmail, int page, int size, String sortBy, String sortDir) {
        User recruiter = getUserByEmail(recruiterEmail);
        Pageable pageable = buildPageable(page, size, sortBy, sortDir);

        var spec = JobSpecification.filterBy(null, null, null, null, null, null, null, recruiter.getId());
        Page<Job> jobs = jobRepository.findAll(spec, pageable);
        return PagedResponse.from(jobs.map(JobResponse::from));
    }

    // ---- Get single job ----

    public JobResponse getJobById(Long id, String requesterEmail) {
        Job job = findJobOrThrow(id);

        // Non-published jobs are only visible to the recruiter who owns them 
        if (job.getStatus() != JobStatus.PUBLISHED) {
            if (requesterEmail == null || !job.getRecruiter().getEmail().equals(requesterEmail)) {
                throw new AuthException("Job not found", HttpStatus.NOT_FOUND);
            }
        }

        return JobResponse.from(job);
    }

    // ---- Create ----

    @Transactional
    public JobResponse createJob(JobRequest request, String recruiterEmail) {
        User recruiter = getUserByEmail(recruiterEmail);
        validateExperienceAndSalaryRanges(request);

        Job job = Job.builder()
                .recruiter(recruiter)
                .title(request.title())
                .description(request.description())
                .location(request.location())
                .employmentType(request.employmentType())
                .experienceMin(request.experienceMin())
                .experienceMax(request.experienceMax())
                .salaryMin(request.salaryMin())
                .salaryMax(request.salaryMax())
                .skills(request.skills())
                .applicationDeadline(request.applicationDeadline())
                .status(JobStatus.DRAFT) 
                .build();

        return JobResponse.from(jobRepository.save(job));
    }

    // ---- Update ----

    @Transactional
    public JobResponse updateJob(Long id, JobRequest request, String recruiterEmail) {
        Job job = findJobOrThrow(id);
        assertOwnership(job, recruiterEmail);
        validateExperienceAndSalaryRanges(request);

        job.setTitle(request.title());
        job.setDescription(request.description());
        job.setLocation(request.location());
        job.setEmploymentType(request.employmentType());
        job.setExperienceMin(request.experienceMin());
        job.setExperienceMax(request.experienceMax());
        job.setSalaryMin(request.salaryMin());
        job.setSalaryMax(request.salaryMax());
        job.setSkills(request.skills());
        job.setApplicationDeadline(request.applicationDeadline());

        return JobResponse.from(jobRepository.save(job));
    }

    // ---- Publish / close ----

    @Transactional
    public JobResponse updateStatus(Long id, JobStatusUpdateRequest request, String recruiterEmail) {
        Job job = findJobOrThrow(id);
        assertOwnership(job, recruiterEmail);

        job.setStatus(request.status());
        return JobResponse.from(jobRepository.save(job));
    }

    // ---- Delete ----

    @Transactional
    public void deleteJob(Long id, String recruiterEmail) {
        Job job = findJobOrThrow(id);
        assertOwnership(job, recruiterEmail);
        jobRepository.delete(job);
    }

    // ---- Helpers ----

    private Job findJobOrThrow(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new AuthException("Job not found", HttpStatus.NOT_FOUND));
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AuthException("User not found", HttpStatus.NOT_FOUND));
    }

    private void assertOwnership(Job job, String requesterEmail) {
        if (!job.getRecruiter().getEmail().equals(requesterEmail)) {
            throw new AuthException("You do not have permission to modify this job", HttpStatus.FORBIDDEN);
        }
    }

    private void validateExperienceAndSalaryRanges(JobRequest request) {
        if (request.experienceMin() > request.experienceMax()) {
            throw new AuthException("experienceMin cannot exceed experienceMax", HttpStatus.BAD_REQUEST);
        }
        if (request.salaryMin() > request.salaryMax()) {
            throw new AuthException("salaryMin cannot exceed salaryMax", HttpStatus.BAD_REQUEST);
        }
    }

    private Pageable buildPageable(int page, int size, String sortBy, String sortDir) {
        var allowedSortFields = java.util.Set.of("createdAt", "salaryMin", "salaryMax", "title", "experienceMin");
        String field = allowedSortFields.contains(sortBy) ? sortBy : "createdAt";

        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC;

        int safeSize = Math.min(Math.max(size, 1), 100);
        int safePage = Math.max(page, 0);

        return PageRequest.of(safePage, safeSize, Sort.by(direction, field));
    }
}
