import { Routes, Route, Navigate } from 'react-router-dom'
import ProtectedRoute from './components/ProtectedRoute'
import PublicRoute from './components/PublicRoute'
import NavBar from './components/NavBar'
import Home from './pages/Home'
import Login from './pages/Login'
import Signup from './pages/Signup'
import Dashboard from './pages/Dashboard'
import Profile from './pages/Profile'
import SurveyBuilder from './pages/SurveyBuilder'
import SurveySession from './pages/SurveySession'
import DashboardLayout from './components/DashboardLayout'
import DashboardAnalytics from './pages/DashboardAnalytics'
import Settings from './pages/Settings'
import Subscriptions from './pages/Subscriptions'
import useAuthStore from './stores/authStore'

export default function App() {

  const AppProtectedRoute = ({ children }) => {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
    return isAuthenticated ? children : <Navigate to="/login" replace />
  }

  const AppPublicRoute = ({ children }) => {
    const isAuthenticated = useAuthStore((state) => state.isAuthenticated)
    return isAuthenticated ? <Navigate to="/dashboard" replace /> : children
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <Routes>
        {/* Public Routes */}
        <Route path="/" element={
          <AppPublicRoute>
            <Home />
          </AppPublicRoute>
        } />
        <Route path="/login" element={
          <AppPublicRoute>
            <Login />
          </AppPublicRoute>
        } />
        <Route path="/signup" element={
          <AppPublicRoute>
            <Signup />
          </AppPublicRoute>
        } />

        {/* Protected Routes using DashboardLayout */}
        <Route path="/dashboard" element={<AppProtectedRoute><DashboardLayout><Dashboard /></DashboardLayout></AppProtectedRoute>} />
        <Route path="/dashboard/analytics" element={<AppProtectedRoute><DashboardLayout><DashboardAnalytics /></DashboardLayout></AppProtectedRoute>} />
        <Route path="/profile" element={<AppProtectedRoute><DashboardLayout><Profile /></DashboardLayout></AppProtectedRoute>} />
        <Route path="/survey-builder" element={<AppProtectedRoute><DashboardLayout><SurveyBuilder /></DashboardLayout></AppProtectedRoute>} />
        <Route path="/settings" element={<AppProtectedRoute><DashboardLayout><Settings /></DashboardLayout></AppProtectedRoute>} />
        <Route path="/subscriptions" element={<AppProtectedRoute><DashboardLayout><Subscriptions /></DashboardLayout></AppProtectedRoute>} />

        {/* Public Survey Routes */}
        <Route path="/survey/:surveyId" element={<SurveySession />} />
        <Route path="/s/:shortCode" element={<SurveySession />} />

        {/* Catch all - redirect to home */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  )
}
