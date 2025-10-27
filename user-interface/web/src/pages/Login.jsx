import { useState } from 'react'
import { useNavigate, useLocation, Link } from 'react-router-dom'
import { Label, TextInput, Button, Checkbox } from 'flowbite-react'
import NavBar from '../components/NavBar'
import { useLogin } from '../hooks/useApi'

const Login = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const from = location.state?.from?.pathname || '/dashboard'

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [remember, setRemember] = useState(false)

  const loginMutation = useLogin()

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await loginMutation.mutateAsync({ email, password, remember })
      navigate(from, { replace: true })
    } catch (error) {
      console.error('Login error:', error.response?.data || error.message)
      // You can show error UI here
    }
  }

  return (
    <div className="min-h-screen flex flex-col">
      <NavBar />
      <div className="max-w-md w-full mx-auto mt-16 p-6">
        <h1 className="text-2xl font-bold mb-4">Sign in to SureSurvey</h1>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="email">Email</Label>
            <TextInput
              id="email"
              type="email"
              placeholder="you@example.com"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1"
            />
          </div>

          <div>
            <Label htmlFor="password">Password</Label>
            <TextInput
              id="password"
              type="password"
              placeholder="Your password"
              autocomplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
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