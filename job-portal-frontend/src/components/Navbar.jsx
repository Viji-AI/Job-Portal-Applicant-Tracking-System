import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { logoutUser } from "../services/api";

function Navbar() {
    const navigate = useNavigate();

    const [user, setUser] = useState(() => {
        const storedUser = localStorage.getItem("user");

        try {
            return storedUser ? JSON.parse(storedUser) : null;
        } catch {
            return null;
        }
    });

    const handleLogout = async () => {
        try {
            if (localStorage.getItem("accessToken")) {
                await logoutUser();
            }
        } catch {
            // Logout locally even if the server request fails
        }

        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("user");

        setUser(null);
        navigate("/login");
    };

    return (
        <nav className="navbar">

            <div className="navbar-container">

                <Link to="/" className="navbar-brand">
                    JobPortal
                </Link>

                <div className="navbar-links">

                    <Link to="/">Home</Link>

                    <Link to="/jobs">Jobs</Link>

                    {user && user.role === "JOBSEEKER" && (
                        <Link to="/my-applications">
                            My Applications
                        </Link>
                    )}

                    {user && user.role === "RECRUITER" && (
                        <Link to="/recruiter-dashboard">
                            Dashboard
                        </Link>
                    )}

                    {!user ? (
                        <>
                            <Link to="/login">
                                Login
                            </Link>

                            <Link to="/register">
                                Register
                            </Link>
                        </>
                    ) : (
                        <>
                            <span className="navbar-user">
                                Hi, {user.name}
                            </span>

                            <button
                                onClick={handleLogout}
                                className="logout-button"
                            >
                                Logout
                            </button>
                        </>
                    )}

                </div>

            </div>

        </nav>
    );
}

export default Navbar;