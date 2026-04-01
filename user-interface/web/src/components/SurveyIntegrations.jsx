import { useState, useEffect } from 'react'
import { integrationAPI } from '../services/apiServices'
import Button from './ui/Button'
import Badge from './ui/Badge'
import Card from './ui/Card'
import Label from './ui/Label'
import Input from './ui/Input'
import Select from './ui/Select'
import Alert from './ui/Alert'
import { HiOutlineLightningBolt, HiClipboardCopy, HiCheck, HiExclamationCircle } from 'react-icons/hi'

const SurveyIntegrations = ({ surveyId }) => {
  const [integrations, setIntegrations] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [copySuccess, setCopySuccess] = useState('')
  const [fetchError, setFetchError] = useState('')

  const [formData, setFormData] = useState({
    businessName: '', type: 'MPESA_C2B', shortcode: '', consumerKey: '', consumerSecret: '',
  })
  const [createError, setCreateError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const fetchIntegrations = async () => {
    setIsLoading(true)
    setFetchError('')
    try {
      const response = await integrationAPI.getIntegrations()
      const all = Array.isArray(response.data) ? response.data : []
      const forSurvey = all.filter(i => i.surveyId === surveyId || i.surveyId === parseInt(surveyId))
      const hasSurveyIdField = all.some(i => i.surveyId !== undefined)
      setIntegrations(hasSurveyIdField ? forSurvey : all)
    } catch {
      setFetchError('Failed to load integrations.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => { if (surveyId) fetchIntegrations() }, [surveyId])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setCreateError('')
    setIsSubmitting(true)
    try {
      await integrationAPI.createIntegration({ ...formData, surveyId })
      setShowCreateForm(false)
      setFormData({ businessName: '', type: 'MPESA_C2B', shortcode: '', consumerKey: '', consumerSecret: '' })
      fetchIntegrations()
    } catch (err) {
      setCreateError(err.response?.data?.message || 'Failed to create integration')
    } finally {
      setIsSubmitting(false)
    }
  }

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text)
    setCopySuccess(text)
    setTimeout(() => setCopySuccess(''), 2000)
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h3 className="text-xl font-bold text-[var(--text)]">Business Integrations</h3>
          <p className="text-[var(--text-muted)] text-sm">Connect payment systems to this survey.</p>
        </div>
        <Button size="sm" onClick={() => setShowCreateForm(!showCreateForm)}>
          {showCreateForm ? 'Cancel' : 'New Integration'}
        </Button>
      </div>

      {fetchError && (
        <Alert color="failure" icon={HiExclamationCircle} onDismiss={() => setFetchError('')}>
          {fetchError}
        </Alert>
      )}

      {showCreateForm && (
        <Card className="border-l-4 border-brand">
          <h4 className="text-lg font-bold text-[var(--text)] mb-4">Add Integration for Survey #{surveyId}</h4>
          {createError && <Alert color="failure" className="mb-4">{createError}</Alert>}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="businessName">Business Name</Label>
                <Input id="businessName" placeholder="e.g. Main Branch Shop" required value={formData.businessName} onChange={(e) => setFormData({ ...formData, businessName: e.target.value })} />
              </div>
              <div>
                <Label htmlFor="type">Integration Type</Label>
                <Select id="type" value={formData.type} onChange={(e) => setFormData({ ...formData, type: e.target.value })}>
                  <option value="MPESA_C2B">M-Pesa Paybill/Buy Goods</option>
                  <option value="POS_GENERIC">Generic POS (API)</option>
                </Select>
              </div>
              <div>
                <Label htmlFor="shortcode">Shortcode / Till Number</Label>
                <Input id="shortcode" placeholder="e.g. 174379" required value={formData.shortcode} onChange={(e) => setFormData({ ...formData, shortcode: e.target.value })} />
              </div>
            </div>

            <div className="bg-[var(--surface-hover)] p-4 rounded-lg border border-[var(--border)]">
              <p className="font-medium text-[var(--text)] mb-2">Daraja API Credentials (Optional)</p>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="consumerKey">Consumer Key</Label>
                  <Input id="consumerKey" placeholder="Key from Daraja" value={formData.consumerKey} onChange={(e) => setFormData({ ...formData, consumerKey: e.target.value })} />
                </div>
                <div>
                  <Label htmlFor="consumerSecret">Consumer Secret</Label>
                  <Input id="consumerSecret" type="password" placeholder="Secret from Daraja" value={formData.consumerSecret} onChange={(e) => setFormData({ ...formData, consumerSecret: e.target.value })} />
                </div>
              </div>
            </div>

            <div className="flex justify-end">
              <Button type="submit" loading={isSubmitting}>Create Integration</Button>
            </div>
          </form>
        </Card>
      )}

      <div className="grid grid-cols-1 gap-4">
        {integrations.map((intg) => (
          <Card key={intg.id}>
            <div className="flex justify-between items-start">
              <div>
                <h5 className="font-bold text-[var(--text)]">{intg.businessName}</h5>
                <div className="flex gap-2 mt-1">
                  <Badge color="blue">{intg.type === 'MPESA_C2B' ? 'M-Pesa C2B' : 'POS'}</Badge>
                  <Badge color={intg.isActive ? 'success' : 'gray'}>{intg.isActive ? 'Active' : 'Pending'}</Badge>
                </div>
              </div>
              <span className="text-sm font-mono bg-[var(--surface-hover)] px-2 py-1 rounded text-[var(--text)]">{intg.shortcode}</span>
            </div>

            <div className="mt-4 pt-4 border-t border-[var(--border)]">
              <p className="text-xs text-[var(--text-muted)] mb-1">Callback URL:</p>
              <div className="flex items-center gap-2 bg-[var(--surface-hover)] p-2 rounded">
                <code className="text-xs text-brand truncate flex-grow">{intg.callbackUrl}</code>
                <button onClick={() => copyToClipboard(intg.callbackUrl)} className="text-[var(--text-muted)] hover:text-[var(--text)]">
                  {copySuccess === intg.callbackUrl ? <HiCheck className="text-[var(--success)]" /> : <HiClipboardCopy />}
                </button>
              </div>
            </div>
          </Card>
        ))}

        {integrations.length === 0 && !isLoading && (
          <div className="text-center py-8 border-2 border-dashed border-[var(--border)] rounded-lg">
            <HiOutlineLightningBolt className="mx-auto h-8 w-8 text-[var(--text-muted)]" />
            <p className="mt-2 text-sm text-[var(--text-muted)]">No integrations configured for this survey.</p>
          </div>
        )}
      </div>
    </div>
  )
}

export default SurveyIntegrations
