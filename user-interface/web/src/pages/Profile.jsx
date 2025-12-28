import { useState, useEffect } from 'react'
import { Card, Label, TextInput, Button, Alert, Spinner } from 'flowbite-react'
import useAuthStore from '../stores/authStore'
import { userAPI, billingAPI } from '../services/apiServices'
import { HiUser, HiOfficeBuilding, HiCheck, HiExclamationCircle } from 'react-icons/hi'

const Profile = () => {
  const { user, setUser } = useAuthStore()
  
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    department: '',
    region: '',
    branch: ''
  })
  
  const [subscription, setSubscription] = useState(null)
  const [isLoading, setIsLoading] = useState(false)
  const [isSaving, setIsSaving] = useState(false)
  const [message, setMessage] = useState(null) // { type: 'success' | 'failure', text: '' }

  // Load latest data
  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true)
      try {
        if (user?.id) {
          const [userRes, subRes] = await Promise.allSettled([
            userAPI.getUser(user.id),
            billingAPI.getSubscription()
          ])

          if (userRes.status === 'fulfilled') {
            const u = userRes.value.data
            setFormData({
              name: u.name || '',
              email: u.email || '',
              department: u.department || '',
              region: u.region || '',
              branch: u.branch || ''
            })
            // Update store if changed
            setUser({ ...user, ...u })
          }
          
          if (subRes.status === 'fulfilled') {
            setSubscription(subRes.value.data)
          }
        }
      } catch (error) {
        console.error('Failed to load profile', error)
        setMessage({ type: 'failure', text: 'Could not load latest profile data.' })
      } finally {
        setIsLoading(false)
      }
    }
    fetchData()
  }, [user?.id, setUser])

  const handleUpdate = async (e) => {
    e.preventDefault()
    setIsSaving(true)
    setMessage(null)
    
    try {
      const response = await userAPI.updateUser(user.id, {
        ...formData,
        tenantId: user.tenantId // Preserve tenant
      })
      setUser({ ...user, ...response.data })
      setMessage({ type: 'success', text: 'Profile updated successfully!' })
    } catch (error) {
      console.error('Update failed', error)
      setMessage({ 
        type: 'failure', 
        text: error.response?.data?.message || 'Failed to update profile.' 
      })
    } finally {
      setIsSaving(false)
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner size="xl" />
      </div>
    )
  }

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">Profile Settings</h1>

      {message && (
        <Alert color={message.type} icon={message.type === 'success' ? HiCheck : HiExclamationCircle} onDismiss={() => setMessage(null)}>
          {message.text}
        </Alert>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: User Info Form */}
        <div className="lg:col-span-2">
          <Card>
            <div className="flex items-center mb-4">
              <div className="p-2 bg-primary-100 rounded-lg mr-3">
                <HiUser className="w-6 h-6 text-primary-600" />
              </div>
              <h3 className="text-xl font-bold text-gray-900">Personal Details</h3>
            </div>
            
            <form onSubmit={handleUpdate} className="space-y-4">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div className="col-span-full">
                  <Label htmlFor="name">Full Name</Label>
                  <TextInput 
                    id="name" 
                    value={formData.name}
                    onChange={(e) => setFormData({...formData, name: e.target.value})}
                    required
                  />
                </div>
                
                <div className="col-span-full">
                  <Label htmlFor="email">Email Address</Label>
                  <TextInput 
                    id="email" 
                    value={formData.email}
                    disabled
                    helperText="Email cannot be changed."
                    color="gray"
                  />
                </div>

                <div>
                  <Label htmlFor="department">Department</Label>
                  <TextInput 
                    id="department" 
                    placeholder="e.g. Sales"
                    value={formData.department}
                    onChange={(e) => setFormData({...formData, department: e.target.value})}
                  />
                </div>

                <div>
                  <Label htmlFor="region">Region</Label>
                  <TextInput 
                    id="region" 
                    placeholder="e.g. Nairobi"
                    value={formData.region}
                    onChange={(e) => setFormData({...formData, region: e.target.value})}
                  />
                </div>

                <div className="md:col-span-2">
                  <Label htmlFor="branch">Branch</Label>
                  <TextInput 
                    id="branch" 
                    placeholder="e.g. CBD Branch"
                    value={formData.branch}
                    onChange={(e) => setFormData({...formData, branch: e.target.value})}
                  />
                </div>
              </div>

              <div className="flex justify-end pt-4">
                <Button type="submit" color="purple" disabled={isSaving}>
                  {isSaving ? <Spinner size="sm" className="mr-2" /> : null}
                  Save Changes
                </Button>
              </div>
            </form>
          </Card>
        </div>

        {/* Right Column: Organization & Subscription Info */}
        <div className="lg:col-span-1 space-y-6">
          {/* Organization Card */}
          <Card>
            <div className="flex items-center mb-4">
              <div className="p-2 bg-gray-100 rounded-lg mr-3">
                <HiOfficeBuilding className="w-6 h-6 text-gray-600" />
              </div>
              <h3 className="text-lg font-bold text-gray-900">Organization</h3>
            </div>
            
            <div className="space-y-3">
              <div>
                <span className="text-xs text-gray-500 uppercase font-semibold">Tenant Name</span>
                <p className="font-medium text-gray-900">{user?.tenantName || 'Individual'}</p>
              </div>
              <div>
                <span className="text-xs text-gray-500 uppercase font-semibold">Tenant ID</span>
                <p className="font-mono text-sm text-gray-600 bg-gray-50 p-1 rounded inline-block">
                  {user?.tenantId}
                </p>
              </div>
              <div>
                <span className="text-xs text-gray-500 uppercase font-semibold">Role</span>
                <p className="font-medium text-gray-900">{user?.role || 'User'}</p>
              </div>
            </div>
          </Card>

          {/* Subscription Summary */}
          <Card className={subscription?.status === 'ACTIVE' ? "border-t-4 border-green-500" : "border-t-4 border-yellow-500"}>
            <h3 className="text-lg font-bold text-gray-900 mb-2">Subscription</h3>
            
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600">Plan:</span>
                <span className="font-bold">{subscription?.plan?.name || 'Free'}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">Status:</span>
                <span className={`px-2 py-0.5 rounded text-xs font-bold ${
                  subscription?.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'
                }`}>
                  {subscription?.status || 'Unknown'}
                </span>
              </div>
              {subscription?.currentPeriodEnd && (
                <div className="flex justify-between">
                  <span className="text-gray-600">Renews:</span>
                  <span className="text-sm">
                    {new Date(subscription.currentPeriodEnd).toLocaleDateString()}
                  </span>
                </div>
              )}
            </div>
            
            <Button size="xs" color="gray" className="mt-4 w-full" href="/subscriptions">
              Manage Subscription
            </Button>
          </Card>
        </div>
      </div>
    </div>
  )
}

export default Profile
