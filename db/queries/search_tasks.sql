-- H2-compatible task search query
-- Used by the Spring Data repository layer
--
-- Parameters:
--   :term   — search term wrapped in wildcards, e.g. '%api%'
--   :status — status filter or NULL for all statuses
--   :limit  — page size
--   :offset — rows to skip, i.e. (page - 1) * pageSize
--
-- Parentheses around the OR are load-bearing: AND binds tighter than OR,
-- so without them the archived/status filters only applied to the
-- description branch (leaking archived tasks and ignoring the status
-- filter for title matches).

SELECT *
FROM tasks
WHERE archived = FALSE
  AND (LOWER(title) LIKE :term ESCAPE '\' OR LOWER(description) LIKE :term ESCAPE '\')
  AND (:status IS NULL OR status = :status)
ORDER BY created_at DESC
LIMIT :limit OFFSET :offset;
