import { lazy, Suspense } from 'react'
import { Routes, Route, Navigate } from 'react-router-dom'
import useAuthStore from './stores/authStore'
import AppShell from './components/layout/AppShell'
import PublicLayout from './components/layout/PublicLayout'
import ToastContainer from './components/ui/ToastContainer'

// Lazy-loaded pages
const Home = lazy(() => import('./pages/public/Home'))
const Login = lazy(() => import('./pages/public/Login'))
const Signup = lazy(() => import('./pages/public/Signup'))
const ForgotPassword = lazy(() => import('./pages/public/ForgotPassword'))
const ResetPassword = lazy(() => import('./pages/public/ResetPassword'))
const VerifyEmail = lazy(() => import('./pages/public/VerifyEmail'))
const SurveySession = lazy(() => import('./pages/SurveySession'))

const Surveys = lazy(() => import('./pages/dashboard/Surveys'))
const SurveyBuilder = lazy(() => import('./pages/dashboard/SurveyBuilder'))
const SurveyDetail = lazy(() => import('./pages/dashboard/SurveyDetail'))
const Analytics = lazy(() => import('./pages/dashboard/Analytics'))
const Intelligence = lazy(() => import('./pages/dashboard/Intelligence'))
const Referrals = lazy(() => import('./pages/dashboard/Referrals'))
const Billing = lazy(() => import('./pages/dashboard/Billing'))
const Profile = lazy(() => import('./pages/dashboard/Profile'))
const Settings = lazy(() => import('./pages/dashboard/Settings'))

const Integrations = lazy(() => import('./pages/dashboard/Integrations'))
const Competitions = lazy(() => import('./pages/dashboard/Competitions'))
const CompetitionSetup = lazy(() => import('./pages/dashboard/CompetitionSetup'))
const AdminLogin = lazy(() => import('./pages/admin/AdminLogin'))
const AdminDashboard = lazy(() => import('./pages/admin/AdminDashboard'))

function PageLoader() {
  return (
    <div className="flex min-h-[50vh] items-center justify-center">
      <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand/30 border-t-brand" />
    </div>
  )
}

function ProtectedRoute({ children }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  return isAuthenticated ? children : <Navigate to="/login" replace />
}

function PublicRoute({ children }) {
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)
  return isAuthenticated ? <Navigate to="/dashboard" replace /> : children
}

function AdminRoute({ children }) {
  const { isAuthenticated, isAdmin } = useAuthStore()
  if (!isAuthenticated) return <Navigate to="/admin" replace />
  if (!isAdmin()) return <Navigate to="/dashboard" replace />
  return children
}

export default function App() {
  return (
    <>
      <Suspense fallback={<PageLoader />}>
        <Routes>
          {/* Public pages with layout */}
          <Route path="/" element={<PublicRoute><PublicLayout><Home /></PublicLayout></PublicRoute>} />

          {/* Auth pages (no layout) */}
          <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
          <Route path="/signup" element={<PublicRoute><Signup /></PublicRoute>} />
          <Route path="/forgot-password" element={<ForgotPassword />} />
          <Route path="/reset-password" element={<ResetPassword />} />
          <Route path="/verify-email" element={<VerifyEmail />} />

          {/* Public survey */}
          <Route path="/survey/:surveyId" element={<SurveySession />} />
          <Route path="/s/:shortCode" element={<SurveySession />} />

          {/* Dashboard (protected) */}
          <Route path="/dashboard" element={<ProtectedRoute><AppShell><Surveys /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/new" element={<ProtectedRoute><AppShell><SurveyBuilder /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/survey/:id" element={<ProtectedRoute><AppShell><SurveyDetail /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/analytics" element={<ProtectedRoute><AppShell><Analytics /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/intelligence" element={<ProtectedRoute><AppShell><Intelligence /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/integrations" element={<ProtectedRoute><AppShell><Integrations /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/competitions" element={<ProtectedRoute><AppShell><Competitions /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/competitions/setup/:surveyId" element={<ProtectedRoute><AppShell><CompetitionSetup /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/referrals" element={<ProtectedRoute><AppShell><Referrals /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/billing" element={<ProtectedRoute><AppShell><Billing /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/profile" element={<ProtectedRoute><AppShell><Profile /></AppShell></ProtectedRoute>} />
          <Route path="/dashboard/settings" element={<ProtectedRoute><AppShell><Settings /></AppShell></ProtectedRoute>} />

          {/* Legacy redirects */}
          <Route path="/survey-builder" element={<Navigate to="/dashboard/new" replace />} />
          <Route path="/subscriptions" element={<Navigate to="/dashboard/billing" replace />} />
          <Route path="/profile" element={<Navigate to="/dashboard/profile" replace />} />
          <Route path="/settings" element={<Navigate to="/dashboard/settings" replace />} />

          {/* Admin */}
          <Route path="/admin" element={<AdminLogin />} />
          <Route path="/admin/dashboard" element={<AdminRoute><AdminDashboard /></AdminRoute>} />

          {/* Catch-all */}
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </Suspense>
      <ToastContainer />
    </>
  )
}
