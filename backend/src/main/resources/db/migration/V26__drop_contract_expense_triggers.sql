-- v26  - Drop triggers that calculate contract totals on the DB side. This is now handled by an application listener,
-- so these triggers are no longer needed and can be removed to improve performance.


-- Stop DB-side auto recalculation; app listener will handle totals
DROP TRIGGER IF EXISTS calculate_contract_totals_after_insert;
DROP TRIGGER IF EXISTS calculate_contract_totals_after_update;
DROP TRIGGER IF EXISTS calculate_contract_totals_after_delete;
