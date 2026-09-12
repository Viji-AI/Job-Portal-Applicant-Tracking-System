package com.jobportal.job.specification;

import com.jobportal.job.entity.EmploymentType;
import com.jobportal.job.entity.Job;
import com.jobportal.job.entity.JobStatus;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class JobSpecification {

    // Builds a single combined Specification from whichever filters are
    // non-null. Any filter left null is simply skipped — no messy chained
    // if/else query-building in the service layer.
    public static Specification<Job> filterBy(
            String search,
            String location,
            EmploymentType employmentType,
            Integer minSalary,
            Integer maxSalary,
            Integer minExperience,
            JobStatus status,
            Long recruiterId
    ) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("skills")), pattern)
                ));
            }

            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }

            if (employmentType != null) {
                predicates.add(cb.equal(root.get("employmentType"), employmentType));
            }

            // A job matches a salary filter if its range overlaps the requested range
            if (minSalary != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMax"), minSalary));
            }
            if (maxSalary != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("salaryMin"), maxSalary));
            }

            if (minExperience != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceMin"), minExperience));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            if (recruiterId != null) {
                predicates.add(cb.equal(root.get("recruiter").get("id"), recruiterId));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
}
