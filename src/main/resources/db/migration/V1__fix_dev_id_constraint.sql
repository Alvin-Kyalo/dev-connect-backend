-- Remove NOT NULL constraint from dev_id to allow projects without developers
ALTER TABLE projects ALTER COLUMN dev_id DROP NOT NULL;
