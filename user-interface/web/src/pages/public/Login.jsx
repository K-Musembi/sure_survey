import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { useLogin } from '../../hooks/useApi'
import FormField from '../../components/forms/FormField'
import Button from '../../components/ui/Button'
import { HiOutlineLockClosed } from 'react-icons/hi'

export default function Login() {
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname || '/dashboard'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')

  const loginMutation = useLogin()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    try {
      await loginMutation.mutateAsync({ email, password })
      navigate(from, { replace: true })
    } catch (err) {
      setError(err.response?.data?.message || 'Invalid email or password.')
    }
  }

  return (
    <div className="flex min-h-screen" style={{ backgroundColor: 'var(--bg)' }}>
      {/* Left panel — branding */}
      <div className="hidden lg:flex lg:w-1/2 items-center justify-center bg-brand p-12">
        <div className="max-w-md text-white">
          <div className="flex items-center gap-3 mb-8">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/20 text-lg font-bold">a</div>
            <span className="text-2xl font-bold">asq</span>
          </div>
          <h2 className="text-3xl font-bold leading-tight mb-4">
            Surveys that drive real decisions
          </h2>
          <p className="text-white/80 text-lg">
            AI-powered surveys with built-in rewards, referral campaigns, and decision intelligence.
          </p>
        </div>
      </div>

      {/* Right panel — form */}
      <div className="flex flex-1 items-center justify-center px-6 py-12">
        <div className="w-full max-w-sm">
          <div className="mb-8 lg:hidden flex items-center gap-2 justify-center">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-white font-bold text-sm">a</div>
            <span className="text-lg font-bold text-[var(--text)]">asq</span>
          </div>

          <h1 className="text-2xl font-bold text-[var(--text)] mb-1">Welcome back</h1>
          <p className="text-sm text-[var(--text-muted)] mb-8">Sign in to your account</p>

          {error && (
            <div className="mb-4 rounded-lg border border-[var(--error)]/30 bg-[var(--error)]/10 px-4 py-3 text-sm text-[var(--error)]">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <FormField label="Email" required>
              <input
                type="email"
                className="input-field"
                placeholder="you@example.com"
                required
                value={email}
                onChange={(e) => { setEmail(e.target.value); setError('') }}
              />
            </FormField>

            <FormField label="Password" required>
              <input
                type="password"
                className="input-field"
                placeholder="Your password"
                autoComplete="current-password"
                required
                value={password}
                onChange={(e) => { setPassword(e.target.value); setError('') }}
              />
            </FormField>

            <div className="flex items-center justify-between text-sm">
              <label className="flex items-center gap-2 text-[var(--text-muted)]">
                <input type="checkbox" className="rounded border-[var(--border)]" />
                Remember me
              </label>
              <Link to="/forgot-password" className="font-medium text-brand hover:text-brand-light">
                Forgot password?
              </Link>
            </div>

            <Button type="submit" className="w-full" loading={loginMutation.isPending}>
              <HiOutlineLockClosed className="h-4 w-4" />
              Sign In
            </Button>

            <p className="text-center text-sm text-[var(--text-muted)]">
              Don't have an account?{' '}
              <Link to="/signup" className="font-medium text-brand hover:text-brand-light">Create one</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  )
}
