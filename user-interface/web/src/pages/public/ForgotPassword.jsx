import { useState } from 'react'
import { Link } from 'react-router-dom'
import { authAPI } from '../../services/apiServices'
import FormField from '../../components/forms/FormField'
import Button from '../../components/ui/Button'
import { HiOutlineMail } from 'react-icons/hi'

export default function ForgotPassword() {
  const [email, setEmail] = useState('')
  const [sent, setSent] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    setError('')
    try {
      await authAPI.forgotPassword(email)
      setSent(true)
    } catch (err) {
      setError(err.response?.data?.message || 'Something went wrong. Please try again.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-6" style={{ backgroundColor: 'var(--bg)' }}>
      <div className="w-full max-w-sm text-center">
        <div className="mx-auto mb-6 flex h-14 w-14 items-center justify-center rounded-full bg-brand/10">
          <HiOutlineMail className="h-7 w-7 text-brand" />
        </div>

        {sent ? (
          <>
            <h1 className="text-2xl font-bold text-[var(--text)] mb-2">Check your email</h1>
            <p className="text-sm text-[var(--text-muted)] mb-8">
              We sent a password reset link to <strong className="text-[var(--text)]">{email}</strong>.
            </p>
            <Link to="/login" className="btn-brand inline-block px-6 py-2.5 text-sm">Back to Sign In</Link>
          </>
        ) : (
          <>
            <h1 className="text-2xl font-bold text-[var(--text)] mb-2">Forgot password?</h1>
            <p className="text-sm text-[var(--text-muted)] mb-8">
              Enter your email and we'll send you a reset link.
            </p>

            {error && (
              <div className="mb-4 rounded-lg border border-[var(--error)]/30 bg-[var(--error)]/10 px-4 py-3 text-sm text-[var(--error)]">
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5 text-left">
              <FormField label="Email" required>
                <input className="input-field" type="email" placeholder="you@example.com" required value={email} onChange={(e) => setEmail(e.target.value)} />
              </FormField>
              <Button type="submit" className="w-full" loading={loading}>Send Reset Link</Button>
            </form>
            <p className="mt-6 text-sm text-[var(--text-muted)]">
              <Link to="/login" className="font-medium text-brand hover:text-brand-light">Back to Sign In</Link>
            </p>
          </>
        )}
      </div>
    </div>
  )
}
