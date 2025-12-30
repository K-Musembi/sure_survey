import { create } from 'zustand'

const useErrorStore = create((set) => ({
  isOpen: false,
  title: 'Error',
  message: '',
  
  showError: (message, title = 'Error') => set({ isOpen: true, message, title }),
  hideError: () => set({ isOpen: false, message: '', title: 'Error' }),
}))

export default useErrorStore