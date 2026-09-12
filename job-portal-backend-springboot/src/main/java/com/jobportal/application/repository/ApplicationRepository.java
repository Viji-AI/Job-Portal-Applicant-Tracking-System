package com.jobportal.application.repository;

import com.jobportal.application.entity.Application;
import com.jobportal.application.entity.ApplicationStatus;
import com.jobportal.auth.entity.User;
import com.jobportal.job.entity.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ApplicationRepository
        extends JpaRepository<Application, Long>,
        JpaSpecificationExecutor<Application> {

    boolean existsByJobAndApplicant(Job job, User applicant);

    Page<Application> findByApplicant(User applicant, Pageable pageable);

    Page<Application> findByJob(Job job, Pageable pageable);

    long countByJob(Job job);

    long countByJobAndStatus(Job job, ApplicationStatus status);

    long countByApplicant(User applicant);

    long countByApplicantAndStatus(User applicant, ApplicationStatus status);
}