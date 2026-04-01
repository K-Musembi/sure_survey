function Base({ className = '' }) {
  return (
    <div className={`animate-pulse rounded-lg bg-[var(--border)] ${className}`} />
  )
}

export function SkeletonText({ lines = 3, className = '' }) {
  return (
    <div className={`space-y-2.5 ${className}`}>
      {Array.from({ length: lines }).map((_, i) => (
        <Base key={i} className={`h-4 ${i === lines - 1 ? 'w-3/4' : 'w-full'}`} />
      ))}
    </div>
  )
}

export function SkeletonCard({ className = '' }) {
  return (
    <div className={`card space-y-4 ${className}`}>
      <Base className="h-5 w-2/5" />
      <Base className="h-4 w-full" />
      <Base className="h-4 w-4/5" />
      <div className="flex gap-2 pt-2">
        <Base className="h-6 w-16 rounded-full" />
        <Base className="h-6 w-20 rounded-full" />
      </div>
    </div>
  )
}

export function SkeletonTableRow({ cols = 4 }) {
  return (
    <tr>
      {Array.from({ length: cols }).map((_, i) => (
        <td key={i} className="px-4 py-3">
          <Base className="h-4 w-full" />
        </td>
      ))}
    </tr>
  )
}

export default function Skeleton({ variant = 'text', ...props }) {
  switch (variant) {
    case 'card': return <SkeletonCard {...props} />
    case 'table-row': return <SkeletonTableRow {...props} />
    default: return <SkeletonText {...props} />
  }
}
