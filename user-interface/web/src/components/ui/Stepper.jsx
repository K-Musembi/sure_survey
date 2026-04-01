import { HiCheck } from 'react-icons/hi'

export default function Stepper({ steps, current, className = '' }) {
  return (
    <nav className={`flex items-center gap-2 ${className}`}>
      {steps.map((step, i) => {
        const done = i < current
        const active = i === current
        return (
          <div key={i} className="flex items-center gap-2">
            <div className={`flex h-8 w-8 items-center justify-center rounded-full text-sm font-medium transition-colors
              ${done ? 'bg-brand text-white' : active ? 'bg-brand/15 text-brand border-2 border-brand' : 'bg-[var(--surface-hover)] text-[var(--text-muted)]'}`}
            >
              {done ? <HiCheck className="h-4 w-4" /> : i + 1}
            </div>
            <span className={`hidden sm:block text-sm font-medium ${active ? 'text-[var(--text)]' : 'text-[var(--text-muted)]'}`}>
              {step}
            </span>
            {i < steps.length - 1 && (
              <div className={`h-px w-8 sm:w-12 ${done ? 'bg-brand' : 'bg-[var(--border)]'}`} />
            )}
          </div>
        )
      })}
    </nav>
  )
}
