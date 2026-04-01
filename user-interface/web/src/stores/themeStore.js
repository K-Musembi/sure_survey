import { create } from 'zustand'
import { persist } from 'zustand/middleware'

const useThemeStore = create(
  persist(
    (set, get) => ({
      theme: 'dark',
      toggle: () => set({ theme: get().theme === 'dark' ? 'light' : 'dark' }),
      setTheme: (theme) => set({ theme }),
    }),
    { name: 'theme-storage' }
  )
)

export default useThemeStore
