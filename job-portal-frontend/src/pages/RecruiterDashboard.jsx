import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import {
    createJob,
    getApplicationsForJob,
    getJobs,
    updateApplicationStatus,
    updateJobStatus
} from "../services/api";

function RecruiterDashboard() {
    const [jobs, setJobs] = useState([]);
    const [selectedJob, setSelectedJob] = useState(null);
    const [applications, setApplications] = useState([]);

    const [loading, setLoading] = useState(true);
    const [applicationsLoading, setApplicationsLoading] = useState(false);

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");

    const [showCreateForm, setShowCreateForm] = useState(false);

    const [jobForm, setJobForm] = useState({
        title: "",
        description: "",
        location: "",
        employmentType: "FULL_TIME",
        experienceMin: 0,
        experienceMax: 3,
        salaryMin: "",
        salaryMax: "",
        skills: "",
        applicationDeadline: ""
    });

    useEffect(() => {
        fetchJobs();
    }, []);

    const fetchJobs = async () => {
        setLoading(true);
        setError("");

        try {
            const response = await getJobs("/my");
            setJobs(response.content || []);
        } catch (err) {
            setError(err.message || "Failed to load jobs");
        } finally {
            setLoading(false);
        }
    };

    const handleJobChange = (e) => {
        setJobForm({
            ...jobForm,
            [e.target.name]: e.target.value
        });
    };

    const handleCreateJob = async (e) => {
        e.preventDefault();

        setError("");
        setSuccess("");

        try {
            const jobData = {
                ...jobForm,
                experienceMin: Number(jobForm.experienceMin),
                experienceMax: Number(jobForm.experienceMax),
                salaryMin: jobForm.salaryMin
                    ? Number(jobForm.salaryMin)
                    : null,
                salaryMax: jobForm.salaryMax
                    ? Number(jobForm.salaryMax)
                    : null,
                applicationDeadline:
                    jobForm.applicationDeadline || null
            };

            await createJob(jobData);

            setSuccess("Job created successfully!");

            setJobForm({
                title: "",
                description: "",
                location: "",
                employmentType: "FULL_TIME",
                experienceMin: 0,
                experienceMax: 3,
                salaryMin: "",
                salaryMax: "",
                skills: "",
                applicationDeadline: ""
            });

            setShowCreateForm(false);

            fetchJobs();

        } catch (err) {
            setError(err.message || "Failed to create job");
        }
    };

    const handleSelectJob = async (job) => {
        setSelectedJob(job);
        setApplications([]);
        setApplicationsLoading(true);
        setError("");

        try {
            const response = await getApplicationsForJob(
                job.id,
                "?page=0&size=100&sortBy=appliedAt&sortDir=desc"
            );

            setApplications(response.content || []);

        } catch (err) {
            setError(
                err.message || "Failed to load applications"
            );
        } finally {
            setApplicationsLoading(false);
        }
    };

    const handlePublishJob = async (job) => {
        setError("");
        setSuccess("");

        try {
            await updateJobStatus(job.id, "PUBLISHED");

            setSuccess("Job published successfully!");

            fetchJobs();

        } catch (err) {
            setError(
                err.message || "Failed to publish job"
            );
        }
    };

    const handleApplicationStatus = async (
        applicationId,
        status
    ) => {
        setError("");
        setSuccess("");

        try {
            await updateApplicationStatus(
                applicationId,
                status
            );

            setSuccess(
                "Application status updated successfully!"
            );

            if (selectedJob) {
                handleSelectJob(selectedJob);
            }

        } catch (err) {
            setError(
                err.message ||
                "Failed to update application status"
            );
        }
    };

    // View applicant resume with JWT authentication
    const handleViewResume = async (applicationId) => {
        try {
            setError("");

            const token = localStorage.getItem("accessToken");

            if (!token) {
                setError("Please login again to view the resume.");
                return;
            }

            const response = await fetch(
                `http://localhost:5000/api/v1/applications/${applicationId}/resume`,
                {
                    method: "GET",
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            if (!response.ok) {
                if (response.status === 401 || response.status === 403) {
                    throw new Error(
                        "Your session has expired. Please login again."
                    );
                }

                throw new Error("Unable to view resume.");
            }

            const blob = await response.blob();

            const fileUrl = window.URL.createObjectURL(blob);

            window.open(fileUrl, "_blank");

            setTimeout(() => {
                window.URL.revokeObjectURL(fileUrl);
            }, 60000);

        } catch (err) {
            console.error("Resume error:", err);

            setError(
                err.message || "Could not view resume."
            );
        }
    };

    return (
        <div className="dashboard-page">

            <div className="dashboard-header">

                <div>
                    <h1>Recruiter Dashboard</h1>

                    <p>
                        Manage your job postings and applicants.
                    </p>
                </div>

                <button
                    className="primary-button"
                    onClick={() =>
                        setShowCreateForm(!showCreateForm)
                    }
                >
                    {showCreateForm
                        ? "Close Form"
                        : "+ Post New Job"}
                </button>

            </div>

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            {success && (
                <div className="success-message">
                    {success}
                </div>
            )}

            {/* Create Job Form */}
            {showCreateForm && (
                <section className="dashboard-card">

                    <h2>
                        Create New Job
                    </h2>

                    <form
                        className="job-form"
                        onSubmit={handleCreateJob}
                    >

                        <div className="form-group">
                            <label>Job Title</label>

                            <input
                                type="text"
                                name="title"
                                value={jobForm.title}
                                onChange={handleJobChange}
                                placeholder="e.g. Java Backend Developer"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>Description</label>

                            <textarea
                                name="description"
                                rows="6"
                                value={jobForm.description}
                                onChange={handleJobChange}
                                placeholder="Describe the job..."
                                required
                            />
                        </div>

                        <div className="form-row">

                            <div className="form-group">
                                <label>Location</label>

                                <input
                                    type="text"
                                    name="location"
                                    value={jobForm.location}
                                    onChange={handleJobChange}
                                    placeholder="e.g. Chennai"
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>Employment Type</label>

                                <select
                                    name="employmentType"
                                    value={jobForm.employmentType}
                                    onChange={handleJobChange}
                                >
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

                        </div>

                        <div className="form-row">

                            <div className="form-group">
                                <label>
                                    Minimum Experience
                                </label>

                                <input
                                    type="number"
                                    name="experienceMin"
                                    min="0"
                                    value={jobForm.experienceMin}
                                    onChange={handleJobChange}
                                    required
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Maximum Experience
                                </label>

                                <input
                                    type="number"
                                    name="experienceMax"
                                    min="0"
                                    value={jobForm.experienceMax}
                                    onChange={handleJobChange}
                                    required
                                />
                            </div>

                        </div>

                        <div className="form-row">

                            <div className="form-group">
                                <label>
                                    Minimum Salary
                                </label>

                                <input
                                    type="number"
                                    name="salaryMin"
                                    min="0"
                                    value={jobForm.salaryMin}
                                    onChange={handleJobChange}
                                    placeholder="e.g. 400000"
                                />
                            </div>

                            <div className="form-group">
                                <label>
                                    Maximum Salary
                                </label>

                                <input
                                    type="number"
                                    name="salaryMax"
                                    min="0"
                                    value={jobForm.salaryMax}
                                    onChange={handleJobChange}
                                    placeholder="e.g. 700000"
                                />
                            </div>

                        </div>

                        <div className="form-group">
                            <label>
                                Skills
                            </label>

                            <input
                                type="text"
                                name="skills"
                                value={jobForm.skills}
                                onChange={handleJobChange}
                                placeholder="Java, Spring Boot, MySQL, REST API"
                                required
                            />
                        </div>

                        <div className="form-group">
                            <label>
                                Application Deadline
                            </label>

                            <input
                                type="datetime-local"
                                name="applicationDeadline"
                                value={jobForm.applicationDeadline}
                                onChange={handleJobChange}
                            />
                        </div>

                        <button
                            type="submit"
                            className="primary-button"
                        >
                            Create Job
                        </button>

                    </form>

                </section>
            )}

            {/* Jobs Section */}
            <section className="dashboard-card">

                <h2>
                    My Job Postings
                </h2>

                {loading && (
                    <p className="loading-message">
                        Loading jobs...
                    </p>
                )}

                {!loading && jobs.length === 0 && (
                    <p>
                        You haven't created any jobs yet.
                    </p>
                )}

                {!loading && jobs.length > 0 && (

                    <div className="recruiter-jobs-list">

                        {jobs.map((job) => (

                            <div
                                className={`recruiter-job ${
                                    selectedJob?.id === job.id
                                        ? "selected-job"
                                        : ""
                                }`}
                                key={job.id}
                            >

                                <div>

                                    <h3>
                                        {job.title}
                                    </h3>

                                    <p>
                                        📍 {job.location}
                                    </p>

                                    <span className="job-status">
                                        {job.status}
                                    </span>

                                </div>

                                <div className="job-actions">

                                    <button
                                        className="secondary-button"
                                        onClick={() =>
                                            handleSelectJob(job)
                                        }
                                    >
                                        View Applicants
                                    </button>

                                    {job.status === "DRAFT" && (
                                        <button
                                            className="primary-button"
                                            onClick={() =>
                                                handlePublishJob(job)
                                            }
                                        >
                                            Publish
                                        </button>
                                    )}

                                    <Link
                                        to={`/jobs/${job.id}`}
                                        className="secondary-button"
                                    >
                                        View Job
                                    </Link>

                                </div>

                            </div>

                        ))}

                    </div>

                )}

            </section>

            {/* Applications */}
            {selectedJob && (
                <section className="dashboard-card">

                    <div className="section-heading">

                        <div>
                            <h2>
                                Applicants
                            </h2>

                            <p>
                                {selectedJob.title}
                            </p>
                        </div>

                        <button
                            className="secondary-button"
                            onClick={() => {
                                setSelectedJob(null);
                                setApplications([]);
                            }}
                        >
                            Close
                        </button>

                    </div>

                    {applicationsLoading && (
                        <p className="loading-message">
                            Loading applicants...
                        </p>
                    )}

                    {!applicationsLoading &&
                        applications.length === 0 && (
                            <p>
                                No applications received yet.
                            </p>
                        )}

                    {!applicationsLoading &&
                        applications.length > 0 && (

                            <div className="applications-list">

                                {applications.map(
                                    (application) => (

                                        <div
                                            className="application-card"
                                            key={application.id}
                                        >

                                            <div className="application-header">

                                                <div>

                                                    <h3>
                                                        {application.applicantName}
                                                    </h3>

                                                    <p>
                                                        {application.applicantEmail}
                                                    </p>

                                                </div>

                                                <span
                                                    className={`application-status status-${application.status.toLowerCase()}`}
                                                >
                                                    {formatStatus(
                                                        application.status
                                                    )}
                                                </span>

                                            </div>

                                            <p>
                                                <strong>
                                                    Applied:
                                                </strong>{" "}
                                                {formatDate(
                                                    application.appliedAt
                                                )}
                                            </p>

                                            {application.coverLetter && (
                                                <div className="cover-letter-preview">

                                                    <strong>
                                                        Cover Letter
                                                    </strong>

                                                    <p>
                                                        {
                                                            application.coverLetter
                                                        }
                                                    </p>

                                                </div>
                                            )}

                                            <div className="application-actions">

                                                {/* Updated Resume Button */}
                                                <button
                                                    type="button"
                                                    className="secondary-button"
                                                    onClick={() =>
                                                        handleViewResume(
                                                            application.id
                                                        )
                                                    }
                                                >
                                                    View Resume
                                                </button>

                                                <select
                                                    value={application.status}
                                                    onChange={(e) =>
                                                        handleApplicationStatus(
                                                            application.id,
                                                            e.target.value
                                                        )
                                                    }
                                                >

                                                    <option value="APPLIED">
                                                        Applied
                                                    </option>

                                                    <option value="SHORTLISTED">
                                                        Shortlisted
                                                    </option>

                                                    <option value="INTERVIEW">
                                                        Interview
                                                    </option>

                                                    <option value="SELECTED">
                                                        Selected
                                                    </option>

                                                    <option value="REJECTED">
                                                        Rejected
                                                    </option>

                                                </select>

                                            </div>

                                        </div>

                                    )
                                )}

                            </div>

                        )}

                </section>
            )}

        </div>
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


function formatDate(dateString) {
    if (!dateString) return "Not available";

    return new Date(dateString).toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );
}


export default RecruiterDashboard;