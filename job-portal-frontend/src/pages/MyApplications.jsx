import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { getMyApplications } from "../services/api";

function MyApplications() {
    const [applications, setApplications] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchApplications();
    }, []);

    const fetchApplications = async () => {
        setLoading(true);
        setError("");

        try {
            const response = await getMyApplications(
                "?page=0&size=100&sortBy=appliedAt&sortDir=desc"
            );

            setApplications(response.content || []);
        } catch (err) {
            setError(
                err.message || "Failed to load applications"
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="applications-page">

            <div className="applications-header">
                <h1>My Applications</h1>

                <p>
                    Track the jobs you have applied for.
                </p>
            </div>

            {loading && (
                <p className="loading-message">
                    Loading applications...
                </p>
            )}

            {error && (
                <div className="error-message">
                    {error}
                </div>
            )}

            {!loading &&
                !error &&
                applications.length === 0 && (
                    <div className="empty-message">

                        <h2>
                            No applications yet
                        </h2>

                        <p>
                            Start applying for jobs to see
                            your applications here.
                        </p>

                        <Link
                            to="/jobs"
                            className="primary-button"
                        >
                            Browse Jobs
                        </Link>

                    </div>
                )}

            {!loading &&
                !error &&
                applications.length > 0 && (

                    <div className="applications-list">

                        {applications.map((application) => (

                            <div
                                className="application-card"
                                key={application.id}
                            >

                                <div className="application-header">

                                    <div>
                                        <h2>
                                            {application.jobTitle}
                                        </h2>

                                        <p>
                                            📍 Application ID:{" "}
                                            {application.id}
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


                                <div className="application-info">

                                    <div>
                                        <strong>
                                            Applied On
                                        </strong>

                                        <p>
                                            {formatDate(
                                                application.appliedAt
                                            )}
                                        </p>
                                    </div>

                                    <div>
                                        <strong>
                                            Status
                                        </strong>

                                        <p>
                                            {formatStatus(
                                                application.status
                                            )}
                                        </p>
                                    </div>

                                </div>


                                {application.coverLetter && (
                                    <div className="cover-letter-preview">

                                        <strong>
                                            Cover Letter
                                        </strong>

                                        <p>
                                            {application.coverLetter}
                                        </p>

                                    </div>
                                )}


                                <div className="application-actions">

                                    <Link
                                        to={`/jobs/${application.jobId}`}
                                        className="secondary-button"
                                    >
                                        View Job
                                    </Link>

                                </div>

                            </div>

                        ))}

                    </div>
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


export default MyApplications;