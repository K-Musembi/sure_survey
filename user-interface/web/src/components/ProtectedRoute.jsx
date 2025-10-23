import { Navigate, useLocation } from 'react-router-dom'
import useAuthStore from '../stores/authStore'
import { useMe } from '../hooks/useApi'
import { Spinner } from 'flowbite-react'

const ProtectedRoute = ({ children }) => {
  const location = useLocation()
  const { isAuthenticated } = useAuthStore()
  const { isLoading, error } = useMe()

  // Show loading spinner while checking authentication
  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <Spinner size="xl" color="success" />
          <p className="mt-4 text-secondary-600">Loading...</p>
        </div>
      </div>
    )
  }

  // If there's an error or user is not authenticated, redirect to login
  if (error || !isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  return children
}

export default ProtectedRoute