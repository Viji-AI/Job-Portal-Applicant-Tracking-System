import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { registerUser } from "../services/api";

function Register() {
    const navigate = useNavigate();

    const [formData, setFormData] = useState({
        name: "",
        email: "",
        password: "",
        phone: "",
        role: "JOBSEEKER"
    });

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setFormData({
            ...formData,
            [e.target.name]: e.target.value
        });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        setError("");
        setSuccess("");
        setLoading(true);

        try {
            await registerUser(formData);

            setSuccess(
                "Registration successful! Redirecting to login..."
            );

            setTimeout(() => {
                window.location.href = "/login";
            }, 1000);

        } catch (err) {
            console.error("Registration error:", err);

            setError(
                err.message ||
                "Registration failed. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">

            <div className="auth-card">

                <h1>JobPortal</h1>

                <h2>Create Account</h2>

                <p className="auth-subtitle">
                    Register as a Job Seeker or Recruiter
                </p>

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

                <form onSubmit={handleSubmit}>

                    {/* Name */}
                    <div className="form-group">

                        <label htmlFor="name">
                            Full Name
                        </label>

                        <input
                            id="name"
                            type="text"
                            name="name"
                            placeholder="Enter your full name"
                            value={formData.name}
                            onChange={handleChange}
                            required
                        />

                    </div>


                    {/* Email */}
                    <div className="form-group">

                        <label htmlFor="email">
                            Email
                        </label>

                        <input
                            id="email"
                            type="email"
                            name="email"
                            placeholder="Enter your email"
                            value={formData.email}
                            onChange={handleChange}
                            required
                        />

                    </div>


                    {/* Phone */}
                    <div className="form-group">

                        <label htmlFor="phone">
                            Phone
                        </label>

                        <input
                            id="phone"
                            type="tel"
                            name="phone"
                            placeholder="Enter your phone number"
                            value={formData.phone}
                            onChange={handleChange}
                            required
                        />

                    </div>


                    {/* Password */}
                    <div className="form-group">

                        <label htmlFor="password">
                            Password
                        </label>

                        <input
                            id="password"
                            type="password"
                            name="password"
                            placeholder="Create a password"
                            value={formData.password}
                            onChange={handleChange}
                            required
                        />

                    </div>


                    {/* Role */}
                    <div className="form-group">

                        <label htmlFor="role">
                            Register As
                        </label>

                        <select
                            id="role"
                            name="role"
                            value={formData.role}
                            onChange={handleChange}
                        >
                            <option value="JOBSEEKER">
                                Job Seeker
                            </option>

                            <option value="RECRUITER">
                                Recruiter
                            </option>
                        </select>

                    </div>


                    {/* Register Button */}
                    <button
                        type="submit"
                        className="primary-button"
                        disabled={loading}
                    >
                        {loading
                            ? "Creating Account..."
                            : "Register"}
                    </button>

                </form>


                {/* Login Link */}
                <p className="auth-footer">

                    Already have an account?{" "}

                    <button
                        type="button"
                        className="link-button"
                        onClick={() =>
                            window.location.href = "/login"
                        }
                    >
                        Login
                    </button>

                </p>

            </div>

        </div>
    );
}

export default Register;