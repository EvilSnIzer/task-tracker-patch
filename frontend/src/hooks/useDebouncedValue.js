import { useState, useEffect } from 'react';

// Returns a copy of `value` that only updates after `delayMs` of quiet time.
// Used to keep the search input responsive while avoiding one API request
// per keystroke.
export function useDebouncedValue(value, delayMs = 300) {
  const [debounced, setDebounced] = useState(value);

  useEffect(() => {
    const id = setTimeout(() => setDebounced(value), delayMs);
    return () => clearTimeout(id);
  }, [value, delayMs]);

  return debounced;
}
