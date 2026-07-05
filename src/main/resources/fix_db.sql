-- ================================================================
-- FULL RESET SCRIPT v5 - Run in MySQL Workbench
-- ================================================================
USE issuetracker;
SET FOREIGN_KEY_CHECKS = 0;
DROP TABLE IF EXISTS reassign_requests;
DROP TABLE IF EXISTS ratings;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS issue_developers;
DROP TABLE IF EXISTS issues;
DROP TABLE IF EXISTS projects;
DROP TABLE IF EXISTS users;
SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE projects (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    manager_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_project_manager FOREIGN KEY (manager_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE issues (
    id BIGINT NOT NULL AUTO_INCREMENT,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    issue_type VARCHAR(100),
    priority VARCHAR(20),
    status VARCHAR(20) DEFAULT 'OPENED',
    created_date DATETIME,
    due_date DATE,
    file_path VARCHAR(512),
    resolved_file_path VARCHAR(512),
    project_id BIGINT,
    reporter_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_issue_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE SET NULL,
    CONSTRAINT fk_issue_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE issue_developers (
    issue_id BIGINT NOT NULL,
    developer_id BIGINT NOT NULL,
    PRIMARY KEY (issue_id, developer_id),
    CONSTRAINT fk_id_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_id_user FOREIGN KEY (developer_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comments (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message TEXT NOT NULL,
    created_by VARCHAR(255),
    created_date DATETIME,
    issue_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_comment_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE ratings (
    id BIGINT NOT NULL AUTO_INCREMENT,
    score INT NOT NULL,
    feedback TEXT,
    issue_id BIGINT,
    reporter_id BIGINT,
    developer_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_rating_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_rating_reporter FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE SET NULL,
    CONSTRAINT fk_rating_developer FOREIGN KEY (developer_id) REFERENCES users(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE notifications (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message TEXT,
    is_read BIT(1) DEFAULT 0,
    created_date DATETIME,
    user_id BIGINT,
    issue_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_notif_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE reassign_requests (
    id BIGINT NOT NULL AUTO_INCREMENT,
    reason TEXT NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    created_date DATETIME,
    issue_id BIGINT,
    developer_id BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_rr_issue FOREIGN KEY (issue_id) REFERENCES issues(id) ON DELETE CASCADE,
    CONSTRAINT fk_rr_developer FOREIGN KEY (developer_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

SELECT 'Database v5 recreated successfully!' AS result;
