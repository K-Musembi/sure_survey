import { HiOutlineInbox } from 'react-icons/hi'
import Button from './Button'

export default function EmptyState({ icon, title, description, actionLabel, onAction }) {
  const IconComp = icon || HiOutlineInbox
  return (
    <div className="flex flex-col items-center justify-center py-16 text-center">
      <div className="mb-4 rounded-full p-4 bg-[var(--surface-hover)]">
        <IconComp className="h-8 w-8 text-[var(--text-muted)]" />
      </div>
      <h3 className="text-lg font-semibold text-[var(--text)]">{title}</h3>
      {description && (
        <p className="mt-1 max-w-sm text-sm text-[var(--text-muted)]">{description}</p>
      )}
      {actionLabel && onAction && (
        <Button className="mt-5" onClick={onAction}>{actionLabel}</Button>
      )}
    </div>
  )
}
