# NOTES.md

## Summary of changes

**Bugs fixed**

1. **SQL operator precedence (critical)** — `TaskRepository.searchTasks` plus `db/queries/search_tasks.sql` and both queries in `db/oracle/task_search_package.sql`. `AND` binds tighter than `OR`, so the clause parsed as `(archived AND title-match) OR (description-match AND status-match)`: archived tasks leaked, and the status filter was silently ignored for title matches (`status=OPEN` returned 47 rows, not 31). Parenthesised the OR everywhere.
2. **Artificial latency** — removed `TaskController`'s `Thread.sleep()` block: a blank query slept 1s, blocking a Tomcat thread. Page load went from ~1.1s to <15ms.
3. **Backend validation** — invalid `status` returns 400 with a message (was 500); `page`/`pageSize` are clamped (`page=0` or negative sizes previously 500'd via `subList`).
4. **DB-side pagination** — `LIMIT/OFFSET` + `COUNT(*)` instead of fetch-all-then-`subList`.
5. **LIKE wildcards** — searching `%` or `_` matched everything; input is now escaped (`ESCAPE '\'` clause).
6. **Frontend race condition** — `useTasks` aborts superseded requests via `AbortController` (typing "api" previously showed results for "a").
7. **Error handling** — errors cleared on retry, `setLoading(false)` on failure, retry button.
8. **Page reset** — filter changes return to page 1.
9. `System.out.println` → SLF4J; `Locale.ROOT` casing.

**Improvements**

- UX: 300ms debounce, skeleton loading rows, dim-during-refresh, clear-search button, '/' search shortcut, result count, "Showing X–Y of Z", Created column, priority badges, responsive layout, dark mode.
- Quality: 7 MockMvc integration tests pin the backend fixes (`./mvnw test`, all green).

## Chose not to change

No auth, caching, or search indexes (leading-wildcard LIKE defeats them) — out of the timebox.

## Biggest remaining risk

No authentication: the API and H2 console are wide open, and search does full-table scans — fine locally, dangerous if shipped.

## Tools/AI used

Used an AI agent (Arena.ai Agent Mode, Claude) to explore the codebase, reproduce bugs with `curl`, and draft fixes; I verified everything by running the app and test suite myself.
