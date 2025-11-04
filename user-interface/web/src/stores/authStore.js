import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'

const useAuthStore = create()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        isAuthenticated: false,
        isLoading: false,
        
        // Actions
        setUser: (user) => set({ user, isAuthenticated: !!user }, false, 'setUser'),
        
        setLoading: (isLoading) => set({ isLoading }, false, 'setLoading'),
        
        login: async (userData) => {
          set({ isLoading: true }, false, 'login/start')
          try {
            set({ 
              user: userData?.user || userData,
              isAuthenticated: true, 
              isLoading: false 
            }, false, 'login/success')
          } catch (error) {
            set({ isLoading: false }, false, 'login/error')
            throw error
          }
        },
        
        logout: () => {
          set({ 
            user: null, 
            isAuthenticated: false, 
            isLoading: false 
          }, false, 'logout')
        },
        
        // Getters
        getUser: () => get().user,
        isUserAuthenticated: () => get().isAuthenticated,
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          isAuthenticated: state.isAuthenticated,
        }),
      }
    ),
    { name: 'auth-store' }
  )
)

export default useAuthStore