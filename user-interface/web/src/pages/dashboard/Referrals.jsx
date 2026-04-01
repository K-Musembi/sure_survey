import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { referralAPI, integrationAPI } from '../../services/apiServices'
import { useMySurveys } from '../../hooks/useApi'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import Modal from '../../components/ui/Modal'
import Label from '../../components/ui/Label'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import FormField from '../../components/forms/FormField'
import { SkeletonCard } from '../../components/ui/Skeleton'
import EmptyState from '../../components/ui/EmptyState'
import PrerequisitesBanner from '../../components/PrerequisitesBanner'
import useToast from '../../hooks/useToast'
import {
  HiOutlinePlus, HiOutlineUserGroup, HiOutlineChevronRight,
  HiOutlineChevronLeft, HiOutlineLightningBolt,
} from 'react-icons/hi'

const statusColors = { ACTIVE: 'success', PAUSED: 'accent', COMPLETED: 'blue', DRAFT: 'gray' }

const CAMPAIGN_TYPES = [
  { value: 'SURVEY_OPEN', label: 'Survey Open', desc: 'Anyone can participate via a shared link' },
  { value: 'SURVEY_CLOSED', label: 'Survey Closed', desc: 'Only invited participants can respond' },
  { value: 'SERVICE', label: 'Service', desc: 'Referral triggered by a service activation' },
]

const REWARD_TRIGGERS = [
  { value: 'SURVEY_COMPLETE', label: 'Survey Completion' },
  { value: 'SERVICE_ACTIVATION', label: 'Service Activation' },
]

const REWARD_TYPES = ['AIRTIME', 'DATA', 'POINTS']

const defaultForm = {
  name: '', campaignType: 'SURVEY_OPEN', purposeDescription: '',
  rewardTrigger: 'SURVEY_COMPLETE', surveyId: '', businessIntegrationId: '',
  referrerRewardType: 'AIRTIME', referrerRewardValue: '',
  maxReferralsPerUser: 3, dailyReferralLimit: 5, inviteExpiryHours: 72,
  startDate: '', endDate: '',
}

export default function Referrals() {
  const navigate = useNavigate()
  const toast = useToast()
  const { data: surveys } = useMySurveys()
  const [campaigns, setCampaigns] = useState([])
  const [loading, setLoading] = useState(true)
  const [showCreate, setShowCreate] = useState(false)
  const [form, setForm] = useState({ ...defaultForm })
  const [creating, setCreating] = useState(false)
  const [modalStep, setModalStep] = useState(0)
  const [integrations, setIntegrations] = useState([])

  const fetchCampaigns = () => {
    setLoading(true)
    referralAPI?.getCampaigns?.()
      ?.then(r => setCampaigns(r.data || []))
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(() => {
    fetchCampaigns()
    integrationAPI.getIntegrations()
      .then(r => setIntegrations(r.data || []))
      .catch(() => {})
  }, [])

  const openCreate = () => {
    setForm({ ...defaultForm })
    setModalStep(0)
    setShowCreate(true)
  }

  const handleCreate = async () => {
    setCreating(true)
    try {
      const payload = {
        ...form,
        surveyId: form.surveyId ? parseInt(form.surveyId) : null,
        businessIntegrationId: form.businessIntegrationId || null,
        referrerRewardValue: form.referrerRewardValue ? parseFloat(form.referrerRewardValue) : null,
        maxReferralsPerUser: parseInt(form.maxReferralsPerUser) || 3,
        dailyReferralLimit: parseInt(form.dailyReferralLimit) || 5,
        inviteExpiryHours: parseInt(form.inviteExpiryHours) || 72,
        startDate: form.startDate || null,
        endDate: form.endDate || null,
      }
      await referralAPI.createCampaign(payload)
      toast.success('Campaign created!')
      setShowCreate(false)
      fetchCampaigns()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create campaign.')
    } finally {
      setCreating(false)
    }
  }

  const set = (field, value) => setForm(prev => ({ ...prev, [field]: value }))

  const canProceedStep0 = form.name.trim() && form.campaignType && form.purposeDescription.trim()
  const canProceedStep1 = true // survey and integration are optional

  if (loading) return <div className="space-y-4">{Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}</div>

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-[var(--text)]">Referrals</h1>
          <p className="text-sm text-[var(--text-muted)] mt-0.5">Manage referral campaigns</p>
        </div>
        <Button onClick={openCreate}><HiOutlinePlus className="h-4 w-4" /> New Campaign</Button>
      </div>

      <PrerequisitesBanner checks={[
        {
          label: 'At least one survey available for referrals',
          passed: (surveys || []).length > 0,
          actionLabel: 'Create survey',
          action: () => navigate('/dashboard/new'),
        },
      ]} />

      {campaigns.length ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {campaigns.map((c) => (
            <div key={c.id} className="card">
              <div className="flex items-start justify-between mb-2">
                <h3 className="font-semibold text-[var(--text)]">{c.name}</h3>
                <Badge color={statusColors[c.status] || 'gray'}>{c.status}</Badge>
              </div>
              <p className="text-sm text-[var(--text-muted)] line-clamp-2">{c.purposeDescription || 'No description'}</p>
              <div className="mt-3 flex items-center gap-4 text-xs text-[var(--text-muted)]">
                <span>{c.inviteCount || 0} invites</span>
                <span>{c.completedCount || 0} completed</span>
              </div>
            </div>
          ))}
        </div>
      ) : (
        <EmptyState
          icon={HiOutlineUserGroup}
          title="No campaigns yet"
          description="Create a referral campaign to grow your survey reach."
          actionLabel="New Campaign"
          onAction={openCreate}
        />
      )}

      {/* Multi-step create modal */}
      <Modal open={showCreate} onClose={() => setShowCreate(false)} title="New Referral Campaign" size="md">
        {/* Step indicators */}
        <div className="flex items-center gap-2 mb-6">
          {['Basics', 'Survey & Integration', 'Rewards & Limits'].map((label, i) => (
            <div key={i} className="flex items-center gap-2">
              <div className={`flex h-7 w-7 items-center justify-center rounded-full text-xs font-medium
                ${i < modalStep ? 'bg-brand text-white' : i === modalStep ? 'bg-brand/15 text-brand border-2 border-brand' : 'bg-[var(--surface-hover)] text-[var(--text-muted)]'}`}>
                {i + 1}
              </div>
              <span className={`hidden sm:block text-xs font-medium ${i === modalStep ? 'text-[var(--text)]' : 'text-[var(--text-muted)]'}`}>{label}</span>
              {i < 2 && <div className={`h-px w-6 ${i < modalStep ? 'bg-brand' : 'bg-[var(--border)]'}`} />}
            </div>
          ))}
        </div>

        {/* Step 0: Basics */}
        {modalStep === 0 && (
          <div className="space-y-4">
            <FormField label="Campaign Name" required>
              <Input value={form.name} onChange={e => set('name', e.target.value)} placeholder="e.g. Q1 Customer Referral" required />
            </FormField>

            <div>
              <Label>Campaign Type</Label>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-2 mt-1">
                {CAMPAIGN_TYPES.map(ct => (
                  <button
                    key={ct.value}
                    type="button"
                    onClick={() => set('campaignType', ct.value)}
                    className={`rounded-lg border p-3 text-left transition-colors ${form.campaignType === ct.value
                      ? 'border-brand bg-brand/5'
                      : 'border-[var(--border)] hover:bg-[var(--surface-hover)]'}`}
                  >
                    <p className="text-sm font-medium text-[var(--text)]">{ct.label}</p>
                    <p className="text-xs text-[var(--text-muted)] mt-0.5">{ct.desc}</p>
                  </button>
                ))}
              </div>
            </div>

            <FormField label="Purpose Description" required helper="Displayed to participants for ODPC consent.">
              <textarea
                className="input-field min-h-[80px]"
                value={form.purposeDescription}
                onChange={e => set('purposeDescription', e.target.value)}
                required
                placeholder="Describe why you are collecting referrals..."
              />
            </FormField>

            <FormField label="Reward Trigger">
              <Select value={form.rewardTrigger} onChange={e => set('rewardTrigger', e.target.value)}>
                {REWARD_TRIGGERS.map(rt => <option key={rt.value} value={rt.value}>{rt.label}</option>)}
              </Select>
            </FormField>

            <div className="flex justify-end pt-2">
              <Button onClick={() => setModalStep(1)} disabled={!canProceedStep0}>
                Next <HiOutlineChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}

        {/* Step 1: Survey & Integration */}
        {modalStep === 1 && (
          <div className="space-y-4">
            <FormField label="Link to Survey" helper="Select the survey participants will complete.">
              <Select value={form.surveyId} onChange={e => set('surveyId', e.target.value)}>
                <option value="">None (configure later)</option>
                {(surveys || []).map(s => (
                  <option key={s.id} value={s.id}>{s.name} ({s.status})</option>
                ))}
              </Select>
            </FormField>

            <FormField label="Business Integration" helper="Auto-trigger referral when a customer transacts via this integration.">
              {integrations.length > 0 ? (
                <Select value={form.businessIntegrationId} onChange={e => set('businessIntegrationId', e.target.value)}>
                  <option value="">None</option>
                  {integrations.map(intg => (
                    <option key={intg.id} value={intg.id}>{intg.businessName} — {intg.shortcode}</option>
                  ))}
                </Select>
              ) : (
                <div className="flex items-center gap-2 p-3 rounded-lg bg-[var(--surface-hover)] border border-[var(--border)]">
                  <HiOutlineLightningBolt className="h-5 w-5 text-[var(--text-muted)] flex-shrink-0" />
                  <div className="flex-1">
                    <p className="text-sm text-[var(--text-muted)]">No integrations configured yet.</p>
                  </div>
                  <Button size="xs" variant="secondary" as={Link} to="/dashboard/integrations" onClick={() => setShowCreate(false)}>
                    Set up
                  </Button>
                </div>
              )}
            </FormField>

            <div className="flex justify-between pt-2">
              <Button variant="secondary" onClick={() => setModalStep(0)}>
                <HiOutlineChevronLeft className="h-4 w-4" /> Back
              </Button>
              <Button onClick={() => setModalStep(2)} disabled={!canProceedStep1}>
                Next <HiOutlineChevronRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
        )}

        {/* Step 2: Rewards & Limits */}
        {modalStep === 2 && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <FormField label="Reward Type">
                <Select value={form.referrerRewardType} onChange={e => set('referrerRewardType', e.target.value)}>
                  {REWARD_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                </Select>
              </FormField>
              <FormField label="Reward Value">
                <Input type="number" value={form.referrerRewardValue} onChange={e => set('referrerRewardValue', e.target.value)} placeholder="e.g. 50" />
              </FormField>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <FormField label="Max Referrals / User">
                <Input type="number" value={form.maxReferralsPerUser} onChange={e => set('maxReferralsPerUser', e.target.value)} />
              </FormField>
              <FormField label="Daily Limit">
                <Input type="number" value={form.dailyReferralLimit} onChange={e => set('dailyReferralLimit', e.target.value)} />
              </FormField>
              <FormField label="Invite Expiry (hrs)">
                <Input type="number" value={form.inviteExpiryHours} onChange={e => set('inviteExpiryHours', e.target.value)} />
              </FormField>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <FormField label="Start Date">
                <Input type="date" value={form.startDate} onChange={e => set('startDate', e.target.value)} />
              </FormField>
              <FormField label="End Date">
                <Input type="date" value={form.endDate} onChange={e => set('endDate', e.target.value)} />
              </FormField>
            </div>

            <div className="flex justify-between pt-2">
              <Button variant="secondary" onClick={() => setModalStep(1)}>
                <HiOutlineChevronLeft className="h-4 w-4" /> Back
              </Button>
              <Button onClick={handleCreate} loading={creating}>
                Create Campaign
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  )
}
