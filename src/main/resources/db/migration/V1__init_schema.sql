
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE company_settings (
                                  id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                                  name       VARCHAR(255) NOT NULL,
                                  created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE users (
                       id                    UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                       name                  VARCHAR(255) NOT NULL,
                       email                 VARCHAR(255) UNIQUE NOT NULL,
                       password_hash         VARCHAR(255) NOT NULL,
                       role                  VARCHAR(20)  NOT NULL CHECK (role IN ('admin','manager','member','viewer')),
                       job_title             VARCHAR(255),
                       avatar_url            VARCHAR(500),
                       timezone              VARCHAR(50)  DEFAULT 'UTC',
                       email_verified        BOOLEAN      DEFAULT FALSE,
                       must_change_pwd       BOOLEAN      DEFAULT FALSE,
                       weekly_capacity_hours DECIMAL(4,1) DEFAULT 40.0,
                       invited_by            UUID         REFERENCES users(id) ON DELETE SET NULL,
                       last_login            TIMESTAMPTZ,
                       created_at            TIMESTAMPTZ  DEFAULT NOW()
);


CREATE TABLE workspaces (
                            id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                            name        VARCHAR(255) NOT NULL,
                            description TEXT,
                            created_at  TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE workspace_members (
                                   workspace_id  UUID REFERENCES workspaces(id) ON DELETE CASCADE,
                                   user_id       UUID REFERENCES users(id)      ON DELETE CASCADE,
                                   role_override VARCHAR(20),
                                   joined_at     TIMESTAMPTZ DEFAULT NOW(),
                                   PRIMARY KEY (workspace_id, user_id)
);

CREATE TABLE projects (
                          id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                          workspace_id    UUID        REFERENCES workspaces(id) ON DELETE CASCADE,
                          name            VARCHAR(255) NOT NULL,
                          description     TEXT,
                          colour          VARCHAR(7)   DEFAULT '#6366F1',
                          icon            VARCHAR(50),
                          status          VARCHAR(20)  DEFAULT 'not_started'
                              CHECK (status IN ('not_started','in_progress','on_hold','completed','archived')),
                          lead_id         UUID         REFERENCES users(id) ON DELETE SET NULL,
                          start_date      DATE,
                          target_end_date DATE,
                          created_by      UUID         REFERENCES users(id) ON DELETE SET NULL,
                          created_at      TIMESTAMPTZ  DEFAULT NOW(),
                          updated_at      TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE project_members (
                                 project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
                                 user_id    UUID REFERENCES users(id)    ON DELETE CASCADE,
                                 role       VARCHAR(20) DEFAULT 'member'
                                     CHECK (role IN ('lead','member','viewer')),
                                 joined_at  TIMESTAMPTZ DEFAULT NOW(),
                                 PRIMARY KEY (project_id, user_id)
);

CREATE TABLE task_statuses (
                               id            UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                               project_id    UUID        REFERENCES projects(id) ON DELETE CASCADE,
                               name          VARCHAR(100) NOT NULL,
                               colour        VARCHAR(7)   DEFAULT '#6B7280',
                               position      INTEGER      NOT NULL,
                               is_done_state BOOLEAN      DEFAULT FALSE,
                               is_default    BOOLEAN      DEFAULT FALSE
);

CREATE TABLE task_groups (
                             id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                             project_id UUID        REFERENCES projects(id) ON DELETE CASCADE,
                             name       VARCHAR(255) NOT NULL,
                             position   INTEGER      NOT NULL,
                             created_at TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE tasks (
                       id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                       project_id      UUID        REFERENCES projects(id)      ON DELETE CASCADE,
                       task_group_id   UUID        REFERENCES task_groups(id)   ON DELETE SET NULL,
                       parent_task_id  UUID        REFERENCES tasks(id)         ON DELETE CASCADE,
                       title           VARCHAR(500) NOT NULL,
                       description     TEXT,
                       status_id       UUID        REFERENCES task_statuses(id) ON DELETE SET NULL,
                       priority        VARCHAR(10)  DEFAULT 'none'
                           CHECK (priority IN ('urgent','high','medium','low','none')),
                       due_date        DATE,
                       start_date      DATE,
                       estimated_hours DECIMAL(6,1),
                       weight          INTEGER,
                       position        INTEGER      DEFAULT 0,
                       is_recurring    BOOLEAN      DEFAULT FALSE,
                       recurrence_rule VARCHAR(100),
                       completed_at    TIMESTAMPTZ,
                       created_by      UUID        REFERENCES users(id) ON DELETE SET NULL,
                       search_vector   TSVECTOR,
                       created_at      TIMESTAMPTZ  DEFAULT NOW(),
                       updated_at      TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_tasks_project  ON tasks(project_id);
CREATE INDEX idx_tasks_status   ON tasks(status_id);
CREATE INDEX idx_tasks_parent   ON tasks(parent_task_id);
CREATE INDEX idx_tasks_due_date ON tasks(due_date);
CREATE INDEX idx_tasks_created  ON tasks(created_at);
CREATE INDEX idx_tasks_search   ON tasks USING GIN(search_vector);

CREATE TABLE task_assignees (
                                task_id     UUID REFERENCES tasks(id) ON DELETE CASCADE,
                                user_id     UUID REFERENCES users(id) ON DELETE CASCADE,
                                assigned_at TIMESTAMPTZ DEFAULT NOW(),
                                PRIMARY KEY (task_id, user_id)
);

CREATE INDEX idx_task_assignees_user ON task_assignees(user_id);

CREATE TABLE labels (
                        id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
                        project_id UUID        REFERENCES projects(id) ON DELETE CASCADE,
                        name       VARCHAR(100) NOT NULL,
                        colour     VARCHAR(7)   DEFAULT '#EF4444',
                        created_at TIMESTAMPTZ  DEFAULT NOW()
);

CREATE TABLE task_labels (
                             task_id  UUID REFERENCES tasks(id)  ON DELETE CASCADE,
                             label_id UUID REFERENCES labels(id) ON DELETE CASCADE,
                             PRIMARY KEY (task_id, label_id)
);

CREATE TABLE task_dependencies (
                                   id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   task_id            UUID REFERENCES tasks(id) ON DELETE CASCADE,
                                   depends_on_task_id UUID REFERENCES tasks(id) ON DELETE CASCADE,
                                   dependency_type    VARCHAR(20) DEFAULT 'blocked_by'
                                       CHECK (dependency_type IN ('blocked_by','related_to')),
                                   created_at         TIMESTAMPTZ DEFAULT NOW(),
                                   UNIQUE (task_id, depends_on_task_id)
);

CREATE TABLE comments (
                          id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          task_id           UUID REFERENCES tasks(id)    ON DELETE CASCADE,
                          user_id           UUID REFERENCES users(id)    ON DELETE SET NULL,
                          content           TEXT NOT NULL,
                          parent_comment_id UUID REFERENCES comments(id) ON DELETE CASCADE,
                          created_at        TIMESTAMPTZ DEFAULT NOW(),
                          updated_at        TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_comments_task ON comments(task_id);

CREATE TABLE attachments (
                             id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             task_id     UUID REFERENCES tasks(id) ON DELETE CASCADE,
                             uploaded_by UUID REFERENCES users(id) ON DELETE SET NULL,
                             file_name   VARCHAR(255) NOT NULL,
                             file_url    VARCHAR(500) NOT NULL,
                             file_size   BIGINT,
                             mime_type   VARCHAR(100),
                             created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE time_entries (
                              id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              task_id     UUID REFERENCES tasks(id) ON DELETE CASCADE,
                              user_id     UUID REFERENCES users(id) ON DELETE SET NULL,
                              hours       DECIMAL(5,2) NOT NULL,
                              description TEXT,
                              date        DATE         NOT NULL,
                              started_at  TIMESTAMPTZ,
                              ended_at    TIMESTAMPTZ,
                              created_at  TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_time_entries_task ON time_entries(task_id);
CREATE INDEX idx_time_entries_user ON time_entries(user_id);
CREATE INDEX idx_time_entries_date ON time_entries(date);

CREATE TABLE activity_log (
                              id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              project_id    UUID REFERENCES projects(id) ON DELETE CASCADE,
                              task_id       UUID REFERENCES tasks(id)    ON DELETE CASCADE,
                              user_id       UUID REFERENCES users(id)    ON DELETE SET NULL,
                              action        VARCHAR(50) NOT NULL,
                              field_changed VARCHAR(50),
                              old_value     TEXT,
                              new_value     TEXT,
                              created_at    TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_activity_task    ON activity_log(task_id,    created_at);
CREATE INDEX idx_activity_user    ON activity_log(user_id,    created_at);
CREATE INDEX idx_activity_project ON activity_log(project_id, created_at);

CREATE TABLE notifications (
                               id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               user_id    UUID REFERENCES users(id) ON DELETE CASCADE,
                               type       VARCHAR(50)  NOT NULL,
                               title      VARCHAR(255) NOT NULL,
                               message    TEXT,
                               link_url   VARCHAR(500),
                               is_read    BOOLEAN     DEFAULT FALSE,
                               created_at TIMESTAMPTZ DEFAULT NOW()
);

CREATE INDEX idx_notifications_user ON notifications(user_id, is_read, created_at);

CREATE TABLE notification_preferences (
                                          user_id              UUID PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
                                          task_assigned        BOOLEAN DEFAULT TRUE,
                                          mentioned_in_comment BOOLEAN DEFAULT TRUE,
                                          task_due_tomorrow    BOOLEAN DEFAULT TRUE,
                                          task_overdue         BOOLEAN DEFAULT TRUE,
                                          status_changes       BOOLEAN DEFAULT FALSE,
                                          weekly_summary       BOOLEAN DEFAULT TRUE,
                                          email_enabled        BOOLEAN DEFAULT TRUE,
                                          updated_at           TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE active_timers (
                               id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               task_id    UUID REFERENCES tasks(id) ON DELETE CASCADE,
                               user_id    UUID REFERENCES users(id) ON DELETE CASCADE,
                               started_at TIMESTAMPTZ NOT NULL,
                               UNIQUE (user_id)
);

CREATE TABLE daily_workload_snapshots (
                                          id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                          user_id         UUID REFERENCES users(id)    ON DELETE CASCADE,
                                          project_id      UUID REFERENCES projects(id) ON DELETE CASCADE,
                                          snapshot_date   DATE         NOT NULL,
                                          active_tasks    INTEGER      DEFAULT 0,
                                          assigned_hours  DECIMAL(6,1) DEFAULT 0,
                                          assigned_weight INTEGER      DEFAULT 0,
                                          overdue_tasks   INTEGER      DEFAULT 0,
                                          completed_today INTEGER      DEFAULT 0,
                                          hours_logged    DECIMAL(5,2) DEFAULT 0,
                                          created_at      TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_workload_snapshot ON daily_workload_snapshots(user_id, snapshot_date);

CREATE TABLE period_metrics (
                                id                      UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                project_id              UUID REFERENCES projects(id) ON DELETE CASCADE,
                                period_type             VARCHAR(10)  NOT NULL CHECK (period_type IN ('week','sprint','month')),
                                period_start            DATE         NOT NULL,
                                period_end              DATE         NOT NULL,
                                tasks_completed         INTEGER      DEFAULT 0,
                                weight_completed        INTEGER      DEFAULT 0,
                                tasks_created           INTEGER      DEFAULT 0,
                                tasks_overdue           INTEGER      DEFAULT 0,
                                avg_cycle_time_hours    DECIMAL(8,2),
                                median_cycle_time_hours DECIMAL(8,2),
                                p90_cycle_time_hours    DECIMAL(8,2),
                                avg_lead_time_hours     DECIMAL(8,2),
                                scope_added             INTEGER      DEFAULT 0,
                                scope_removed           INTEGER      DEFAULT 0,
                                created_at              TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_period_metrics ON period_metrics(project_id, period_type, period_start);

CREATE TABLE user_period_metrics (
                                     id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     user_id              UUID REFERENCES users(id)    ON DELETE CASCADE,
                                     project_id           UUID REFERENCES projects(id) ON DELETE CASCADE,
                                     period_type          VARCHAR(10)  NOT NULL,
                                     period_start         DATE         NOT NULL,
                                     period_end           DATE         NOT NULL,
                                     tasks_completed      INTEGER      DEFAULT 0,
                                     tasks_assigned       INTEGER      DEFAULT 0,
                                     on_time_count        INTEGER      DEFAULT 0,
                                     overdue_count        INTEGER      DEFAULT 0,
                                     avg_cycle_time_hours DECIMAL(8,2),
                                     hours_logged         DECIMAL(6,2) DEFAULT 0,
                                     hours_estimated      DECIMAL(6,2) DEFAULT 0,
                                     created_at           TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_user_metrics ON user_period_metrics(user_id, period_type, period_start);

CREATE TABLE team_health_history (
                                     id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                     workspace_id     UUID REFERENCES workspaces(id) ON DELETE CASCADE,
                                     project_id       UUID REFERENCES projects(id)   ON DELETE CASCADE,
                                     score_date       DATE         NOT NULL,
                                     health_score     DECIMAL(5,2),
                                     overdue_rate     DECIMAL(5,2),
                                     blocked_rate     DECIMAL(5,2),
                                     workload_balance DECIMAL(5,2),
                                     velocity_trend   DECIMAL(5,2),
                                     created_at       TIMESTAMPTZ  DEFAULT NOW()
);

CREATE INDEX idx_health_history ON team_health_history(workspace_id, score_date);

CREATE OR REPLACE FUNCTION tasks_search_vector_update() RETURNS TRIGGER AS $$
BEGIN
    NEW.search_vector := to_tsvector('english',
        COALESCE(NEW.title, '') || ' ' || COALESCE(NEW.description, ''));
RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER tasks_search_vector_trigger
    BEFORE INSERT OR UPDATE OF title, description
                     ON tasks
                         FOR EACH ROW EXECUTE FUNCTION tasks_search_vector_update();