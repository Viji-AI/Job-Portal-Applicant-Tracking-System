package com.jobportal.application.specification;

import com.jobportal.application.entity.Application;
import com.jobportal.application.entity.ApplicationStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class ApplicationSpecification {

    public static Specification<Application> filterBy(
            Long jobId,
            Long applicantId,
            ApplicationStatus status,
            String search
    ) {
        return (root, query, cb) -> {

            List<Predicate> predicates = new ArrayList<>();

            /*
             * Filter by job.
             */
            if (jobId != null) {
                predicates.add(
                        cb.equal(
                                root.get("job").get("id"),
                                jobId
                        )
                );
            }

            /*
             * Filter by applicant.
             */
            if (applicantId != null) {
                predicates.add(
                        cb.equal(
                                root.get("applicant").get("id"),
                                applicantId
                        )
                );
            }

            /*
             * Filter by application status.
             */
            if (status != null) {
                predicates.add(
                        cb.equal(
                                root.get("status"),
                                status
                        )
                );
            }

            /*
             * Search applicant name/email or job title.
             */
            if (search != null && !search.isBlank()) {

                String pattern = "%" + search.toLowerCase().trim() + "%";

                predicates.add(
                        cb.or(
                                cb.like(
                                        cb.lower(root.get("applicant").get("name")),
                                        pattern
                                ),
                                cb.like(
                                        cb.lower(root.get("applicant").get("email")),
                                        pattern
                                ),
                                cb.like(
                                        cb.lower(root.get("job").get("title")),
                                        pattern
                                )
                        )
                );
            }

            return cb.and(
                    predicates.toArray(new Predicate[0])
            );
        };
    }
}
