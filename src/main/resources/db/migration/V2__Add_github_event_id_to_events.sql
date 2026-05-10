ALTER TABLE github_events
    ADD COLUMN IF NOT EXISTS github_event_id VARCHAR(255);

UPDATE github_events
SET github_event_id = 'legacy-' || id
WHERE github_event_id IS NULL;

ALTER TABLE github_events
    ALTER COLUMN github_event_id SET NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS idx_github_events_github_event_id
    ON github_events(github_event_id);
