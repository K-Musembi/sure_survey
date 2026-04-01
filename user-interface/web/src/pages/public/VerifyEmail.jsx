import { useState, useEffect, useRef } from 'react'
import { useSearchParams, Link } from 'react-router-dom'
import { authAPI } from '../../services/apiServices'
import Button from '../../components/ui/Button'
import { HiCheckCircle, HiExclamationCircle } from 'react-icons/hi'

export default function VerifyEmail() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') || ''

  const [status, setStatus] = useState('loading') // loading | success | error
  const [message, setMessage] = useState('')
  const calledRef = useRef(false)

  useEffect(() => {
    if (!token) { setStatus('error'); setMessage('No verification token provided.'); return }
    if (calledRef.current) return
    calledRef.current = true

    authAPI.verifyEmail(token)
      .then(() => { setStatus('success'); setMessage('Your email has been verified.') })
      .catch((err) => { setStatus('error'); setMessage(err.response?.data?.message || 'Verification failed. The link may have expired.') })
  }, [token])

  const isSuccess = status === 'success'

  return (
    <div className="flex min-h-screen items-center justify-center px-6" style={{ backgroundColor: 'var(--bg)' }}>
      <div className="w-full max-w-sm text-center">
        {status === 'loading' ? (
          <div className="flex flex-col items-center gap-4">
            <div className="h-10 w-10 animate-spin rounded-full border-4 border-brand/30 border-t-brand" />
            <p className="text-sm text-[var(--text-muted)]">Verifying your email...</p>
          </div>
        ) : (
          <>
            <div className={`mx-auto mb-6 flex h-14 w-14 items-center justify-center rounded-full ${isSuccess ? 'bg-[var(--success)]/10' : 'bg-[var(--error)]/10'}`}>
              {isSuccess
                ? <HiCheckCircle className="h-7 w-7 text-[var(--success)]" />
                : <HiExclamationCircle className="h-7 w-7 text-[var(--error)]" />}
            </div>
            <h1 className="text-2xl font-bold text-[var(--text)] mb-2">
              {isSuccess ? 'Email verified' : 'Verification failed'}
            </h1>
            <p className="text-sm text-[var(--text-muted)] mb-8">{message}</p>
            <Link to="/login" className="btn-brand inline-block px-6 py-2.5 text-sm">
              {isSuccess ? 'Sign In' : 'Back to Sign In'}
            </Link>
          </>
        )}
      </div>
    </div>
  )
}
