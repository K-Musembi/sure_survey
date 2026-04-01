import { useState } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { authAPI } from '../../services/apiServices'
import FormField from '../../components/forms/FormField'
import Button from '../../components/ui/Button'
import { HiOutlineLockClosed, HiCheckCircle } from 'react-icons/hi'

export default function ResetPassword() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''

  const [password, setPassword] = useState('')
  const [confirm, setConfirm] = useState('')
  const [done, setDone] = useState(false)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (password !== confirm) { setError('Passwords do not match.'); return }
    if (password.length < 8) { setError('Password must be at least 8 characters.'); return }
    setLoading(true)
    setError('')
    try {
      await authAPI.resetPassword({ token, newPassword: password })
      setDone(true)
    } catch (err) {
      setError(err.response?.data?.message || 'Reset failed. The link may have expired.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="flex min-h-screen items-center justify-center px-6" style={{ backgroundColor: 'var(--bg)' }}>
      <div className="w-full max-w-sm text-center">
        {done ? (
          <>
            <div className="mx-auto mb-6 flex h-14 w-14 items-center justify-center rounded-full bg-[var(--success)]/10">
              <HiCheckCircle className="h-7 w-7 text-[var(--success)]" />
            </div>
            <h1 className="text-2xl font-bold text-[var(--text)] mb-2">Password reset</h1>
            <p className="text-sm text-[var(--text-muted)] mb-8">Your password has been updated successfully.</p>
            <Link to="/login" className="btn-brand inline-block px-6 py-2.5 text-sm">Sign In</Link>
          </>
        ) : (
          <>
            <div className="mx-auto mb-6 flex h-14 w-14 items-center justify-center rounded-full bg-brand/10">
              <HiOutlineLockClosed className="h-7 w-7 text-brand" />
            </div>
            <h1 className="text-2xl font-bold text-[var(--text)] mb-2">Set new password</h1>
            <p className="text-sm text-[var(--text-muted)] mb-8">Choose a strong password for your account.</p>

            {error && (
              <div className="mb-4 rounded-lg border border-[var(--error)]/30 bg-[var(--error)]/10 px-4 py-3 text-sm text-[var(--error)]">
                {error}
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-5 text-left">
              <FormField label="New password" required>
                <input className="input-field" type="password" placeholder="Min 8 characters" required value={password} onChange={(e) => setPassword(e.target.value)} />
              </FormField>
              <FormField label="Confirm password" required>
                <input className="input-field" type="password" placeholder="Repeat password" required value={confirm} onChange={(e) => setConfirm(e.target.value)} />
              </FormField>
              <Button type="submit" className="w-full" loading={loading}>Reset Password</Button>
            </form>
          </>
        )}
      </div>
    </div>
  )
}
