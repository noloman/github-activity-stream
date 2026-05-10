-- Create the github_events table
CREATE TABLE github_events (
    id BIGSERIAL PRIMARY KEY,
    type VARCHAR(50) NOT NULL,
    repo_name VARCHAR(255) NOT NULL,
    actor_login VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    github_event_id VARCHAR(255) NOT NULL UNIQUE,
    payload TEXT NOT NULL,
    processed BOOLEAN DEFAULT FALSE
);

-- Add indexes for common queries
CREATE INDEX idx_github_events_repo_name ON github_events(repo_name);
CREATE INDEX idx_github_events_type ON github_events(type);
CREATE INDEX idx_github_events_created_at ON github_events(created_at);
CREATE INDEX idx_github_events_processed ON github_events(processed);
CREATE INDEX idx_github_events_github_event_id ON github_events(github_event_id);
