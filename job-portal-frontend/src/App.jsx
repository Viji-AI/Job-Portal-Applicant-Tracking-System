import {
    BrowserRouter,
    Routes,
    Route,
    Navigate
} from "react-router-dom";

import Home from "./pages/Home";
import Login from "./pages/Login";
import Register from "./pages/Register";
import Jobs from "./pages/Jobs";
import JobDetails from "./pages/JobDetails";
import MyApplications from "./pages/MyApplications";
import RecruiterDashboard from "./pages/RecruiterDashboard";

import Navbar from "./components/Navbar";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
    return (
        <BrowserRouter>

            <Navbar />

            <Routes>

                {/* =========================
                    PUBLIC PAGES
                ========================= */}

                <Route
                    path="/"
                    element={<Home />}
                />

                <Route
                    path="/login"
                    element={<Login />}
                />

                <Route
                    path="/register"
                    element={<Register />}
                />

                <Route
                    path="/jobs"
                    element={<Jobs />}
                />

                <Route
                    path="/jobs/:id"
                    element={<JobDetails />}
                />


                {/* =========================
                    JOB SEEKER
                ========================= */}

                <Route
                    path="/my-applications"
                    element={
                        <ProtectedRoute role="JOBSEEKER">
                            <MyApplications />
                        </ProtectedRoute>
                    }
                />


                {/* =========================
                    RECRUITER
                ========================= */}

                <Route
                    path="/recruiter-dashboard"
                    element={
                        <ProtectedRoute role="RECRUITER">
                            <RecruiterDashboard />
                        </ProtectedRoute>
                    }
                />


                {/* =========================
                    UNKNOWN URL
                ========================= */}

                <Route
                    path="*"
                    element={
                        <Navigate
                            to="/"
                            replace
                        />
                    }
                />

            </Routes>

        </BrowserRouter>
    );
}

export default App;