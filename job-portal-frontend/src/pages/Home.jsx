import { Link } from "react-router-dom";

function Home() {
    return (
        <div className="home-page">

            {/* Hero Section */}
            <section className="hero-section">

                <div className="hero-content">

                    <div className="hero-badge">
                        🚀 Your Career Starts Here
                    </div>

                    <h1>
                        Find Your
                        <span className="hero-highlight">
                            {" "}Dream Job
                        </span>
                    </h1>

                    <p>
                        Discover the right opportunities, connect with
                        employers, and take the next step toward your
                        career goals.
                    </p>

                    <div className="hero-buttons">

                        <Link
                            to="/jobs"
                            className="primary-button"
                        >
                            🔎 Browse Jobs
                        </Link>

                        <Link
                            to="/register"
                            className="secondary-button"
                        >
                            Create Free Account
                        </Link>

                    </div>

                    <div className="hero-stats">

                        <div className="hero-stat">
                            <strong>100+</strong>
                            <span>Job Opportunities</span>
                        </div>

                        <div className="hero-stat">
                            <strong>50+</strong>
                            <span>Companies</span>
                        </div>

                        <div className="hero-stat">
                            <strong>Easy</strong>
                            <span>Application Process</span>
                        </div>

                    </div>

                </div>

            </section>


            {/* Features Section */}
            <section className="features-section">

                <div className="section-title">

                    <span className="section-label">
                        SIMPLE &amp; POWERFUL
                    </span>

                    <h2>
                        Everything You Need
                        <br />
                        to Build Your Career
                    </h2>

                    <p>
                        JobPortal makes finding and applying for your
                        next opportunity simple and convenient.
                    </p>

                </div>


                <div className="features-grid">

                    <div className="feature-card">

                        <div className="feature-icon">
                            🔍
                        </div>

                        <h3>
                            Find the Right Jobs
                        </h3>

                        <p>
                            Search and filter opportunities by location,
                            experience, skills, and employment type.
                        </p>

                        <Link
                            to="/jobs"
                            className="feature-link"
                        >
                            Explore Jobs →
                        </Link>

                    </div>


                    <div className="feature-card">

                        <div className="feature-icon">
                            📄
                        </div>

                        <h3>
                            Apply Easily
                        </h3>

                        <p>
                            Upload your resume, add a cover letter,
                            and apply for suitable positions quickly.
                        </p>

                        <Link
                            to="/jobs"
                            className="feature-link"
                        >
                            Start Applying →
                        </Link>

                    </div>


                    <div className="feature-card">

                        <div className="feature-icon">
                            📊
                        </div>

                        <h3>
                            Track Applications
                        </h3>

                        <p>
                            Monitor your applications and stay updated
                            on your recruitment progress in one place.
                        </p>

                        <Link
                            to="/my-applications"
                            className="feature-link"
                        >
                            View Applications →
                        </Link>

                    </div>


                    <div className="feature-card">

                        <div className="feature-icon">
                            👥
                        </div>

                        <h3>
                            Recruiter Dashboard
                        </h3>

                        <p>
                            Create job postings, review applicants,
                            manage applications, and find great talent.
                        </p>

                        <Link
                            to="/recruiter-dashboard"
                            className="feature-link"
                        >
                            Recruiter Portal →
                        </Link>

                    </div>

                </div>

            </section>


            {/* How It Works */}
            <section className="how-it-works-section">

                <div className="section-title">

                    <span className="section-label">
                        HOW IT WORKS
                    </span>

                    <h2>
                        Start Your Journey in 3 Steps
                    </h2>

                </div>


                <div className="steps-grid">

                    <div className="step-card">

                        <div className="step-number">
                            01
                        </div>

                        <h3>
                            Create an Account
                        </h3>

                        <p>
                            Register as a job seeker and create
                            your profile in just a few steps.
                        </p>

                    </div>


                    <div className="step-card">

                        <div className="step-number">
                            02
                        </div>

                        <h3>
                            Find Your Opportunity
                        </h3>

                        <p>
                            Browse available jobs and use filters
                            to find positions that match your skills.
                        </p>

                    </div>


                    <div className="step-card">

                        <div className="step-number">
                            03
                        </div>

                        <h3>
                            Apply &amp; Track
                        </h3>

                        <p>
                            Submit your application and track its
                            status directly from your dashboard.
                        </p>

                    </div>

                </div>

            </section>


            {/* Call To Action */}
            <section className="cta-section">

                <div className="cta-content">

                    <span className="section-label">
                        YOUR NEXT OPPORTUNITY IS WAITING
                    </span>

                    <h2>
                        Ready to Take the Next Step?
                    </h2>

                    <p>
                        Explore opportunities and start building
                        the career you've been looking for.
                    </p>

                    <div className="cta-buttons">

                        <Link
                            to="/jobs"
                            className="primary-button"
                        >
                            Browse Jobs
                        </Link>

                        <Link
                            to="/register"
                            className="secondary-button"
                        >
                            Join JobPortal
                        </Link>

                    </div>

                </div>

            </section>

        </div>
    );
}

export default Home;