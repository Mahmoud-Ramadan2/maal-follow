
-- V_27 Add collection_status and collected_amount columns to collection_route_item table

ALTER TABLE collection_route_item
    ADD COLUMN collection_status ENUM ('PENDING', 'COLLECTED','NOT_COMPLETED', 'SKIPPED', 'RESCHEDULED', 'FAILED') DEFAULT 'PENDING';

ALTER TABLE collection_route_item
    ADD COLUMN amount DECIMAL(12,2) DEFAULT 0;

ALTER TABLE collection_route_item
    ADD COLUMN collected_amount DECIMAL(12,2) DEFAULT 0;