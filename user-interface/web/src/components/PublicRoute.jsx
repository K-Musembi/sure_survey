import { Navigate } from 'react-router-dom'
import useAuthStore from '../stores/authStore'

const PublicRoute = ({ children }) => {
  const { isAuthenticated } = useAuthStore()

  // If user is authenticated, redirect to dashboard
  if (isAuthenticated) {
    return <Navigate to="/dashboard" replace />
  }

  return children
}

export default PublicRoute