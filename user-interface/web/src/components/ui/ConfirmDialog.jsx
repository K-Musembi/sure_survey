import Modal from './Modal'
import Button from './Button'
import { HiExclamation } from 'react-icons/hi'

export default function ConfirmDialog({ open, onClose, onConfirm, title, message, confirmLabel = 'Confirm', variant = 'danger', loading }) {
  return (
    <Modal open={open} onClose={onClose} size="sm">
      <div className="flex flex-col items-center text-center">
        <div className="mb-4 rounded-full p-3 bg-red-500/10">
          <HiExclamation className="h-6 w-6 text-[var(--error)]" />
        </div>
        <h3 className="text-lg font-semibold text-[var(--text)]">{title}</h3>
        <p className="mt-2 text-sm text-[var(--text-muted)]">{message}</p>
        <div className="mt-6 flex gap-3">
          <Button variant="secondary" onClick={onClose}>Cancel</Button>
          <Button variant={variant} onClick={onConfirm} loading={loading}>{confirmLabel}</Button>
        </div>
      </div>
    </Modal>
  )
}
