import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../services/api";

function Login() {
    const navigate = useNavigate();

    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        setError("");
        setLoading(true);

        try {
            const response = await loginUser({
                email: email,
                password: password
            });

            // Save authentication details
            localStorage.setItem(
                "accessToken",
                response.accessToken
            );

            localStorage.setItem(
                "refreshToken",
                response.refreshToken
            );

            localStorage.setItem(
                "user",
                JSON.stringify(response.user)
            );

            // Redirect based on user role
            if (response.user.role === "RECRUITER") {
                window.location.href = "/recruiter-dashboard";
            } else {
                window.location.href = "/jobs";
            }

        } catch (err) {
            console.error("Login error:", err);

            setError(
                err.message || "Login failed. Please check your credentials."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-container">

            <div className="auth-card">

                <h1>JobPortal</h1>

                <h2>Login</h2>

                <p className="auth-subtitle">
                    Login to your account
                </p>

                {error && (
                    <div className="error-message">
                        {error}
                    </div>
                )}

                <form onSubmit={handleSubmit}>

                    {/* Email */}
                    <div className="form-group">

                        <label htmlFor="email">
                            Email
                        </label>

                        <input
                            id="email"
                            type="email"
                            placeholder="Enter your email"
                            value={email}
                            onChange={(e) =>
                                setEmail(e.target.value)
                            }
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
                            placeholder="Enter your password"
                            value={password}
                            onChange={(e) =>
                                setPassword(e.target.value)
                            }
                            required
                        />

                    </div>


                    {/* Login Button */}
                    <button
                        type="submit"
                        className="primary-button"
                        disabled={loading}
                    >
                        {loading
                            ? "Logging in..."
                            : "Login"}
                    </button>

                </form>


                {/* Register Link */}
                <p className="auth-footer">

                    Don't have an account?{" "}

                    <button
                        type="button"
                        className="link-button"
                        onClick={() => navigate("/register")}
                    >
                        Register
                    </button>

                </p>

            </div>

        </div>
    );
}

export default Login;