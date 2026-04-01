import { useState, useEffect } from 'react'
import { integrationAPI } from '../../services/apiServices'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import Card from '../../components/ui/Card'
import Modal from '../../components/ui/Modal'
import Label from '../../components/ui/Label'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import { SkeletonCard } from '../../components/ui/Skeleton'
import EmptyState from '../../components/ui/EmptyState'
import useToast from '../../hooks/useToast'
import {
  HiOutlineLightningBolt, HiOutlinePlus, HiClipboardCopy, HiCheck,
  HiOutlineChevronDown, HiOutlineChevronUp,
} from 'react-icons/hi'

export default function Integrations() {
  const toast = useToast()
  const [integrations, setIntegrations] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)
  const [creating, setCreating] = useState(false)
  const [copySuccess, setCopySuccess] = useState('')
  const [expandedId, setExpandedId] = useState(null)
  const [transactions, setTransactions] = useState({})
  const [txLoading, setTxLoading] = useState({})

  const [form, setForm] = useState({
    businessName: '', type: 'MPESA_C2B', shortcode: '', consumerKey: '', consumerSecret: '',
  })

  const fetchIntegrations = () => {
    setLoading(true)
    integrationAPI.getIntegrations()
      .then(r => setIntegrations(r.data || []))
      .catch(() => toast.error('Failed to load integrations'))
      .finally(() => setLoading(false))
  }

  useEffect(() => { fetchIntegrations() }, [])

  const handleCreate = async (e) => {
    e.preventDefault()
    setCreating(true)
    try {
      await integrationAPI.createIntegration(form)
      toast.success('Integration created!')
      setShowCreate(false)
      setForm({ businessName: '', type: 'MPESA_C2B', shortcode: '', consumerKey: '', consumerSecret: '' })
      fetchIntegrations()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create integration')
    } finally {
      setCreating(false)
    }
  }

  const toggleTransactions = async (id) => {
    if (expandedId === id) {
      setExpandedId(null)
      return
    }
    setExpandedId(id)
    if (!transactions[id]) {
      setTxLoading(p => ({ ...p, [id]: true }))
      try {
        const res = await integrationAPI.getTransactions(id)
        setTransactions(p => ({ ...p, [id]: res.data || [] }))
      } catch {
        toast.error('Failed to load transactions')
      } finally {
        setTxLoading(p => ({ ...p, [id]: false }))
      }
    }
  }

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text)
    setCopySuccess(text)
    setTimeout(() => setCopySuccess(''), 2000)
  }

  if (loading) return <div className="space-y-4">{Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}</div>

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[var(--text)]">Integrations</h1>
          <p className="text-sm text-[var(--text-muted)] mt-0.5">Connect payment systems to capture customer contacts automatically</p>
        </div>
        <Button onClick={() => setShowCreate(true)}>
          <HiOutlinePlus className="h-4 w-4" /> New Integration
        </Button>
      </div>

      {integrations.length ? (
        <div className="space-y-4">
          {integrations.map((intg) => (
            <Card key={intg.id}>
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="font-semibold text-[var(--text)]">{intg.businessName}</h3>
                  <div className="flex gap-2 mt-1.5">
                    <Badge color="blue">{intg.type === 'MPESA_C2B' ? 'M-Pesa C2B' : 'POS Generic'}</Badge>
                    <Badge color={intg.isActive ? 'success' : 'gray'}>{intg.isActive ? 'Active' : 'Pending'}</Badge>
                  </div>
                </div>
                <div className="text-right">
                  <span className="inline-block text-sm font-mono bg-[var(--surface-hover)] px-2.5 py-1 rounded text-[var(--text)]">
                    {intg.shortcode}
                  </span>
                </div>
              </div>

              {/* Callback URL */}
              <div className="mt-4 pt-4 border-t border-[var(--border)]">
                <p className="text-xs text-[var(--text-muted)] mb-1">Callback URL</p>
                <div className="flex items-center gap-2 bg-[var(--surface-hover)] p-2 rounded">
                  <code className="text-xs text-brand truncate flex-grow">{intg.callbackUrl}</code>
                  <button
                    onClick={() => copyToClipboard(intg.callbackUrl)}
                    className="text-[var(--text-muted)] hover:text-[var(--text)] transition-colors"
                  >
                    {copySuccess === intg.callbackUrl ? <HiCheck className="text-[var(--success)]" /> : <HiClipboardCopy />}
                  </button>
                </div>
              </div>

              {/* Transaction toggle */}
              <button
                onClick={() => toggleTransactions(intg.id)}
                className="mt-3 flex items-center gap-1 text-xs text-brand hover:underline"
              >
                {expandedId === intg.id ? <HiOutlineChevronUp className="h-3.5 w-3.5" /> : <HiOutlineChevronDown className="h-3.5 w-3.5" />}
                {expandedId === intg.id ? 'Hide transactions' : 'View transactions'}
              </button>

              {expandedId === intg.id && (
                <div className="mt-3 border-t border-[var(--border)] pt-3">
                  {txLoading[intg.id] ? (
                    <p className="text-xs text-[var(--text-muted)]">Loading...</p>
                  ) : transactions[intg.id]?.length ? (
                    <div className="overflow-x-auto">
                      <table className="w-full text-xs text-left">
                        <thead>
                          <tr className="border-b border-[var(--border)]">
                            <th className="py-2 pr-3 font-medium text-[var(--text-muted)]">Date</th>
                            <th className="py-2 pr-3 font-medium text-[var(--text-muted)]">Phone</th>
                            <th className="py-2 pr-3 font-medium text-[var(--text-muted)]">Name</th>
                            <th className="py-2 pr-3 font-medium text-[var(--text-muted)]">Amount</th>
                            <th className="py-2 font-medium text-[var(--text-muted)]">Ref</th>
                          </tr>
                        </thead>
                        <tbody>
                          {transactions[intg.id].slice(0, 10).map((tx) => (
                            <tr key={tx.id} className="border-b border-[var(--border)]">
                              <td className="py-2 pr-3 text-[var(--text)]">{new Date(tx.transactionTime || tx.createdAt).toLocaleDateString()}</td>
                              <td className="py-2 pr-3 text-[var(--text)]">{tx.msisdn}</td>
                              <td className="py-2 pr-3 text-[var(--text)]">{[tx.firstName, tx.lastName].filter(Boolean).join(' ') || '—'}</td>
                              <td className="py-2 pr-3 text-[var(--text)] font-medium">KES {Number(tx.amount).toLocaleString()}</td>
                              <td className="py-2 text-[var(--text-muted)]">{tx.externalTransactionId || '—'}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                      {transactions[intg.id].length > 10 && (
                        <p className="text-xs text-[var(--text-muted)] mt-2">Showing 10 of {transactions[intg.id].length} transactions</p>
                      )}
                    </div>
                  ) : (
                    <p className="text-xs text-[var(--text-muted)]">No transactions yet</p>
                  )}
                </div>
              )}
            </Card>
          ))}
        </div>
      ) : (
        <EmptyState
          icon={HiOutlineLightningBolt}
          title="No integrations yet"
          description="Connect your M-Pesa Paybill or Till Number to automatically capture customer contacts when payments are made. This powers referral campaigns and competition surveys."
          actionLabel="New Integration"
          onAction={() => setShowCreate(true)}
        />
      )}

      {/* Create modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Business Integration" size="md">
        <form onSubmit={handleCreate} className="space-y-4">
          <div>
            <Label>Business Name</Label>
            <Input
              value={form.businessName}
              onChange={(e) => setForm({ ...form, businessName: e.target.value })}
              placeholder="e.g. Main Branch Shop"
              required
            />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label>Integration Type</Label>
              <Select value={form.type} onChange={(e) => setForm({ ...form, type: e.target.value })}>
                <option value="MPESA_C2B">M-Pesa Paybill / Buy Goods</option>
                <option value="POS_GENERIC">Generic POS (API)</option>
              </Select>
            </div>
            <div>
              <Label>Shortcode / Till Number</Label>
              <Input
                value={form.shortcode}
                onChange={(e) => setForm({ ...form, shortcode: e.target.value })}
                placeholder="e.g. 174379"
                required
              />
            </div>
          </div>

          <div className="bg-[var(--surface-hover)] p-4 rounded-lg border border-[var(--border)]">
            <p className="text-sm font-medium text-[var(--text)] mb-3">Daraja API Credentials (Optional)</p>
            <p className="text-xs text-[var(--text-muted)] mb-3">Provide these to auto-register callback URLs with Safaricom. Otherwise, copy the callback URL manually after creation.</p>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label>Consumer Key</Label>
                <Input
                  value={form.consumerKey}
                  onChange={(e) => setForm({ ...form, consumerKey: e.target.value })}
                  placeholder="From Daraja portal"
                />
              </div>
              <div>
                <Label>Consumer Secret</Label>
                <Input
                  type="password"
                  value={form.consumerSecret}
                  onChange={(e) => setForm({ ...form, consumerSecret: e.target.value })}
                  placeholder="From Daraja portal"
                />
              </div>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2">
            <Button variant="secondary" type="button" onClick={() => setShowCreate(false)}>Cancel</Button>
            <Button type="submit" loading={creating}>Create Integration</Button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
