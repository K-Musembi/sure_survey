-- Create event_publication table for Spring Modulith
CREATE TABLE event_publication (
  id UUID PRIMARY KEY,
  completion_date TIMESTAMP,
  event_type VARCHAR(512),
  listener_id VARCHAR(512),
  publication_date TIMESTAMP,
  serialized_event TEXT
);
