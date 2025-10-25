import NavBar from '../components/NavBar'
import useAuthStore from '../stores/authStore'

const Profile = () => {
  const { user } = useAuthStore()

  return (
    <div className="min-h-screen flex flex-col">
      <NavBar />
      <main className="flex-grow max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <h1 className="text-2xl font-semibold mb-4">Profile Settings</h1>
        <div className="card">
          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700">Name</label>
              <div className="mt-1 text-gray-900">{user?.name || '—'}</div>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700">Email</label>
              <div className="mt-1 text-gray-900">{user?.email || '—'}</div>
            </div>
          </div>
        </div>
      </main>
    </div>
  )
}

export default Profile