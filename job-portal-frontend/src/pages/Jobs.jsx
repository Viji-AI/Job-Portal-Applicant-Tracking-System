import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getJobs } from "../services/api";

function Jobs() {
    const [jobs, setJobs] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [search, setSearch] = useState("");
    const [location, setLocation] = useState("");
    const [employmentType, setEmploymentType] = useState("");

    const fetchJobs = async () => {
        setLoading(true);
        setError("");

        try {
            const params = new URLSearchParams();

            if (search.trim()) {
                params.append("search", search.trim());
            }

            if (location.trim()) {
                params.append("location", location.trim());
            }

            if (employmentType) {
                params.append("employmentType", employmentType);
            }

            const queryString = params.toString();

            const response = await getJobs(
                queryString ? `?${queryString}` : ""
            );

            setJobs(response.content || []);
        } catch (err) {
            setError(err.message || "Failed to load jobs");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchJobs();
    }, []);

    const handleSearch = (e) => {
        e.preventDefault();
        fetchJobs();
    };

    const handleClearFilters = () => {
        setSearch("");
        setLocation("");
        setEmploymentType("");

        setTimeout(() => {
            fetchJobs();
        }, 0);
    };

    return (
        <div className="jobs-page">

            {/* Page Header */}
            <div className="jobs-header">

                <div>
                    <span className="section-label">
                        CAREER OPPORTUNITIES
                    </span>

                    <h1>
                        Find Your Next Job
                    </h1>

                    <p>
                        Explore opportunities that match your skills,
                        experience, and career goals.
                    </p>
                </div>

            </div>


            {/* Search and Filters */}
            <form
                className="job-search-form"
                onSubmit={handleSearch}
            >

                <div className="search-field">
                    <label>
                        Search
                    </label>

                    <input
                        type="text"
                        placeholder="Job title, skills..."
                        value={search}
                        onChange={(e) =>
                            setSearch(e.target.value)
                        }
                    />
                </div>


                <div className="search-field">
                    <label>
                        Location
                    </label>

                    <input
                        type="text"
                        placeholder="e.g. Chennai"
                        value={location}
                        onChange={(e) =>
                            setLocation(e.target.value)
                        }
                    />
                </div>


                <div className="search-field">
                    <label>
                        Employment Type
                    </label>

                    <select
                        value={employmentType}
                        onChange={(e) =>
                            setEmploymentType(e.target.value)
                        }
                    >
                        <option value="">
                            All Types
                        </option>

                        <option value="FULL_TIME">
                            Full Time
                        </option>

                        <option value="PART_TIME">
                            Part Time
                        </option>

                        <option value="CONTRACT">
                            Contract
                        </option>

                        <option value="INTERNSHIP">
                            Internship
                        </option>
                    </select>
                </div>


                <button
                    type="submit"
                    className="primary-button search-button"
                >
                    🔍 Search
                </button>

            </form>


            {/* Results Header */}
            {!loading && !error && (
                <div className="jobs-results-header">

                    <div>
                        <h2>
                            Available Jobs
                        </h2>

                        <p>
                            {jobs.length}{" "}
                            {jobs.length === 1
                                ? "position"
                                : "positions"}{" "}
                            found
                        </p>
                    </div>

                    {(search || location || employmentType) && (
                        <button
                            type="button"
                            className="clear-filters-button"
                            onClick={handleClearFilters}
                        >
                            Clear Filters
                        </button>
                    )}

                </div>
            )}


            {/* Jobs */}
            <div className="jobs-container">

                {loading && (
                    <div className="loading-message">
                        <div className="loading-spinner"></div>
                        <p>
                            Finding the best opportunities...
                        </p>
                    </div>
                )}


                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}


                {!loading &&
                    !error &&
                    jobs.length === 0 && (

                        <div className="empty-message">

                            <div className="empty-icon">
                                🔎
                            </div>

                            <h3>
                                No jobs found
                            </h3>

                            <p>
                                Try changing your search terms
                                or filters to find more opportunities.
                            </p>

                            <button
                                type="button"
                                className="secondary-button"
                                onClick={handleClearFilters}
                            >
                                Clear Filters
                            </button>

                        </div>
                    )}


                {!loading &&
                    !error &&
                    jobs.length > 0 && (

                        <div className="jobs-grid">

                            {jobs.map((job) => (

                                <div
                                    className="job-card"
                                    key={job.id}
                                >

                                    {/* Job Header */}
                                    <div className="job-card-header">

                                        <div className="job-title-area">

                                            <div className="company-placeholder">
                                                {getJobInitials(job.title)}
                                            </div>

                                            <div>
                                                <h2>
                                                    {job.title}
                                                </h2>

                                                <p className="job-company">
                                                    JobPortal Opportunity
                                                </p>
                                            </div>

                                        </div>

                                        <span className="job-status">
                                            {formatStatus(job.status)}
                                        </span>

                                    </div>


                                    {/* Job Information */}
                                    <div className="job-meta">

                                        <p className="job-location">
                                            📍 {job.location}
                                        </p>

                                        <p className="job-type">
                                            💼{" "}
                                            {formatEmploymentType(
                                                job.employmentType
                                            )}
                                        </p>

                                        <p className="job-experience">
                                            🎓{" "}
                                            {job.experienceMin} -{" "}
                                            {job.experienceMax} years
                                        </p>

                                    </div>


                                    {/* Salary */}
                                    {job.salaryMin != null &&
                                        job.salaryMax != null && (

                                            <div className="job-salary-box">

                                                <span>
                                                    Salary
                                                </span>

                                                <strong>
                                                    ₹
                                                    {job.salaryMin.toLocaleString(
                                                        "en-IN"
                                                    )}
                                                    {" - "}
                                                    ₹
                                                    {job.salaryMax.toLocaleString(
                                                        "en-IN"
                                                    )}
                                                </strong>

                                                <small>
                                                    per year
                                                </small>

                                            </div>
                                        )}


                                    {/* Skills */}
                                    {job.skills && (
                                        <div className="job-skills">

                                            {job.skills
                                                .split(",")
                                                .slice(0, 5)
                                                .map((skill) => (
                                                    <span
                                                        key={skill.trim()}
                                                        className="skill-tag"
                                                    >
                                                        {skill.trim()}
                                                    </span>
                                                ))}

                                        </div>
                                    )}


                                    {/* Description */}
                                    {job.description && (
                                        <p className="job-description-preview">
                                            {job.description.length > 120
                                                ? `${job.description.substring(
                                                      0,
                                                      120
                                                  )}...`
                                                : job.description}
                                        </p>
                                    )}


                                    {/* Action */}
                                    <Link
                                        to={`/jobs/${job.id}`}
                                        className="view-job-button"
                                    >
                                        View Job Details →
                                    </Link>

                                </div>

                            ))}

                        </div>
                    )}

            </div>

        </div>
    );
}


function formatEmploymentType(type) {
    if (!type) return "";

    return type
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, (letter) =>
            letter.toUpperCase()
        );
}


function formatStatus(status) {
    if (!status) return "";

    return status
        .replaceAll("_", " ")
        .toLowerCase()
        .replace(/\b\w/g, (letter) =>
            letter.toUpperCase()
        );
}


function getJobInitials(title) {
    if (!title) return "JP";

    const words = title.trim().split(" ");

    if (words.length === 1) {
        return words[0].substring(0, 2).toUpperCase();
    }

    return (
        words[0].charAt(0) +
        words[1].charAt(0)
    ).toUpperCase();
}


export default Jobs;