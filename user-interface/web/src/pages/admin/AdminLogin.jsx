import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Label, TextInput, Button, Alert } from 'flowbite-react'
import { adminAPI } from '../../services/apiServices'
import useAuthStore from '../../stores/authStore'
import { HiExclamationCircle } from 'react-icons/hi'

const AdminLogin = () => {
  const navigate = useNavigate()
  const { login } = useAuthStore()

  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErrorMessage('')
    setIsLoading(true)

    try {
      const response = await adminAPI.login({ email, password })
      // Admin login response might be same as user login response (token + user)
      // Ensure we store it correctly
      await login(response.data)
      navigate('/admin/dashboard')
    } catch (error) {
      console.error('Admin Login error:', error.response?.data || error.message)
      setErrorMessage(error.response?.data?.message || 'Invalid admin credentials.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex flex-col items-center justify-center bg-gray-900">
      <div className="max-w-md w-full p-8 bg-white rounded-lg shadow-xl">
        <h1 className="text-2xl font-bold mb-2 text-center text-gray-900">System Admin</h1>
        <p className="text-gray-500 text-center mb-6">Restricted Access</p>
        
        {errorMessage && (
          <Alert color="failure" icon={HiExclamationCircle} className="mb-4">
            {errorMessage}
          </Alert>
        )}
        
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="email">Admin Email</Label>
            <TextInput
              id="email"
              type="email"
              placeholder="admin@suresurvey.com"
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
              placeholder="••••••••"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => { setPassword(e.target.value); setErrorMessage(''); }}
              className="mt-1"
            />
          </div>

          <Button type="submit" className="w-full bg-gray-800 hover:bg-gray-700" disabled={isLoading}>
            {isLoading ? 'Authenticating...' : 'Access Dashboard'}
          </Button>
        </form>
      </div>
    </div>
  )
}

export default AdminLogin
