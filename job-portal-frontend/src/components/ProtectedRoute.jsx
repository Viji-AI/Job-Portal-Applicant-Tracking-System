import { Navigate } from "react-router-dom";

function ProtectedRoute({ children, role }) {
    const token = localStorage.getItem("accessToken");
    const userData = localStorage.getItem("user");

    // Not logged in
    if (!token || !userData) {
        return <Navigate to="/login" replace />;
    }

    let user;

    try {
        user = JSON.parse(userData);
    } catch {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("refreshToken");
        localStorage.removeItem("user");

        return <Navigate to="/login" replace />;
    }

    // Logged in but wrong role
    if (role && user.role !== role) {
        if (user.role === "RECRUITER") {
            return <Navigate to="/recruiter-dashboard" replace />;
        }

        return <Navigate to="/jobs" replace />;
    }

    return children;
}

export default ProtectedRoute;