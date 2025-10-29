import { Card, Label, TextInput } from 'flowbite-react'
import useAuthStore from '../stores/authStore'
import { useQuery } from '@tanstack/react-query'
import { subscriptionAPI } from '../services/apiServices'
import { QUERY_KEYS } from '../lib/queryClient'

const Profile = () => {
  const { user, tenant } = useAuthStore()

  const { data: subscription, isLoading: isLoadingSubscription } = useQuery({
    queryKey: QUERY_KEYS.auth.subscription,
    queryFn: async () => {
      const response = await subscriptionAPI.getSubscription()
      return response.data
    },
    enabled: !!user, // Only fetch if user is available
  })

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">My Information</h1>

      <Card className="mt-4">
        <p>Profile content placeholder.</p>
        <p>User: {user?.name}</p>
        <p>Tenant: {tenant?.name}</p>
        <p>Subscription Status: {subscription?.status}</p>
      </Card>
    </div>
  )
}

export default Profile