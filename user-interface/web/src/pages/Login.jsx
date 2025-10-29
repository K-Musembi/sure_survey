import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { Label, TextInput, Button, Checkbox, Alert } from 'flowbite-react'
import NavBar from '../components/NavBar'
import { useLogin } from '../hooks/useApi'
import { HiExclamationCircle } from 'react-icons/hi'

const Login = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname || '/dashboard'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const loginMutation = useLogin()

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErrorMessage('') // Clear previous errors on new submission
    try {
      await loginMutation.mutateAsync({ email, password, remember })
      navigate(from, { replace: true })
    } catch (error) {
      console.error('Login error:', error.response?.data || error.message)
      setErrorMessage('Invalid email or password.')
    }
  }

  return (
    <div className="min-h-screen flex flex-col">
      <NavBar />
      <div className="max-w-md w-full mx-auto mt-16 p-6">
        <h1 className="text-2xl font-bold mb-4 text-center">Sure Survey</h1>
        {errorMessage && (
          <Alert color="failure" icon={HiExclamationCircle} className="mb-4">
            {errorMessage}
          </Alert>
        )}
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="email">Email</Label>
            <TextInput
              id="email"
              type="email"
              placeholder="you@example.com"
              required
              value={email}
              onChange={(e) => { setEmail(e.target.value); setErrorMessage(''); }}
              className="mt-1"
            />
          </div>

          <div>
            <Label htmlFor="password">Password</Label>
            <TextInput
              id="password"
              type="password"
              placeholder="Your password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => { setPassword(e.target.value); setErrorMessage(''); }}
              className="mt-1"
            />
          </div>

          <div className="flex items-center justify-between">
            <div className="flex items-center gap-2">
              <Checkbox
                id="remember"
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
              />
              <Label htmlFor="remember">Remember me</Label>
            </div>

            <Link to="/forgot" className="text-sm text-primary-600">Forgot password?</Link>
          </div>

          <div>
            <Button type="submit" className="w-full">
              {loginMutation.isLoading ? 'Signing in...' : 'Sign In'}
            </Button>
          </div>

          <p className="text-sm text-gray-600 text-center">
            Don't have an account? <Link to="/signup" className="text-primary-600 font-medium">Create one</Link>
          </p>
        </form>
      </div>
    </div>
  )
}

export default Login