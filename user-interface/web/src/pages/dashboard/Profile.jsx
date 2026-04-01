import { useState, useEffect } from 'react'
import useAuthStore from '../../stores/authStore'
import { userAPI, billingAPI } from '../../services/apiServices'
import FormField from '../../components/forms/FormField'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import { SkeletonText } from '../../components/ui/Skeleton'
import useToast from '../../hooks/useToast'
import { HiOutlineUser, HiOutlineOfficeBuilding } from 'react-icons/hi'
import { Link } from 'react-router-dom'

export default function Profile() {
  const { user, setUser } = useAuthStore()
  const toast = useToast()

  const [form, setForm] = useState({ name: '', email: '', department: '', region: '', branch: '' })
  const [subscription, setSubscription] = useState(null)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    if (!user?.id) return
    setLoading(true)
    Promise.allSettled([userAPI.getUser(user.id), billingAPI.getSubscription()])
      .then(([uRes, sRes]) => {
        if (uRes.status === 'fulfilled') {
          const u = uRes.value.data
          setForm({ name: u.name || '', email: u.email || '', department: u.department || '', region: u.region || '', branch: u.branch || '' })
          setUser({ ...user, ...u })
        }
        if (sRes.status === 'fulfilled') setSubscription(sRes.value.data)
      })
      .finally(() => setLoading(false))
  }, [user?.id])

  const handleSave = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      const res = await userAPI.updateUser(user.id, { ...form, tenantId: user.tenantId })
      setUser({ ...user, ...res.data })
      toast.success('Profile updated!')
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to update profile.') }
    finally { setSaving(false) }
  }

  if (loading) return <div className="max-w-4xl mx-auto space-y-6"><SkeletonText lines={8} /></div>

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Form */}
        <div className="lg:col-span-2">
          <div className="card">
            <div className="flex items-center gap-3 mb-6">
              <div className="flex h-10 w-10 items-center justify-center rounded-lg bg-brand/10">
                <HiOutlineUser className="h-5 w-5 text-brand" />
              </div>
              <h3 className="text-lg font-semibold text-[var(--text)]">Personal Details</h3>
            </div>
            <form onSubmit={handleSave} className="space-y-5">
              <FormField label="Full Name" required>
                <input className="input-field" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
              </FormField>
              <FormField label="Email" helper="Email cannot be changed.">
                <input className="input-field opacity-60 cursor-not-allowed" value={form.email} disabled />
              </FormField>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <FormField label="Department">
                  <input className="input-field" placeholder="e.g. Sales" value={form.department} onChange={(e) => setForm({ ...form, department: e.target.value })} />
                </FormField>
                <FormField label="Region">
                  <input className="input-field" placeholder="e.g. Nairobi" value={form.region} onChange={(e) => setForm({ ...form, region: e.target.value })} />
                </FormField>
              </div>
              <FormField label="Branch">
                <input className="input-field" placeholder="e.g. CBD Branch" value={form.branch} onChange={(e) => setForm({ ...form, branch: e.target.value })} />
              </FormField>
              <div className="flex justify-end pt-2">
                <Button type="submit" loading={saving}>Save Changes</Button>
              </div>
            </form>
          </div>
        </div>

        {/* Sidebar */}
        <div className="space-y-6">
          <div className="card">
            <div className="flex items-center gap-3 mb-4">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-[var(--surface-hover)]">
                <HiOutlineOfficeBuilding className="h-4 w-4 text-[var(--text-muted)]" />
              </div>
              <h3 className="text-base font-semibold text-[var(--text)]">Organization</h3>
            </div>
            <div className="space-y-3 text-sm">
              <div><span className="text-[var(--text-muted)]">Tenant</span><p className="font-medium text-[var(--text)]">{user?.tenantName || 'Individual'}</p></div>
              <div><span className="text-[var(--text-muted)]">Role</span><p className="font-medium text-[var(--text)]">{user?.role || 'User'}</p></div>
            </div>
          </div>

          <div className="card">
            <h3 className="text-base font-semibold text-[var(--text)] mb-3">Subscription</h3>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between"><span className="text-[var(--text-muted)]">Plan</span><span className="font-medium text-[var(--text)]">{subscription?.plan?.name || 'Free'}</span></div>
              <div className="flex justify-between"><span className="text-[var(--text-muted)]">Status</span><Badge color={subscription?.status === 'ACTIVE' ? 'success' : 'accent'}>{subscription?.status || 'N/A'}</Badge></div>
              {subscription?.currentPeriodEnd && (
                <div className="flex justify-between"><span className="text-[var(--text-muted)]">Renews</span><span className="text-[var(--text)]">{new Date(subscription.currentPeriodEnd).toLocaleDateString()}</span></div>
              )}
            </div>
            <Link to="/dashboard/billing" className="btn-secondary w-full mt-4 text-sm py-2">Manage Subscription</Link>
          </div>
        </div>
      </div>
    </div>
  )
}
