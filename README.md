# TaskFlow API 🚀

TaskFlow is a robust, internal project and task tracking platform deployed for a single company. Think of it as a streamlined alternative to Linear or Asana. It provides the foundation for managing workspaces, projects, and tasks (with subtasks, dependencies, and comments), but its key differentiator is a powerful analytics engine that gives managers real-time insights into team workload, bottlenecks, and project velocity.

## 🛠 Tech Stack

* **Backend:** Java 21+, Spring Boot 3.x
* **Database:** PostgreSQL (with built-in Full-Text Search)
* **Migrations:** Flyway
* **Security:** Spring Security & JWT
* **Caching:** Caffeine Cache (In-Memory)
* **File Storage:** AWS S3
* **Email:** Spring Mail (SMTP/Mailtrap) / AWS SES
* **Frontend:** React / Next.js

## 👥 User Roles & Permissions

TaskFlow utilizes strict Role-Based Access Control (RBAC):
* **Admin:** Full access. Manages users, app settings, and creates workspaces. Can view all projects and company-wide analytics.
* **Manager:** Creates and manages projects they lead. Can assign tasks and view team workload/performance analytics for their specific projects.
* **Member:** Can create tasks, update statuses, log time, and add comments. Can only view their own personal analytics.
* **Viewer:** Read-only access to assigned projects and dashboards. Cannot create or edit data.

## 🗄️ Core Domain Architecture (Database)

The system is broken down into five highly relational domains:

1. **Identity & Organization:** `users`, `company_settings`, `workspaces`, and `workspace_members`. Handles authentication and top-level grouping.
2. **Project Hierarchy:** `projects`, `project_members`, `task_groups`, and `task_statuses`. Dictates how work is organized and who has access.
3. **Task Management:** `tasks`, `task_assignees`, `task_labels`, and `task_dependencies`. The core engine tracking priority, due dates, subtasks, and blockers.
4. **Collaboration:** `comments` (threaded), `attachments` (S3), `notifications`, and `activity_log` (audit trail for all status/field changes).
5. **Time Tracking & Analytics:** `time_entries`, `active_timers`, and pre-calculated snapshots (`daily_workload_snapshots`, `period_metrics`) populated by nightly batch jobs for instant dashboard loading.

## 🔌 Core API Contract

The backend exposes a comprehensive RESTful API. Here is an overview of the primary modules:

### Authentication & Users
* `POST /api/setup/init` - First-launch wizard to create the initial Admin account.
* `POST /api/auth/login` - Authenticate and retrieve JWT.
* `POST /api/users/invite` - Admin endpoint to invite new members.

### Workspaces & Projects
* `GET /api/workspaces` - List workspaces with member/project counts.
* `POST /api/projects` - Create a new project within a workspace.
* `PUT /api/projects/{projectId}/statuses/reorder` - Custom Kanban column ordering.

### Task Management
* `GET /api/projects/{projectId}/tasks` - Fetch tasks with extensive filtering, sorting, and pagination.
* `POST /api/projects/{projectId}/tasks` - Create tasks with priority, estimates, and assignees.
* `PATCH /api/tasks/{id}/status` - Quick-update status (used for Board drag-and-drop).
* `GET /api/my-tasks` - Cross-project view of tasks assigned to the current user.

### Collaboration & Time
* `POST /api/tasks/{taskId}/comments` - Add threaded comments with `@mention` support.
* `POST /api/tasks/{taskId}/attachments` - Multipart file upload to AWS S3.
* `POST /api/tasks/{taskId}/timer/start` - Start an active time-tracking session.

### Analytics
* `GET /api/analytics/workload` - Fetch user capacities, active tasks, and utilization percentages.
* `GET /api/analytics/projects/{projectId}/burndown` - Retrieve ideal vs. actual task burndown data.
* `GET /api/analytics/team-health` - Composite metrics on overdue rates, blockers, and workload balance.

## ⚙️ Local Setup Instructions

### Prerequisites
* Java 21 or higher installed
* PostgreSQL running locally (or via Docker)
* Maven (included via `./mvnw` wrapper)

### 1. Clone the repository
```bash
git clone [https://github.com/davefurn/taskflow.git](https://github.com/davefurn/taskflow.git)
cd taskflow
```
### 2. Configure Environment Variables

Create a `.env` file in the root directory (this file is git-ignored for security). Add your local credentials:

```env
# Database
DB_URL=jdbc:postgresql://localhost:5432/taskflow
DB_USERNAME=your_postgres_user
DB_PASSWORD=your_postgres_password

# Security
JWT_SECRET=your_super_secret_jwt_key

# Email
MAIL_HOST=sandbox.smtp.mailtrap.io
MAIL_PORT=2525
MAIL_USERNAME=your_mailtrap_user
MAIL_PASSWORD=your_mailtrap_password

# App Config
FRONTEND_BASE_URL=http://localhost:3000
```
### 3. Run the Application
You can run the application directly using the Maven wrapper. Flyway will automatically create the required database tables on startup.
```bash
./mvnw spring-boot:run
```
The server will start on `http://localhost:8080`.

## 📚 API Documentation

Once the server is running, you can view and test all available endpoints using the interactive Swagger UI:

* **Swagger UI:** `http://localhost:8080/swagger-ui.html`
* **OpenAPI JSON:** `http://localhost:8080/api-docs`
