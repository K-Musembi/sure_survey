import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Label, TextInput, Button, Radio } from 'flowbite-react'
import NavBar from '../components/NavBar'
import { useSignup } from '../hooks/useApi'

const Signup = () => {
  const navigate = useNavigate()
  const [mode, setMode] = useState('individual') // or 'enterprise'
  const [name, setName] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [tenantName, setTenantName] = useState('')

  const signupMutation = useSignup()

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      const payload = { name, email, password }
      if (mode === 'enterprise') payload.tenantName = tenantName
      await signupMutation.mutateAsync(payload)
      navigate('/login')
    } catch (error) {
      console.error('Signup error:', error.response?.data || error.message)
    }
  }

  return (
    <div className="min-h-screen flex flex-col">
      <NavBar />
      <div className="max-w-lg w-full mx-auto mt-16 p-6">
        <h1 className="text-2xl font-bold mb-4">Create an account</h1>

        <div className="mb-4">
          <div className="flex items-center gap-4">
            <div className="flex items-center gap-2">
              <Radio id="individual" name="mode" value="individual" checked={mode === 'individual'} onChange={() => setMode('individual')} />
              <Label htmlFor="individual">Individual</Label>
            </div>
            <div className="flex items-center gap-2">
              <Radio id="enterprise" name="mode" value="enterprise" checked={mode === 'enterprise'} onChange={() => setMode('enterprise')} />
              <Label htmlFor="enterprise">Enterprise</Label>
            </div>
          </div>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="name">Full name</Label>
            <TextInput id="name" placeholder="Jane Doe" required value={name} onChange={(e) => setName(e.target.value)} />
          </div>

          {mode === 'enterprise' && (
            <div>
              <Label htmlFor="tenant">Organization name</Label>
              <TextInput id="tenant" placeholder="Acme LLC" required value={tenantName} onChange={(e) => setTenantName(e.target.value)} />
            </div>
          )}

          <div>
            <Label htmlFor="email">Email</Label>
            <TextInput id="email" placeholder="you@example.com" required value={email} onChange={(e) => setEmail(e.target.value)} />
          </div>

          <div>
            <Label htmlFor="password">Password</Label>
            <TextInput id="password" type="password" placeholder="Create a password" required value={password} onChange={(e) => setPassword(e.target.value)} />
            <p className="text-xs text-gray-500 mt-1">Password must be at least 8 characters, include uppercase, lowercase and a number.</p>
          </div>

          <div>
            <Button type="submit" className="w-full">{signupMutation.isLoading ? 'Creating account...' : 'Create account'}</Button>
          </div>

          <p className="text-sm text-gray-600 text-center">
            Already have an account? <Link to="/login" className="text-primary-600 font-medium">Sign in</Link>
          </p>
        </form>
      </div>
    </div>
  )
}

export default Signup