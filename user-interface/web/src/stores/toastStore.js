import { create } from 'zustand'

let toastId = 0

const useToastStore = create((set, get) => ({
  toasts: [],

  addToast: ({ type = 'info', message, duration = 4000 }) => {
    const id = ++toastId
    set((s) => ({ toasts: [...s.toasts, { id, type, message }] }))
    if (duration > 0) {
      setTimeout(() => get().removeToast(id), duration)
    }
    return id
  },

  removeToast: (id) =>
    set((s) => ({ toasts: s.toasts.filter((t) => t.id !== id) })),

  success: (message) => get().addToast({ type: 'success', message }),
  error: (message) => get().addToast({ type: 'error', message, duration: 6000 }),
  info: (message) => get().addToast({ type: 'info', message }),
  warning: (message) => get().addToast({ type: 'warning', message }),
}))

export default useToastStore
