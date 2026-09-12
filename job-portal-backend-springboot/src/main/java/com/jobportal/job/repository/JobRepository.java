package com.jobportal.job.repository;

import com.jobportal.job.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

// JpaSpecificationExecutor lets us build dynamic WHERE clauses at runtime
// (title/location/type/salary/experience filters) without writing a
// separate @Query for every possible filter combination.
public interface JobRepository extends JpaRepository<Job, Long>, JpaSpecificationExecutor<Job> {
}
