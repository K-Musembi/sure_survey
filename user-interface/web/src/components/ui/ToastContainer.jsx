import { HiCheckCircle, HiExclamationCircle, HiInformationCircle, HiX } from 'react-icons/hi'
import useToastStore from '../../stores/toastStore'

const icons = {
  success: HiCheckCircle,
  error: HiExclamationCircle,
  warning: HiExclamationCircle,
  info: HiInformationCircle,
}

const colors = {
  success: 'text-[var(--success)]',
  error: 'text-[var(--error)]',
  warning: 'text-amber-500',
  info: 'text-brand',
}

export default function ToastContainer() {
  const { toasts, removeToast } = useToastStore()

  if (toasts.length === 0) return null

  return (
    <div className="fixed bottom-4 right-4 z-[100] flex flex-col gap-2 max-w-sm">
      {toasts.map((toast) => {
        const Icon = icons[toast.type] || icons.info
        return (
          <div
            key={toast.id}
            className="flex items-start gap-3 rounded-xl border border-[var(--border)] bg-[var(--surface)] p-4 shadow-lg animate-in"
          >
            <Icon className={`mt-0.5 h-5 w-5 flex-shrink-0 ${colors[toast.type]}`} />
            <p className="flex-1 text-sm text-[var(--text)]">{toast.message}</p>
            <button
              onClick={() => removeToast(toast.id)}
              className="text-[var(--text-muted)] hover:text-[var(--text)] transition-colors"
            >
              <HiX className="h-4 w-4" />
            </button>
          </div>
        )
      })}
    </div>
  )
}
