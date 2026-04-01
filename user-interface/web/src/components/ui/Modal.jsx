import { useEffect, useRef } from 'react'
import { HiX } from 'react-icons/hi'

export default function Modal({ open, onClose, title, size = 'md', children }) {
  const overlayRef = useRef(null)

  useEffect(() => {
    if (open) {
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = ''
    }
    return () => { document.body.style.overflow = '' }
  }, [open])

  useEffect(() => {
    const handleKey = (e) => { if (e.key === 'Escape') onClose?.() }
    if (open) window.addEventListener('keydown', handleKey)
    return () => window.removeEventListener('keydown', handleKey)
  }, [open, onClose])

  if (!open) return null

  const sizeClasses = {
    sm: 'max-w-md',
    md: 'max-w-lg',
    lg: 'max-w-2xl',
    xl: 'max-w-4xl',
  }

  return (
    <div
      ref={overlayRef}
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/50 backdrop-blur-sm"
      onClick={(e) => e.target === overlayRef.current && onClose?.()}
    >
      <div className={`w-full ${sizeClasses[size]} rounded-xl border border-[var(--border)] bg-[var(--surface)] shadow-2xl animate-in`}>
        {title && (
          <div className="flex items-center justify-between border-b border-[var(--border)] px-6 py-4">
            <h3 className="text-lg font-semibold text-[var(--text)]">{title}</h3>
            <button
              onClick={onClose}
              className="rounded-lg p-1.5 text-[var(--text-muted)] hover:bg-[var(--surface-hover)] hover:text-[var(--text)] transition-colors"
            >
              <HiX className="h-5 w-5" />
            </button>
          </div>
        )}
        <div className="p-6">{children}</div>
      </div>
    </div>
  )
}
