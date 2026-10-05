import { useState, useEffect } from 'react';
import { fetchTasks } from '../api';

export function useTasks(query, status, page, pageSize, refreshNonce = 0) {
  const [tasks, setTasks] = useState([]);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    // Abort the in-flight request whenever inputs change (or on unmount),
    // so a slow earlier response can never overwrite a newer one.
    const controller = new AbortController();

    setLoading(true);
    setError(null); // clear the previous error, or it would stick forever

    fetchTasks({ query, status, page, pageSize, signal: controller.signal })
      .then((data) => {
        setTasks(data.items);
        setTotal(data.total);
        setLoading(false);
      })
      .catch((err) => {
        if (err.name === 'AbortError') return; // superseded by a newer request
        setError(err.message);
        setLoading(false); // previously missing: UI stayed on "Loading tasks..."
      });

    return () => controller.abort();
    // refreshNonce: bump to re-run the same request (Retry button)
  }, [query, status, page, pageSize, refreshNonce]);

  return { tasks, total, loading, error };
}
