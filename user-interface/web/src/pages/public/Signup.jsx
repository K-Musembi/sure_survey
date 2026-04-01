import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useSignup } from '../../hooks/useApi'
import { authAPI } from '../../services/apiServices'
import FormField from '../../components/forms/FormField'
import Button from '../../components/ui/Button'
import { HiOutlineMail } from 'react-icons/hi'

const validatePassword = (v) => {
  if (v.length < 8) return 'Must be at least 8 characters'
  if (!/[A-Z]/.test(v)) return 'Must include an uppercase letter'
  if (!/[a-z]/.test(v)) return 'Must include a lowercase letter'
  if (!/[0-9]/.test(v)) return 'Must include a number'
  return ''
}

export default function Signup() {
  const [mode, setMode] = useState('individual')
  const [signupSuccess, setSignupSuccess] = useState(false)
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [organization, setOrganization] = useState('')
  const [department, setDepartment] = useState('')
  const [similarTenants, setSimilarTenants] = useState([])
  const [error, setError] = useState('')
  const [pwError, setPwError] = useState('')

  const signupMutation = useSignup()

  useEffect(() => {
    if (organization.length <= 2) { setSimilarTenants([]); return }
    const timer = setTimeout(async () => {
      try {
        const res = await authAPI.checkTenant(organization)
        setSimilarTenants(res.data)
      } catch { setSimilarTenants([]) }
    }, 500)
    return () => clearTimeout(timer)
  }, [organization])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    const pErr = validatePassword(password)
    if (pErr) { setPwError(pErr); return }

    try {
      const payload = { name, email, password, role: 'USER' }
      if (mode === 'enterprise') {
        payload.organization = organization
        if (department) payload.department = department
      }
      await signupMutation.mutateAsync(payload)
      setSignupSuccess(true)
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to create account. Please try again.')
    }
  }

  if (signupSuccess) {
    return (
      <div className="flex min-h-screen items-center justify-center px-6" style={{ backgroundColor: 'var(--bg)' }}>
        <div className="w-full max-w-sm text-center">
          <div className="mx-auto mb-6 flex h-14 w-14 items-center justify-center rounded-full bg-brand/10">
            <HiOutlineMail className="h-7 w-7 text-brand" />
          </div>
          <h1 className="text-2xl font-bold text-[var(--text)] mb-2">Check your email</h1>
          <p className="text-sm text-[var(--text-muted)] mb-6">
            We sent a verification link to <span className="font-medium text-[var(--text)]">{email}</span>. Click the link to activate your account.
          </p>
          <p className="text-xs text-[var(--text-muted)] mb-8">
            Didn't receive it? Check your spam folder or wait a minute and try again.
          </p>
          <Link to="/login" className="btn-brand inline-block px-6 py-2.5 text-sm">
            Go to Sign In
          </Link>
        </div>
      </div>
    )
  }

  return (
    <div className="flex min-h-screen" style={{ backgroundColor: 'var(--bg)' }}>
      {/* Left panel */}
      <div className="hidden lg:flex lg:w-1/2 items-center justify-center bg-brand p-12">
        <div className="max-w-md text-white">
          <div className="flex items-center gap-3 mb-8">
            <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-white/20 text-lg font-bold">a</div>
            <span className="text-2xl font-bold">asq</span>
          </div>
          <h2 className="text-3xl font-bold leading-tight mb-4">Start collecting insights today</h2>
          <p className="text-white/80 text-lg">
            Create your free account and launch your first survey in minutes.
          </p>
          <div className="mt-8 space-y-3 text-white/80">
            <div className="flex items-center gap-3"><span className="flex h-6 w-6 items-center justify-center rounded-full bg-white/20 text-xs font-bold">1</span> Create surveys with AI or templates</div>
            <div className="flex items-center gap-3"><span className="flex h-6 w-6 items-center justify-center rounded-full bg-white/20 text-xs font-bold">2</span> Distribute via Web, SMS, or WhatsApp</div>
            <div className="flex items-center gap-3"><span className="flex h-6 w-6 items-center justify-center rounded-full bg-white/20 text-xs font-bold">3</span> Get AI-powered insights and action plans</div>
          </div>
        </div>
      </div>

      {/* Right panel */}
      <div className="flex flex-1 items-center justify-center px-6 py-12">
        <div className="w-full max-w-md">
          <div className="mb-8 lg:hidden flex items-center gap-2 justify-center">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-white font-bold text-sm">a</div>
            <span className="text-lg font-bold text-[var(--text)]">asq</span>
          </div>

          <h1 className="text-2xl font-bold text-[var(--text)] mb-1">Create your account</h1>
          <p className="text-sm text-[var(--text-muted)] mb-6">Free to get started. No credit card required.</p>

          {/* Mode toggle */}
          <div className="mb-6 flex rounded-full border border-[var(--border)] p-1">
            {['individual', 'enterprise'].map((m) => (
              <button
                key={m}
                type="button"
                onClick={() => setMode(m)}
                className={`flex-1 rounded-full py-2 text-sm font-medium transition-colors ${mode === m ? 'bg-brand text-white' : 'text-[var(--text-muted)]'}`}
              >
                {m === 'individual' ? 'Individual' : 'Enterprise'}
              </button>
            ))}
          </div>

          {error && (
            <div className="mb-4 rounded-lg border border-[var(--error)]/30 bg-[var(--error)]/10 px-4 py-3 text-sm text-[var(--error)]">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <FormField label="Full name" required>
              <input className="input-field" placeholder="Jane Doe" required value={name} onChange={(e) => setName(e.target.value)} />
            </FormField>

            {mode === 'enterprise' && (
              <>
                <FormField label="Organization name" required>
                  <input className="input-field" placeholder="Acme LLC" required value={organization} onChange={(e) => setOrganization(e.target.value)} />
                  {similarTenants.length > 0 && (
                    <div className="mt-1 rounded-lg border border-[var(--border)] bg-[var(--surface)] overflow-hidden">
                      {similarTenants.map((t) => (
                        <div key={t} onClick={() => { setOrganization(t); setSimilarTenants([]) }}
                          className="px-3 py-2 text-sm cursor-pointer hover:bg-[var(--surface-hover)] text-[var(--text)]">
                          {t}
                        </div>
                      ))}
                    </div>
                  )}
                </FormField>
                <FormField label="Department" helper="Optional">
                  <input className="input-field" placeholder="Your department" value={department} onChange={(e) => setDepartment(e.target.value)} />
                </FormField>
              </>
            )}

            <FormField label="Email" required>
              <input className="input-field" type="email" placeholder="you@example.com" required value={email} onChange={(e) => setEmail(e.target.value)} />
            </FormField>

            <FormField label="Password" required error={pwError}>
              <input
                className={`input-field ${pwError ? 'input-error' : ''}`}
                type="password"
                placeholder="Create a password"
                required
                value={password}
                onChange={(e) => { setPassword(e.target.value); setPwError('') }}
                onBlur={(e) => { if (e.target.value) setPwError(validatePassword(e.target.value)) }}
              />
              {!pwError && <p className="mt-1 text-xs text-[var(--text-muted)]">Min 8 chars, include uppercase, lowercase and a number.</p>}
            </FormField>

            <Button type="submit" className="w-full" loading={signupMutation.isPending}>
              Create account
            </Button>

            <p className="text-center text-sm text-[var(--text-muted)]">
              Already have an account?{' '}
              <Link to="/login" className="font-medium text-brand hover:text-brand-light">Sign in</Link>
            </p>
          </form>
        </div>
      </div>
    </div>
  )
}
