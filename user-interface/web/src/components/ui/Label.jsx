export default function Label({ children, className = '', ...props }) {
  return (
    <label
      className={`block text-sm font-medium text-[var(--text)] mb-1 ${className}`}
      {...props}
    >
      {children}
    </label>
  )
}
