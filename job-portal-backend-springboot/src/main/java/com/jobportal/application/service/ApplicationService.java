package com.jobportal.application.service;

import com.jobportal.application.dto.ApplicationRequest;
import com.jobportal.application.dto.ApplicationResponse;
import com.jobportal.application.dto.ApplicationStatusUpdateRequest;
import com.jobportal.application.entity.Application;
import com.jobportal.application.entity.ApplicationStatus;
import com.jobportal.application.repository.ApplicationRepository;
import com.jobportal.application.specification.ApplicationSpecification;
import com.jobportal.auth.entity.Role;
import com.jobportal.auth.entity.User;
import com.jobportal.auth.exception.AuthException;
import com.jobportal.auth.repository.UserRepository;
import com.jobportal.job.entity.Job;
import com.jobportal.job.entity.JobStatus;
import com.jobportal.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    private static final String UPLOAD_DIR = "uploads/resumes";

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5 MB

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    /*
     * Job seeker applies for a job.
     */
    @Transactional
    public ApplicationResponse apply(
            ApplicationRequest request,
            MultipartFile resume,
            String applicantEmail
    ) {

        User applicant = getUserByEmail(applicantEmail);

        if (applicant.getRole() != Role.JOBSEEKER) {
            throw new AuthException(
                    "Only job seekers can apply for jobs",
                    HttpStatus.FORBIDDEN
            );
        }

        Job job = jobRepository.findById(request.jobId())
                .orElseThrow(() ->
                        new AuthException("Job not found", HttpStatus.NOT_FOUND)
                );

        /*
         * Applications are allowed only for published jobs.
         */
        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new AuthException(
                    "Applications are allowed only for published jobs",
                    HttpStatus.BAD_REQUEST
            );
        }

        /*
         * Check application deadline.
         */
        if (job.getApplicationDeadline() != null &&
                LocalDateTime.now().isAfter(job.getApplicationDeadline())) {

            throw new AuthException(
                    "The application deadline has passed",
                    HttpStatus.BAD_REQUEST
            );
        }

        /*
         * Prevent duplicate applications.
         */
        if (applicationRepository.existsByJobAndApplicant(job, applicant)) {
            throw new AuthException(
                    "You have already applied for this job",
                    HttpStatus.CONFLICT
            );
        }

        /*
         * Validate resume.
         */
        validateResume(resume);

        /*
         * Save resume.
         */
        String resumePath = saveResume(resume);

        Application application = Application.builder()
                .job(job)
                .applicant(applicant)
                .resume(resumePath)
                .coverLetter(request.coverLetter())
                .status(ApplicationStatus.APPLIED)
                .appliedAt(LocalDateTime.now())
                .build();

        Application saved = applicationRepository.save(application);

        return ApplicationResponse.from(saved);
    }

    /*
     * Job seeker views their own applications.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getMyApplications(
            String applicantEmail,
            ApplicationStatus status,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        User applicant = getUserByEmail(applicantEmail);

        Pageable pageable = buildPageable(
                page,
                size,
                sortBy,
                sortDir
        );

        var specification = ApplicationSpecification.filterBy(
                null,
                applicant.getId(),
                status,
                search
        );

        return applicationRepository
                .findAll(specification, pageable)
                .map(ApplicationResponse::from);
    }

    /*
     * Recruiter views applications for one of their jobs.
     */
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsForJob(
            Long jobId,
            String recruiterEmail,
            ApplicationStatus status,
            String search,
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        User recruiter = getUserByEmail(recruiterEmail);

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new AuthException(
                    "Only recruiters can view applicants",
                    HttpStatus.FORBIDDEN
            );
        }

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() ->
                        new AuthException("Job not found", HttpStatus.NOT_FOUND)
                );

        /*
         * Make sure this recruiter owns the job.
         */
        assertJobOwnership(job, recruiterEmail);

        Pageable pageable = buildPageable(
                page,
                size,
                sortBy,
                sortDir
        );

        var specification = ApplicationSpecification.filterBy(
                jobId,
                null,
                status,
                search
        );

        return applicationRepository
                .findAll(specification, pageable)
                .map(ApplicationResponse::from);
    }

    /*
     * Recruiter views one application.
     */
    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(
            Long applicationId,
            String requesterEmail
    ) {

        Application application = findApplicationOrThrow(applicationId);

        User requester = getUserByEmail(requesterEmail);

        /*
         * Applicant can view their own application.
         */
        if (requester.getRole() == Role.JOBSEEKER) {

            if (!application.getApplicant()
                    .getEmail()
                    .equals(requesterEmail)) {

                throw new AuthException(
                        "You do not have permission to view this application",
                        HttpStatus.FORBIDDEN
                );
            }

            return ApplicationResponse.from(application);
        }

        /*
         * Recruiter can view applications for their own jobs.
         */
        if (requester.getRole() == Role.RECRUITER) {

            assertJobOwnership(
                    application.getJob(),
                    requesterEmail
            );

            return ApplicationResponse.from(application);
        }

        throw new AuthException(
                "You do not have permission to view this application",
                HttpStatus.FORBIDDEN
        );
    }

    /*
     * Recruiter changes application status.
     */
    @Transactional
    public ApplicationResponse updateStatus(
            Long applicationId,
            ApplicationStatusUpdateRequest request,
            String recruiterEmail
    ) {

        Application application =
                findApplicationOrThrow(applicationId);

        User recruiter = getUserByEmail(recruiterEmail);

        if (recruiter.getRole() != Role.RECRUITER) {
            throw new AuthException(
                    "Only recruiters can update application status",
                    HttpStatus.FORBIDDEN
            );
        }

        /*
         * Recruiter must own the job.
         */
        assertJobOwnership(
                application.getJob(),
                recruiterEmail
        );

        ApplicationStatus newStatus = request.status();

        /*
         * Do not allow changing a selected/rejected application
         * back to an earlier stage.
         */
        if (application.getStatus() == ApplicationStatus.SELECTED ||
                application.getStatus() == ApplicationStatus.REJECTED) {

            throw new AuthException(
                    "The application has already reached a final status",
                    HttpStatus.BAD_REQUEST
            );
        }

        application.setStatus(newStatus);

        Application saved =
                applicationRepository.save(application);

        return ApplicationResponse.from(saved);
    }

    /*
     * Securely returns the resume file path.
     *
     * The controller will use this method after authorization.
     */
    @Transactional(readOnly = true)
    public Path getResumePath(
            Long applicationId,
            String requesterEmail
    ) {

        Application application =
                findApplicationOrThrow(applicationId);

        User requester = getUserByEmail(requesterEmail);

        boolean isApplicant =
                requester.getRole() == Role.JOBSEEKER &&
                        application.getApplicant()
                                .getEmail()
                                .equals(requesterEmail);

        boolean isOwnerRecruiter =
                requester.getRole() == Role.RECRUITER &&
                        application.getJob()
                                .getRecruiter()
                                .getEmail()
                                .equals(requesterEmail);

        if (!isApplicant && !isOwnerRecruiter) {
            throw new AuthException(
                    "You do not have permission to access this resume",
                    HttpStatus.FORBIDDEN
            );
        }

        Path path = Paths.get(application.getResume())
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(path)) {
            throw new AuthException(
                    "Resume file not found",
                    HttpStatus.NOT_FOUND
            );
        }

        return path;
    }

    /*
     * Find application.
     */
    private Application findApplicationOrThrow(Long id) {

        return applicationRepository.findById(id)
                .orElseThrow(() ->
                        new AuthException(
                                "Application not found",
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    /*
     * Find user by email.
     */
    private User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AuthException(
                                "User not found",
                                HttpStatus.NOT_FOUND
                        )
                );
    }

    /*
     * Check recruiter ownership of a job.
     */
    private void assertJobOwnership(
            Job job,
            String recruiterEmail
    ) {

        if (!job.getRecruiter()
                .getEmail()
                .equals(recruiterEmail)) {

            throw new AuthException(
                    "You do not have permission to access this job",
                    HttpStatus.FORBIDDEN
            );
        }
    }

    /*
     * Validate resume extension, MIME type and file size.
     */
    private void validateResume(MultipartFile resume) {

        if (resume == null || resume.isEmpty()) {
            throw new AuthException(
                    "Resume is required",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (resume.getSize() > MAX_FILE_SIZE) {
            throw new AuthException(
                    "Resume size cannot exceed 5 MB",
                    HttpStatus.BAD_REQUEST
            );
        }

        String contentType = resume.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType)) {

            throw new AuthException(
                    "Only PDF, DOC and DOCX resumes are allowed",
                    HttpStatus.BAD_REQUEST
            );
        }

        String originalFilename =
                resume.getOriginalFilename();

        if (originalFilename == null ||
                !hasAllowedExtension(originalFilename)) {

            throw new AuthException(
                    "Invalid resume file type",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private boolean hasAllowedExtension(String filename) {

        String lower =
                filename.toLowerCase();

        return lower.endsWith(".pdf") ||
                lower.endsWith(".doc") ||
                lower.endsWith(".docx");
    }

    /*
     * Store resume with a generated filename.
     *
     * UUID prevents filename collisions and avoids trusting
     * the original filename.
     */
    private String saveResume(MultipartFile resume) {

        try {

            Path uploadDirectory =
                    Paths.get(UPLOAD_DIR)
                            .toAbsolutePath()
                            .normalize();

            Files.createDirectories(uploadDirectory);

            String originalFilename =
                    resume.getOriginalFilename();

            String extension = "";

            if (originalFilename != null) {

                int dotIndex =
                        originalFilename.lastIndexOf('.');

                if (dotIndex >= 0) {
                    extension =
                            originalFilename.substring(dotIndex)
                                    .toLowerCase();
                }
            }

            String filename =
                    UUID.randomUUID() + extension;

            Path target =
                    uploadDirectory
                            .resolve(filename)
                            .normalize();

            /*
             * Make sure the generated target is actually
             * inside the upload directory.
             */
            if (!target.startsWith(uploadDirectory)) {
                throw new AuthException(
                        "Invalid file path",
                        HttpStatus.BAD_REQUEST
                );
            }

            Files.copy(
                    resume.getInputStream(),
                    target,
                    StandardCopyOption.REPLACE_EXISTING
            );

            return target.toString();

        } catch (IOException e) {

            throw new AuthException(
                    "Unable to save resume",
                    HttpStatus.INTERNAL_SERVER_ERROR
            );
        }
    }

    /*
     * Pagination + sorting.
     */
    private Pageable buildPageable(
            int page,
            int size,
            String sortBy,
            String sortDir
    ) {

        Set<String> allowedSortFields = Set.of(
                "appliedAt",
                "updatedAt",
                "status"
        );

        String field =
                allowedSortFields.contains(sortBy)
                        ? sortBy
                        : "appliedAt";

        Sort.Direction direction =
                "asc".equalsIgnoreCase(sortDir)
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        int safePage =
                Math.max(page, 0);

        int safeSize =
                Math.min(
                        Math.max(size, 1),
                        100
                );

        return PageRequest.of(
                safePage,
                safeSize,
                Sort.by(direction, field)
        );
    }
}