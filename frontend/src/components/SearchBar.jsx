export default function SearchBar({ value, onChange, loading }) {
  return (
    <div className="search-wrapper">
      <svg
        className="search-icon"
        width="16"
        height="16"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        strokeWidth="2"
        strokeLinecap="round"
        aria-hidden="true"
      >
        <circle cx="11" cy="11" r="7" />
        <line x1="16.5" y1="16.5" x2="21" y2="21" />
      </svg>
      <input
        id="task-search"
        type="text"
        className="search-input"
        placeholder="Search tasks…  (press /)"
        value={value}
        onChange={(e) => onChange(e.target.value)}
        aria-label="Search tasks"
      />
      {value && (
        <button type="button" className="clear-btn" onClick={() => onChange('')} aria-label="Clear search">
          ×
        </button>
      )}
      {loading && <span className={`search-spinner${value ? ' with-clear' : ''}`} aria-hidden="true" />}
    </div>
  );
}
