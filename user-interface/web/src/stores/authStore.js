import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'

const useAuthStore = create()(
  devtools(
    persist(
      (set, get) => ({
        user: null,
        token: null,
        isAuthenticated: false,
        isLoading: false,
        
        // Actions
        setUser: (user) => set({ user, isAuthenticated: !!user }, false, 'setUser'),
        
        setLoading: (isLoading) => set({ isLoading }, false, 'setLoading'),
        
        login: async (userData) => {
          set({ isLoading: true }, false, 'login/start')
          try {
            // Attempt to find the token in common fields
            const token = userData?.token || 
                          userData?.accessToken || 
                          userData?.access_token || 
                          userData?.jwt
            
            // Determine user object: if userData.user exists use it, otherwise assume userData is the user object (excluding the token if possible, but keeping it is harmless)
            const user = userData?.user || userData
            
            if (!token) {
              console.log('No token found in response body. Assuming cookie-based authentication.')
            }
            
            set({ 
              user: user,
              token: token, // Might be null/undefined if using cookies
              isAuthenticated: !!user, 
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
            token: null,
            isAuthenticated: false, 
            isLoading: false 
          }, false, 'logout')
        },
        
        // Getters
        getUser: () => get().user,
        getToken: () => get().token,
        isUserAuthenticated: () => get().isAuthenticated,
        isAdmin: () => get().user?.role === 'SYSTEM_ADMIN' || get().user?.roles?.includes('SYSTEM_ADMIN'), // Handle both string or list scenarios
      }),
      {
        name: 'auth-storage',
        partialize: (state) => ({
          user: state.user,
          // token is NOT persisted to avoid stale token issues. 
          // Browser cookies are used for session persistence.
          isAuthenticated: state.isAuthenticated,
        }),
      }
    ),
    { name: 'auth-store' }
  )
)

export default useAuthStore