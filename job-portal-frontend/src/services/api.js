const API_BASE_URL = "http://localhost:5000/api/v1";

const apiRequest = async (endpoint, options = {}) => {
    const token = localStorage.getItem("accessToken");

    const headers = {
        ...options.headers,
    };

    if (!(options.body instanceof FormData)) {
        headers["Content-Type"] = "application/json";
    }

    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }

    const response = await fetch(
        `${API_BASE_URL}${endpoint}`,
        {
            ...options,
            headers,
        }
    );

    if (!response.ok) {
        let errorMessage = "Something went wrong";

        try {
            const errorData = await response.json();
            errorMessage =
                errorData.message || errorMessage;
        } catch {
        }

        throw new Error(errorMessage);
    }

    if (response.status === 204) {
        return null;
    }

    return response.json();
};


// =========================
// AUTH
// =========================

export const registerUser = async (userData) => {
    return apiRequest("/auth/register", {
        method: "POST",
        body: JSON.stringify(userData),
    });
};


export const loginUser = async (loginData) => {
    return apiRequest("/auth/login", {
        method: "POST",
        body: JSON.stringify(loginData),
    });
};


export const getCurrentUser = async () => {
    return apiRequest("/auth/me");
};


export const logoutUser = async () => {
    return apiRequest("/auth/logout", {
        method: "POST",
    });
};


// =========================
// JOBS
// =========================

export const getJobs = async (params = "") => {
    return apiRequest(`/jobs${params}`);
};


export const getJobById = async (id) => {
    return apiRequest(`/jobs/${id}`);
};


export const createJob = async (jobData) => {
    return apiRequest("/jobs", {
        method: "POST",
        body: JSON.stringify(jobData),
    });
};


export const updateJob = async (id, jobData) => {
    return apiRequest(`/jobs/${id}`, {
        method: "PUT",
        body: JSON.stringify(jobData),
    });
};


export const updateJobStatus = async (id, status) => {
    return apiRequest(`/jobs/${id}/status`, {
        method: "PATCH",
        body: JSON.stringify({ status }),
    });
};


// =========================
// APPLICATIONS
// =========================

export const applyForJob = async (
    jobId,
    coverLetter,
    resumeFile
) => {
    const formData = new FormData();

    formData.append("jobId", jobId);
    formData.append(
        "coverLetter",
        coverLetter || ""
    );
    formData.append("resume", resumeFile);

    return apiRequest("/applications", {
        method: "POST",
        body: formData,
    });
};


export const getMyApplications = async (
    params = ""
) => {
    return apiRequest(
        `/applications/my${params}`
    );
};


export const getApplicationsForJob = async (
    jobId,
    params = ""
) => {
    return apiRequest(
        `/applications/job/${jobId}${params}`
    );
};


export const getApplicationById = async (id) => {
    return apiRequest(
        `/applications/${id}`
    );
};


export const updateApplicationStatus = async (
    id,
    status
) => {
    return apiRequest(
        `/applications/${id}/status`,
        {
            method: "PATCH",
            body: JSON.stringify({ status }),
        }
    );
};


export default apiRequest;
