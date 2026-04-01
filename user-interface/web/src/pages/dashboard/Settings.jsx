import { useState, useEffect } from 'react'
import { webhookAPI } from '../../services/apiServices'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import Modal from '../../components/ui/Modal'
import FormField from '../../components/forms/FormField'
import EmptyState from '../../components/ui/EmptyState'
import ThemeToggle from '../../components/ui/ThemeToggle'
import useThemeStore from '../../stores/themeStore'
import useToast from '../../hooks/useToast'
import { HiOutlinePlus, HiOutlineCode, HiOutlineTrash } from 'react-icons/hi'

export default function Settings() {
  const toast = useToast()
  const { theme } = useThemeStore()
  const [subscriptions, setSubscriptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({ url: '', eventType: 'RESPONSE_SUBMITTED' })
  const [creating, setCreating] = useState(false)

  const fetchSubs = () => {
    setLoading(true)
    webhookAPI?.getSubscriptions?.()
      ?.then(r => setSubscriptions(r.data || []))
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchSubs() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setCreating(true)
    try {
      await webhookAPI.createSubscription(form)
      toast.success('Webhook created!')
      setShowCreate(false)
      setForm({ url: '', eventType: 'RESPONSE_SUBMITTED' })
      fetchSubs()
    } catch (err) { toast.error(err.response?.data?.message || 'Failed to create webhook.') }
    finally { setCreating(false) }
  }

  const handleDelete = async (id) => {
    try { await webhookAPI.deleteSubscription(id); toast.success('Webhook deleted.'); fetchSubs() }
    catch { toast.error('Failed to delete webhook.') }
  }

  return (
    <div className="max-w-3xl space-y-8">
      {/* Appearance */}
      <section className="card">
        <h3 className="text-lg font-semibold text-[var(--text)] mb-4">Appearance</h3>
        <div className="flex items-center justify-between">
          <div>
            <p className="text-sm font-medium text-[var(--text)]">Theme</p>
            <p className="text-xs text-[var(--text-muted)]">Currently using {theme} mode</p>
          </div>
          <ThemeToggle />
        </div>
      </section>

      {/* Webhooks */}
      <section className="card">
        <div className="flex items-center justify-between mb-4">
          <h3 className="text-lg font-semibold text-[var(--text)]">Webhook Subscriptions</h3>
          <Button size="sm" onClick={() => setShowCreate(true)}><HiOutlinePlus className="h-4 w-4" /> Add</Button>
        </div>

        {loading ? (
          <p className="text-sm text-[var(--text-muted)]">Loading...</p>
        ) : subscriptions.length ? (
          <div className="space-y-3">
            {subscriptions.map((s) => (
              <div key={s.id} className="flex items-center justify-between rounded-lg border border-[var(--border)] p-3">
                <div className="flex items-center gap-3 min-w-0">
                  <HiOutlineCode className="h-4 w-4 text-[var(--text-muted)] flex-shrink-0" />
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-[var(--text)] truncate">{s.url}</p>
                    <p className="text-xs text-[var(--text-muted)]">{s.eventType}</p>
                  </div>
                </div>
                <div className="flex items-center gap-2 flex-shrink-0">
                  <Badge color={s.active ? 'success' : 'gray'}>{s.active ? 'Active' : 'Inactive'}</Badge>
                  <button onClick={() => handleDelete(s.id)} className="text-[var(--text-muted)] hover:text-[var(--error)] transition-colors">
                    <HiOutlineTrash className="h-4 w-4" />
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState icon={HiOutlineCode} title="No webhooks" description="Add a webhook to receive real-time event notifications." />
        )}
      </section>

      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Webhook" size="sm">
        <form onSubmit={handleCreate} className="space-y-5">
          <FormField label="Callback URL" required>
            <input className="input-field" type="url" value={form.url} onChange={(e) => setForm({ ...form, url: e.target.value })} required placeholder="https://example.com/webhook" />
          </FormField>
          <FormField label="Event Type">
            <select className="input-field" value={form.eventType} onChange={(e) => setForm({ ...form, eventType: e.target.value })}>
              <option value="RESPONSE_SUBMITTED">Response Submitted</option>
              <option value="SURVEY_ACTIVATED">Survey Activated</option>
              <option value="SURVEY_CLOSED">Survey Closed</option>
              <option value="PAYMENT_COMPLETED">Payment Completed</option>
            </select>
          </FormField>
          <div className="flex justify-end gap-3">
            <Button variant="secondary" type="button" onClick={() => setShowCreate(false)}>Cancel</Button>
            <Button type="submit" loading={creating}>Create</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
