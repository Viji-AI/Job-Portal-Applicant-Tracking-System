import { useEffect, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { applyForJob, getJobById } from "../services/api";

function JobDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [job, setJob] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    const [coverLetter, setCoverLetter] = useState("");
    const [resume, setResume] = useState(null);
    const [applying, setApplying] = useState(false);
    const [applicationMessage, setApplicationMessage] = useState("");

    const user = (() => {
        try {
            const storedUser = localStorage.getItem("user");
            return storedUser ? JSON.parse(storedUser) : null;
        } catch {
            return null;
        }
    })();

    useEffect(() => {
        const fetchJob = async () => {
            setLoading(true);
            setError("");

            try {
                const response = await getJobById(id);
                setJob(response);
            } catch (err) {
                setError(err.message || "Failed to load job");
            } finally {
                setLoading(false);
            }
        };

        fetchJob();
    }, [id]);

    const handleApply = async (e) => {
        e.preventDefault();

        setApplicationMessage("");
        setError("");

        const token = localStorage.getItem("accessToken");

        if (!token) {
            navigate("/login");
            return;
        }

        if (!user || user.role !== "JOBSEEKER") {
            setError("Only job seekers can apply for jobs.");
            return;
        }

        if (!resume) {
            setError("Please select your resume.");
            return;
        }

        const allowedTypes = [
            "application/pdf",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        ];

        if (!allowedTypes.includes(resume.type)) {
            setError("Only PDF, DOC, and DOCX files are allowed.");
            return;
        }

        if (resume.size > 5 * 1024 * 1024) {
            setError("Resume must be smaller than 5 MB.");
            return;
        }

        setApplying(true);

        try {
            await applyForJob(
                id,
                coverLetter,
                resume
            );

            setApplicationMessage(
                "Application submitted successfully!"
            );

            setCoverLetter("");
            setResume(null);

            document.getElementById("resume-input").value = "";

        } catch (err) {
            setError(
                err.message || "Failed to submit application"
            );
        } finally {
            setApplying(false);
        }
    };

    if (loading) {
        return (
            <div className="page-container">
                <p className="loading-message">
                    Loading job details...
                </p>
            </div>
        );
    }

    if (error && !job) {
        return (
            <div className="page-container">
                <p className="error-message">
                    {error}
                </p>

                <Link
                    to="/jobs"
                    className="secondary-button"
                >
                    Back to Jobs
                </Link>
            </div>
        );
    }

    if (!job) {
        return (
            <div className="page-container">
                <p>Job not found.</p>
            </div>
        );
    }

    return (
        <div className="job-details-page">

            <div className="job-details-container">

                {/* Job Information */}
                <section className="job-details-card">

                    <div className="job-details-header">

                        <div>
                            <h1>{job.title}</h1>

                            <p className="job-location">
                                📍 {job.location}
                            </p>
                        </div>

                        <span className="job-status">
                            {job.status}
                        </span>

                    </div>


                    <div className="job-info-grid">

                        <div>
                            <strong>Employment Type</strong>
                            <p>
                                {formatEmploymentType(
                                    job.employmentType
                                )}
                            </p>
                        </div>

                        <div>
                            <strong>Experience</strong>
                            <p>
                                {job.experienceMin} -{" "}
                                {job.experienceMax} years
                            </p>
                        </div>

                        <div>
                            <strong>Salary</strong>
                            <p>
                                {formatSalary(
                                    job.salaryMin,
                                    job.salaryMax
                                )}
                            </p>
                        </div>

                        <div>
                            <strong>Application Deadline</strong>
                            <p>
                                {job.applicationDeadline
                                    ? formatDate(
                                        job.applicationDeadline
                                    )
                                    : "Not specified"}
                            </p>
                        </div>

                    </div>


                    <div className="job-description">

                        <h2>Job Description</h2>

                        <p>
                            {job.description}
                        </p>

                    </div>


                    {job.skills && (
                        <div className="job-details-skills">

                            <h2>Required Skills</h2>

                            <div className="job-skills">

                                {job.skills
                                    .split(",")
                                    .map((skill) => (
                                        <span
                                            key={skill.trim()}
                                            className="skill-tag"
                                        >
                                            {skill.trim()}
                                        </span>
                                    ))}

                            </div>

                        </div>
                    )}

                    <Link
                        to="/jobs"
                        className="secondary-button"
                    >
                        ← Back to Jobs
                    </Link>

                </section>


                {/* Application Section */}
                {user && user.role === "JOBSEEKER" && (
                    <section className="application-card">

                        <h2>
                            Apply for this Job
                        </h2>

                        {applicationMessage && (
                            <div className="success-message">
                                {applicationMessage}
                            </div>
                        )}

                        {error && (
                            <div className="error-message">
                                {error}
                            </div>
                        )}

                        <form onSubmit={handleApply}>

                            <div className="form-group">

                                <label>
                                    Resume
                                </label>

                                <input
                                    id="resume-input"
                                    type="file"
                                    accept=".pdf,.doc,.docx"
                                    onChange={(e) =>
                                        setResume(
                                            e.target.files[0]
                                        )
                                    }
                                    required
                                />

                                <small>
                                    PDF, DOC or DOCX — Maximum 5 MB
                                </small>

                            </div>


                            <div className="form-group">

                                <label>
                                    Cover Letter
                                </label>

                                <textarea
                                    rows="7"
                                    placeholder="Write a short cover letter..."
                                    value={coverLetter}
                                    onChange={(e) =>
                                        setCoverLetter(
                                            e.target.value
                                        )
                                    }
                                    maxLength="5000"
                                />

                                <small>
                                    {coverLetter.length}/5000
                                </small>

                            </div>


                            <button
                                type="submit"
                                className="primary-button"
                                disabled={applying}
                            >
                                {applying
                                    ? "Submitting..."
                                    : "Submit Application"}
                            </button>

                        </form>

                    </section>
                )}


                {/* Login message */}
                {!user && (
                    <section className="application-card">

                        <h2>
                            Interested in this job?
                        </h2>

                        <p>
                            Login as a job seeker to apply.
                        </p>

                        <Link
                            to="/login"
                            className="primary-button"
                        >
                            Login to Apply
                        </Link>

                    </section>
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


function formatSalary(min, max) {
    if (min == null && max == null) {
        return "Not specified";
    }

    if (min != null && max != null) {
        return `₹${min.toLocaleString("en-IN")} - ₹${max.toLocaleString("en-IN")}`;
    }

    if (min != null) {
        return `From ₹${min.toLocaleString("en-IN")}`;
    }

    return `Up to ₹${max.toLocaleString("en-IN")}`;
}


function formatDate(dateString) {
    return new Date(dateString).toLocaleDateString(
        "en-IN",
        {
            day: "2-digit",
            month: "short",
            year: "numeric"
        }
    );
}


export default JobDetails;