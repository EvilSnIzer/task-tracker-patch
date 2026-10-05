function TableHead() {
  return (
    <thead>
      <tr>
        <th>ID</th>
        <th>Title</th>
        <th>Status</th>
        <th>Priority</th>
        <th>Assignee</th>
        <th className="col-date">Created</th>
      </tr>
    </thead>
  );
}

function formatDate(iso) {
  if (!iso) return '—';
  const d = new Date(iso);
  if (Number.isNaN(d.getTime())) return '—';
  return d.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' });
}

export default function TaskTable({ tasks, loading, error, onRetry }) {
  if (error) {
    return (
      <div className="state-message error" role="alert">
        <div className="state-icon" aria-hidden="true">⚠️</div>
        <p>Couldn’t load tasks — {error}</p>
        <button type="button" className="retry-btn" onClick={onRetry}>
          Retry
        </button>
      </div>
    );
  }

  // First load: skeleton rows instead of a bare spinner line
  if (loading && (!tasks || tasks.length === 0)) {
    return (
      <table className="task-table" aria-busy="true" aria-label="Loading tasks">
        <TableHead />
        <tbody>
          {Array.from({ length: 5 }, (_, i) => (
            <tr key={i} className="skeleton-row">
              <td colSpan={6}>
                <div className="skeleton-bar" />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    );
  }

  if (!tasks || tasks.length === 0) {
    return (
      <div className="state-message" role="status">
        <div className="state-icon" aria-hidden="true">🔍</div>
        <p>No tasks match your filters.</p>
        <p className="state-hint">Try different keywords or clear the status filter.</p>
      </div>
    );
  }

  return (
    // While refreshing, keep the current rows visible but dimmed instead of
    // blanking the table — much calmer UX than flashing "Loading...".
    <div className={`table-wrap${loading ? ' refreshing' : ''}`}>
      <table className="task-table">
        <TableHead />
        <tbody>
          {tasks.map((task) => (
            <tr key={task.id}>
              <td className="col-id">#{task.id}</td>
              <td>
                <div className="task-title">{task.title}</div>
                <div className="task-desc">{task.description}</div>
              </td>
              <td>
                <span className={`status-badge ${task.status.toLowerCase()}`}>
                  {task.status.replace('_', ' ')}
                </span>
              </td>
              <td>
                <span className={`priority-badge ${(task.priority || '').toLowerCase()}`}>
                  {task.priority}
                </span>
              </td>
              <td>{task.assignee || '—'}</td>
              <td className="col-date">{formatDate(task.createdAt)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
