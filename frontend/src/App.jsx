import { useState, useEffect } from 'react';
import SearchBar from './components/SearchBar';
import StatusFilter from './components/StatusFilter';
import TaskTable from './components/TaskTable';
import { useTasks } from './hooks/useTasks';
import { useDebouncedValue } from './hooks/useDebouncedValue';

export default function App() {
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(1);
  const [refreshNonce, setRefreshNonce] = useState(0);

  const pageSize = 10;

  // Debounce the search term so typing doesn't fire an API request per
  // keystroke; the input itself stays responsive because it uses `query`.
  const debouncedQuery = useDebouncedValue(query, 300);

  const { tasks, total, loading, error } = useTasks(
    debouncedQuery, status, page, pageSize, refreshNonce
  );

  const totalPages = Math.max(1, Math.ceil(total / pageSize));

  // Changing filters must reset to page 1. Previously, narrowing results
  // while on a high page left users on an empty page with the pagination
  // controls hidden — no way back without clearing the filter manually.
  const handleQueryChange = (value) => {
    setQuery(value);
    setPage(1);
  };

  const handleStatusChange = (value) => {
    setStatus(value);
    setPage(1);
  };

  // Power-user shortcut: '/' focuses the search box from anywhere.
  useEffect(() => {
    const onKeyDown = (e) => {
      if (e.key === '/' && !/^(INPUT|SELECT|TEXTAREA)$/.test(document.activeElement.tagName)) {
        e.preventDefault();
        document.getElementById('task-search')?.focus();
      }
    };
    window.addEventListener('keydown', onKeyDown);
    return () => window.removeEventListener('keydown', onKeyDown);
  }, []);

  const from = total === 0 ? 0 : (page - 1) * pageSize + 1;
  const to = Math.min(page * pageSize, total);

  return (
    <div className="app">
      <header className="app-header">
        <div>
          <h1>Task Tracker</h1>
          <p className="subtitle">Internal task management</p>
        </div>
        <span className="task-count" aria-live="polite">
          {total} task{total === 1 ? '' : 's'}
        </span>
      </header>

      <div className="controls">
        <SearchBar value={query} onChange={handleQueryChange} loading={loading} />
        <StatusFilter value={status} onChange={handleStatusChange} />
      </div>

      <TaskTable
        tasks={tasks}
        loading={loading}
        error={error}
        onRetry={() => setRefreshNonce((n) => n + 1)}
      />

      {!error && (
        <div className="table-footer">
          <span className="range-label">
            Showing {from}–{to} of {total}
          </span>
          {totalPages > 1 && (
            <div className="pagination">
              <button disabled={page <= 1} onClick={() => setPage((p) => p - 1)} aria-label="Previous page">
                ‹ Prev
              </button>
              <span>
                Page {page} of {totalPages}
              </span>
              <button disabled={page >= totalPages} onClick={() => setPage((p) => p + 1)} aria-label="Next page">
                Next ›
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
