import { HiArrowUp, HiArrowDown } from 'react-icons/hi'

export default function StatCard({ label, value, delta, icon: Icon, className = '' }) {
  const isPositive = delta > 0
  const isNegative = delta < 0

  return (
    <div className={`card ${className}`}>
      <div className="flex items-start justify-between">
        <div>
          <p className="text-sm font-medium text-[var(--text-muted)]">{label}</p>
          <p className="mt-1 text-2xl font-bold text-[var(--text)]">{value}</p>
        </div>
        {Icon && (
          <div className="rounded-lg p-2.5 bg-brand/10">
            <Icon className="h-5 w-5 text-brand" />
          </div>
        )}
      </div>
      {delta !== undefined && delta !== null && (
        <div className="mt-3 flex items-center gap-1 text-xs font-medium">
          {isPositive && <HiArrowUp className="h-3.5 w-3.5 text-[var(--success)]" />}
          {isNegative && <HiArrowDown className="h-3.5 w-3.5 text-[var(--error)]" />}
          <span className={isPositive ? 'text-[var(--success)]' : isNegative ? 'text-[var(--error)]' : 'text-[var(--text-muted)]'}>
            {Math.abs(delta)}%
          </span>
          <span className="text-[var(--text-muted)]">vs last period</span>
        </div>
      )}
    </div>
  )
}
