import { HiOutlineExclamationCircle, HiOutlineCheckCircle, HiOutlineInformationCircle, HiOutlineX } from 'react-icons/hi'

const styles = {
  info:    { bg: 'bg-blue-500/10', border: 'border-blue-500/20', text: 'text-blue-700 dark:text-blue-400', icon: HiOutlineInformationCircle },
  success: { bg: 'bg-[var(--success)]/10', border: 'border-[var(--success)]/20', text: 'text-[var(--success)]', icon: HiOutlineCheckCircle },
  warning: { bg: 'bg-amber-500/10', border: 'border-amber-500/20', text: 'text-amber-700 dark:text-amber-400', icon: HiOutlineExclamationCircle },
  failure: { bg: 'bg-[var(--error)]/10', border: 'border-[var(--error)]/20', text: 'text-[var(--error)]', icon: HiOutlineExclamationCircle },
}

export default function Alert({ color = 'info', icon: CustomIcon, children, onDismiss, className = '' }) {
  const s = styles[color] || styles.info
  const Icon = CustomIcon || s.icon

  return (
    <div className={`flex items-start gap-3 rounded-lg border p-4 text-sm ${s.bg} ${s.border} ${s.text} ${className}`} role="alert">
      <Icon className="h-5 w-5 flex-shrink-0 mt-0.5" />
      <div className="flex-1">{children}</div>
      {onDismiss && (
        <button onClick={onDismiss} className="flex-shrink-0 opacity-70 hover:opacity-100 transition-opacity">
          <HiOutlineX className="h-4 w-4" />
        </button>
      )}
    </div>
  )
}
