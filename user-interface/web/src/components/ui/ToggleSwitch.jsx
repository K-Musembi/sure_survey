export default function ToggleSwitch({ checked, onChange, label, disabled, className = '' }) {
  return (
    <label className={`inline-flex items-center gap-3 cursor-pointer select-none ${disabled ? 'opacity-50 pointer-events-none' : ''} ${className}`}>
      <button
        type="button"
        role="switch"
        aria-checked={checked}
        onClick={() => onChange?.(!checked)}
        disabled={disabled}
        className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-200 focus:outline-none focus:ring-2 focus:ring-brand/30
          ${checked ? 'bg-brand' : 'bg-[var(--border)]'}`}
      >
        <span
          className={`inline-block h-4 w-4 rounded-full bg-white transition-transform duration-200 shadow-sm
            ${checked ? 'translate-x-6' : 'translate-x-1'}`}
        />
      </button>
      {label && <span className="text-sm text-[var(--text)]">{label}</span>}
    </label>
  )
}
