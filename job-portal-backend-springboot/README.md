# Job Portal & ATS — Backend (Auth Module, Spring Boot)

Java 17 + Spring Boot 3 + Spring Security + Spring Data JPA + MySQL. This is
the auth slice of the full project — register, login, JWT refresh, logout,
role-based access.

> **Why Spring Boot instead of Node/Express?** This is the stack I have the
> strongest command of. I judged that delivering higher-quality, better-tested,
> more secure code in a familiar stack outweighs strict adherence to the MERN
> acronym. The frontend is still React per the requirements.

## Prerequisites

- **Java 17** (`java -version` to check)
- **Maven** (or use the included `mvnw` wrapper — recommend adding one via
  `mvn -N wrapper:wrapper` if it's not already present)
- **MySQL** — local install, or a free cloud instance (Railway, Aiven)

## 1. Get a MySQL database

**Option A — No local install:**
1. Go to https://railway.app or https://aiven.io, sign up free
2. Create a MySQL database, note host/port/db name/username/password

**Option B — Local MySQL:**
```sql
CREATE DATABASE job_portal;
```

## 2. Configure environment variables

Spring Boot reads config from environment variables (see `application.yml`).
Copy `.env.example` to `.env` for reference, then set these in your shell
before running (or configure them in your IDE's run configuration):

```bash
export DB_HOST=localhost
export DB_PORT=3306
export DB_NAME=job_portal
export DB_USERNAME=root
export DB_PASSWORD=yourpassword
export JWT_ACCESS_SECRET=$(openssl rand -hex 32)
export JWT_REFRESH_SECRET=$(openssl rand -hex 32)
export CLIENT_URL=http://localhost:5173
```

> IntelliJ IDEA: Run Configuration → Environment Variables — paste the same
> key=value pairs there instead of exporting in a shell.

## 3. Build and run

```bash
mvn clean install
mvn spring-boot:run
```

On first run, Hibernate (`ddl-auto: update` in `application.yml`) will
auto-create the `users` table from the `User` entity — no manual migration
step needed for this slice.

Server starts at `http://localhost:5000`.

```bash
curl http://localhost:5000/api/v1/health
```

## API endpoints (auth)

| Method | Route | Auth required | Description |
|---|---|---|---|
| POST | `/api/v1/auth/register` | No | Create account (`name, email, password, role, phone?`) |
| POST | `/api/v1/auth/login` | No | Login (`email, password`) → returns accessToken + refreshToken |
| POST | `/api/v1/auth/refresh-token` | No | Exchange refresh token for new access token |
| POST | `/api/v1/auth/logout` | Yes | Invalidates stored refresh token |
| GET  | `/api/v1/auth/me` | Yes | Get current user profile |

**Auth header format:** `Authorization: Bearer <accessToken>`
**Role values:** `JOBSEEKER` or `RECRUITER` (uppercase, exact)

## Quick test with curl

```bash
# Register
curl -X POST http://localhost:5000/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Test User","email":"test@example.com","password":"Password123!","role":"JOBSEEKER"}'

# Login
curl -X POST http://localhost:5000/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"Password123!"}'

# Get current user (replace TOKEN with the accessToken from login response)
curl http://localhost:5000/api/v1/auth/me \
  -H "Authorization: Bearer TOKEN"
```

## Project structure

```
src/main/java/com/jobportal/auth/
├── JobPortalApplication.java   # entry point
├── config/
│   ├── SecurityConfig.java     # filter chain, CORS, password encoder
│   └── RateLimitFilter.java    # brute-force protection on /register, /login
├── controller/                 # REST endpoints
├── dto/                        # request/response shapes (never expose entities directly)
├── entity/                     # JPA entities (User, Role)
├── exception/                  # custom exceptions + global handler
├── repository/                 # Spring Data JPA repositories
├── security/                   # JwtUtil, JwtAuthFilter, CustomUserDetailsService
└── service/                    # business logic (AuthService)

src/main/java/com/jobportal/job/
├── controller/JobController.java
├── dto/                         # JobRequest, JobResponse, JobStatusUpdateRequest, PagedResponse
├── entity/                      # Job, EmploymentType, JobStatus
├── repository/JobRepository.java
├── service/JobService.java      # CRUD, ownership checks, search/filter/pagination
└── specification/JobSpecification.java  # dynamic WHERE-clause builder for search/filter
```

## API endpoints (jobs)

| Method | Route | Auth required | Description |
|---|---|---|---|
| GET | `/api/v1/jobs` | No | Search/filter/paginate **published** jobs |
| GET | `/api/v1/jobs/my` | Yes (RECRUITER) | List your own jobs, any status |
| GET | `/api/v1/jobs/{id}` | No (published) / Yes (owner, if draft/closed) | Get a single job |
| POST | `/api/v1/jobs` | Yes (RECRUITER) | Create a job (starts as `DRAFT`) |
| PUT | `/api/v1/jobs/{id}` | Yes (RECRUITER, owner only) | Update job details |
| PATCH | `/api/v1/jobs/{id}/status` | Yes (RECRUITER, owner only) | Change status: `DRAFT`/`PUBLISHED`/`CLOSED` |
| DELETE | `/api/v1/jobs/{id}` | Yes (RECRUITER, owner only) | Delete a job |

**Search/filter query params on `GET /api/v1/jobs`:**
`search`, `location`, `employmentType` (`FULL_TIME`/`PART_TIME`/`CONTRACT`/`INTERNSHIP`),
`minSalary`, `maxSalary`, `minExperience`, `page` (default 0), `size` (default 10, max 100),
`sortBy` (`createdAt`/`salaryMin`/`salaryMax`/`title`/`experienceMin`), `sortDir` (`asc`/`desc`)

**Example:**
```bash
curl "http://localhost:5000/api/v1/jobs?search=developer&location=bangalore&minSalary=600000&page=0&size=5&sortBy=salaryMax&sortDir=desc"
```

**Create a job (as a logged-in recruiter):**
```bash
curl -X POST http://localhost:5000/api/v1/jobs \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer RECRUITER_ACCESS_TOKEN" \
  -d '{
    "title": "Backend Developer",
    "description": "Build and maintain REST APIs",
    "location": "Bangalore",
    "employmentType": "FULL_TIME",
    "experienceMin": 1,
    "experienceMax": 4,
    "salaryMin": 600000,
    "salaryMax": 1200000,
    "skills": "Java, Spring Boot, MySQL"
  }'

# Publish it (jobs start as DRAFT and are invisible in public search until published)
curl -X PATCH http://localhost:5000/api/v1/jobs/1/status \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer RECRUITER_ACCESS_TOKEN" \
  -d '{"status": "PUBLISHED"}'
```

## What's next (not yet built)

Next to add, following the same layered pattern (entity → repository → service → controller):
- `Application` entity + apply/status-tracking endpoints (with `MultipartFile` resume upload)
- `Interview` entity + scheduling endpoints
- `Notification` entity + endpoints
- Recruiter dashboard stats endpoint
- React frontend

## Security features already in place

- Passwords hashed with BCrypt (strength 12)
- JWT access token (short-lived, 15 min default) + refresh token (long-lived,
  7 days default, revocable via DB — cleared on logout)
- In-memory rate limiting on `/register` and `/login` (20 requests / 15 min per IP)
- Stateless sessions (`SessionCreationPolicy.STATELESS`) — no server-side session state
- CSRF disabled (safe here since there are no cookies/sessions to forge — pure
  stateless Bearer-token API)
- CORS restricted to `CLIENT_URL`
- Bean validation (`@Valid`) on all request DTOs
- Generic error messages on login/register (no user enumeration)
- Global exception handler — no stack traces leaked to clients
- `@Transactional` on state-changing service methods
- `@PreAuthorize` method-level role checks on all recruiter-only Job endpoints
- Ownership checks on update/status-change/delete — a recruiter can only modify their own jobs
- Whitelisted sort fields (prevents arbitrary field probing via `sortBy` param)
- Page size capped at 100 (prevents oversized result-set requests)
- Draft/closed jobs are invisible to everyone except the owning recruiter
