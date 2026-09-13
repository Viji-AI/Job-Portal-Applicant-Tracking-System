# Job Portal & Applicant Tracking System

A full-stack **Job Portal and Applicant Tracking System (ATS)** built with **Spring Boot, React, MySQL, and Spring Security**.

The application provides separate workflows for **Job Seekers** and **Recruiters**. Job seekers can discover opportunities, apply with a resume and cover letter, and track their applications. Recruiters can create and manage job postings, review applicants, view resumes, and update application statuses.

---

## ⚠️ Technology Stack Note

The original task specification requested the **MERN stack**.

For this implementation, I used:

- **Frontend:** React + Vite
- **Frontend tooling:** Node.js + npm
- **Backend:** Java 17 + Spring Boot + Spring Security + Spring Data JPA
- **Database:** MySQL

Node.js is used for the React frontend development environment, package management, and Vite development server. **Node.js/Express are not used as the backend**, and MongoDB is not used.

I chose Spring Boot for the backend because it is the backend technology I have the strongest practical command of, allowing me to implement the required functionality with reliability, security, and proper testing within the given timeframe.

The implementation includes the core recruitment requirements such as authentication, role-based access, job management, job search/filtering, resume upload, job applications, application status tracking, and recruiter functionality.


---

## 🚀 Tech Stack

### Backend
- Java 17
- Spring Boot 3
- Spring Security
- JWT Authentication
- Spring Data JPA / Hibernate
- MySQL
- Maven
- REST APIs
- BCrypt password hashing

### Frontend
- React
- Vite
- React Router
- JavaScript
- HTML5
- CSS3
- Node.js
- npm
- Responsive UI

### Database
- MySQL

---

## ✨ Main Features

### 🔐 Authentication & Authorization
- User registration and login
- JWT-based authentication
- Access token and refresh token support
- Logout
- Role-based access control
- BCrypt password hashing
- Protected recruiter and job-seeker routes

### 👤 Job Seeker
- Browse published jobs
- Search jobs by title/skills
- Filter jobs by location and employment type
- View complete job details
- Upload resume
- Submit cover letter
- Apply for jobs
- Track submitted applications
- View application status

### 🏢 Recruiter
- Register as a recruiter
- Create job postings
- Update job postings
- Publish and close jobs
- View recruiter dashboard
- View applications for jobs
- View applicant resumes
- Update application status

### 💼 Job Management
- Job title and description
- Location
- Employment type
- Experience range
- Salary range
- Required skills
- Application deadline
- Draft / Published / Closed job status

### 📄 Resume Handling
- PDF, DOC, and DOCX support
- Maximum resume size: 5 MB
- Resume upload during application
- Recruiters can securely view applicant resumes

---

## 🏗️ Project Structure

```text
job-portal-backend-springboot/
│
├── job-portal-backend-springboot/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/jobportal/
│   │       │   ├── auth/
│   │       │   ├── job/
│   │       │   └── application/
│   │       └── resources/
│   │           └── application.yml
│   └── pom.xml
│
├── job-portal-frontend/
│   ├── src/
│   │   ├── components/
│   │   ├── pages/
│   │   ├── services/
│   │   ├── App.jsx
│   │   ├── main.jsx
│   │   └── index.css
│   ├── package.json
│   └── vite.config.js
│
├── .gitignore
└── README.md
```

---

# ⚙️ Setup & Installation

## Prerequisites

Install the following:

- Java 17
- Maven
- Node.js
- npm
- MySQL
- Git

Verify installations:

```bash
java -version
mvn -version
node -v
npm -v
mysql --version
```

---

# 🗄️ 1. Database Setup

Open MySQL Workbench or MySQL command line and create the database:

```sql
CREATE DATABASE job_portal;
```

The application uses Hibernate to create/update the required tables automatically.

> No manual table creation is required for the current project.

---

# 🔧 2. Backend Configuration

The backend runs on:

```text
http://localhost:5000
```

Configure the database connection and JWT secrets using environment variables.

Example:

```text
DB_HOST=localhost
DB_PORT=3306
DB_NAME=job_portal
DB_USERNAME=root
DB_PASSWORD=YOUR_MYSQL_PASSWORD

JWT_ACCESS_SECRET=YOUR_ACCESS_SECRET
JWT_REFRESH_SECRET=YOUR_REFRESH_SECRET

CLIENT_URL=http://localhost:5173
```

### IntelliJ IDEA

Open:

```text
Run → Edit Configurations → JobPortalApplication
```

Add the environment variables under **Environment variables**.

Do not commit real passwords or JWT secrets to GitHub.

---

# ▶️ 3. Run the Backend

Open a terminal in:

```text
job-portal-backend-springboot/job-portal-backend-springboot
```

Run:

```bash
mvn clean install
```

Then:

```bash
mvn spring-boot:run
```

The backend should start at:

```text
http://localhost:5000
```

Health check:

```text
http://localhost:5000/api/v1/health
```

---

# 💻 4. Run the Frontend

Open another terminal in:

```text
job-portal-backend-springboot/job-portal-frontend
```

Install dependencies:

```bash
npm install
```

Start the React development server:

```bash
npm run dev
```

Open:

```text
http://localhost:5173
```

---

# 🧪 Recruiter Testing Guide

The easiest way to evaluate the complete application is to test both roles.

## Test 1 — Job Seeker Flow

### Step 1: Register

Open:

```text
http://localhost:5173/register
```

Create an account with:

```text
Register As: Job Seeker
```

Use any test email, name, phone number, and password.

### Step 2: Login

Open:

```text
http://localhost:5173/login
```

Login using the account you created.

### Step 3: Browse Jobs

Go to:

```text
Jobs
```

Test:
- Search
- Location filter
- Employment type filter
- Job details

### Step 4: Apply

Open a published job.

Click:

```text
Apply Now
```

Upload a test resume:

```text
PDF / DOC / DOCX
Maximum 5 MB
```

Enter a cover letter and submit the application.

### Step 5: Track Application

Open:

```text
My Applications
```

The submitted application should appear with its current status.

---

# 🏢 Test 2 — Recruiter Flow

### Step 1: Create Recruiter Account

Open:

```text
http://localhost:5173/register
```

Select:

```text
Register As: Recruiter
```

Create a test recruiter account.

### Step 2: Login

Login using the recruiter account.

The application redirects the recruiter to:

```text
/recruiter-dashboard
```

### Step 3: Create a Job

Create a job posting with example information:

```text
Title:
Java Backend Developer

Description:
Build and maintain REST APIs using Java and Spring Boot.

Location:
Chennai

Employment Type:
FULL_TIME

Experience:
0 - 3 years

Salary:
₹400000 - ₹700000

Skills:
Java, Spring Boot, MySQL, REST API

Application Deadline:
Choose a future date
```

### Step 4: Publish the Job

A newly created job starts as:

```text
DRAFT
```

Publish it from the recruiter dashboard.

After publishing, it becomes visible in the public Jobs page.

### Step 5: Review Applications

After a job seeker applies:

1. Open the recruiter dashboard.
2. Select the relevant job.
3. View applicants.
4. View the applicant's resume.
5. Review the cover letter/application information.
6. Update the application status.

Supported application status flow includes:

```text
APPLIED
SHORTLISTED
INTERVIEW
REJECTED
HIRED
```

---

# 🔑 Test Account

A job-seeker test account that has been used during development:

```text
Email: viji@test.com
Password: Pass@123
Role: JOBSEEKER
```

You can also create fresh test accounts from the Register page.

> For recruiter testing, create a recruiter account from the application instead of relying on a shared recruiter password.

---

# 🔌 REST API Overview

Backend base URL:

```text
http://localhost:5000/api/v1
```

## Authentication

| Method | Endpoint | Access |
|---|---|---|
| POST | `/auth/register` | Public |
| POST | `/auth/login` | Public |
| POST | `/auth/refresh-token` | Public |
| POST | `/auth/logout` | Authenticated |
| GET | `/auth/me` | Authenticated |

## Jobs

| Method | Endpoint | Access |
|---|---|---|
| GET | `/jobs` | Public |
| GET | `/jobs/{id}` | Public for published jobs |
| GET | `/jobs/my` | Recruiter |
| POST | `/jobs` | Recruiter |
| PUT | `/jobs/{id}` | Job owner |
| PATCH | `/jobs/{id}/status` | Job owner |
| DELETE | `/jobs/{id}` | Job owner |

## Applications

| Method | Endpoint | Access |
|---|---|---|
| POST | `/applications` | Job Seeker |
| GET | `/applications/my` | Job Seeker |
| GET | `/applications/{id}` | Authenticated |
| GET | `/applications/job/{jobId}` | Recruiter |
| PATCH | `/applications/{id}/status` | Recruiter |
| GET | `/applications/{id}/resume` | Authorized user |

Authentication uses:

```text
Authorization: Bearer <accessToken>
```

---

# 🔎 Job Search & Filtering

The Jobs API supports filtering and pagination.

Available query parameters include:

```text
search
location
employmentType
minSalary
maxSalary
minExperience
page
size
sortBy
sortDir
```

Example:

```text
http://localhost:5000/api/v1/jobs?search=developer&location=chennai&page=0&size=10
```

---

# 🛡️ Security

The project includes several security measures:

- JWT-based stateless authentication
- BCrypt password hashing
- Role-based authorization
- Protected API endpoints
- Ownership checks for recruiter job management
- Rate limiting for authentication endpoints
- Request validation
- Restricted CORS configuration
- Maximum resume upload size
- Allowed resume file-type validation
- Generic authentication error messages
- Global exception handling
- No passwords or JWT secrets stored in source control

---

# 📱 UI Pages

### Public Pages
- Home
- Jobs
- Job Details
- Login
- Register

### Job Seeker Pages
- My Applications

### Recruiter Pages
- Recruiter Dashboard

The frontend is responsive and designed for desktop and mobile screen sizes.

---

# 🔄 Application Workflow

```text
                    ┌─────────────────┐
                    │     Register    │
                    └────────┬────────┘
                             │
                 ┌───────────┴───────────┐
                 │                       │
          JOB SEEKER                  RECRUITER
                 │                       │
                 ▼                       ▼
          Browse Jobs              Create Job
                 │                       │
                 ▼                       ▼
           Job Details               Draft Job
                 │                       │
                 ▼                       ▼
          Upload Resume              Publish
                 │                       │
                 ▼                       ▼
            Apply Job             Receive Applications
                 │                       │
                 ▼                       ▼
        Track Application          Review Applicants
                                         │
                                         ▼
                                  Update Status
```

---

# 📌 Current Scope

This version focuses on the core recruitment workflow:

- Authentication
- Job management
- Job discovery
- Applications
- Resume handling
- Application tracking
- Recruiter dashboard

Advanced modules such as interview scheduling, email notifications, and production deployment are outside the current scope.

---

# 👨‍💻 Developer Notes

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

DTOs are used for API request/response handling, while Spring Security handles authentication and authorization.

The React frontend communicates with the Spring Boot backend through REST APIs.

---

# 📄 License

This project was developed as a technical/full-stack project demonstrating a complete Job Portal and Applicant Tracking System workflow.
