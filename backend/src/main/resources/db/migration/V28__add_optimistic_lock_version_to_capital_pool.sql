--  This migration adds an optimistic lock version column to the capital_pool table to support optimistic locking in the application.

ALTER TABLE capital_pool
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

