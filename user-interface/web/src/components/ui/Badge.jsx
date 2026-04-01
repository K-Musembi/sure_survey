const colorMap = {
  brand: 'bg-brand/15 text-brand',
  accent: 'bg-amber-500/15 text-amber-600 dark:text-amber-400',
  success: 'bg-emerald-500/15 text-emerald-600 dark:text-emerald-400',
  error: 'bg-red-500/15 text-red-600 dark:text-red-400',
  gray: 'bg-[var(--surface-hover)] text-[var(--text-muted)]',
  blue: 'bg-blue-500/15 text-blue-600 dark:text-blue-400',
  purple: 'bg-purple-500/15 text-purple-600 dark:text-purple-400',
}

export default function Badge({ color = 'gray', className = '', children }) {
  return (
    <span className={`inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium ${colorMap[color] || colorMap.gray} ${className}`}>
      {children}
    </span>
  )
}
