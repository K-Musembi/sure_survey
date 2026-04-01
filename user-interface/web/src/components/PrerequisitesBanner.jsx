import { useState } from 'react'
import { HiCheckCircle, HiOutlineChevronDown, HiOutlineChevronUp } from 'react-icons/hi'
import Button from './ui/Button'

export default function PrerequisitesBanner({ checks = [] }) {
  const [collapsed, setCollapsed] = useState(false)

  const allPassed = checks.every(c => c.passed)
  const passedCount = checks.filter(c => c.passed).length

  if (allPassed) return null

  return (
    <div className="rounded-lg border border-[var(--border)] bg-[var(--surface)] overflow-hidden">
      <button
        onClick={() => setCollapsed(!collapsed)}
        className="w-full flex items-center justify-between px-4 py-3 hover:bg-[var(--surface-hover)] transition-colors"
      >
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-[var(--text)]">
            Setup checklist
          </span>
          <span className="text-xs text-[var(--text-muted)]">
            {passedCount}/{checks.length} complete
          </span>
        </div>
        {collapsed
          ? <HiOutlineChevronDown className="h-4 w-4 text-[var(--text-muted)]" />
          : <HiOutlineChevronUp className="h-4 w-4 text-[var(--text-muted)]" />}
      </button>

      {!collapsed && (
        <div className="px-4 pb-4 space-y-2 border-t border-[var(--border)] pt-3">
          {checks.map((check, i) => (
            <div key={i} className="flex items-center justify-between gap-3">
              <div className="flex items-center gap-2">
                <HiCheckCircle className={`h-5 w-5 flex-shrink-0 ${check.passed ? 'text-[var(--success)]' : 'text-[var(--border)]'}`} />
                <span className={`text-sm ${check.passed ? 'text-[var(--text-muted)] line-through' : 'text-[var(--text)]'}`}>
                  {check.label}
                </span>
              </div>
              {!check.passed && check.action && (
                <Button size="xs" variant="secondary" onClick={check.action}>
                  {check.actionLabel || 'Set up'}
                </Button>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
