import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useMySurveys, useActivateSurvey, useUsage } from '../../hooks/useApi'
import { distributionAPI, surveyAPI } from '../../services/apiServices'
import SurveyCard from '../../components/survey/SurveyCard'
import { SkeletonCard } from '../../components/ui/Skeleton'
import EmptyState from '../../components/ui/EmptyState'
import Button from '../../components/ui/Button'
import Modal from '../../components/ui/Modal'
import PaymentModal from '../../components/PaymentModal'
import UpgradeModal from '../../components/UpgradeModal'
import useToast from '../../hooks/useToast'
import { HiOutlinePlus, HiOutlineClipboardList, HiOutlineSearch, HiOutlineSparkles } from 'react-icons/hi'

const statusFilters = ['All', 'DRAFT', 'ACTIVE', 'PAUSED', 'CLOSED']

export default function Surveys() {
  const { data: surveys, isLoading, refetch } = useMySurveys()
  const activateSurveyMutation = useActivateSurvey()
  const toast = useToast()

  const [filter, setFilter] = useState('All')
  const [search, setSearch] = useState('')
  const [selectedSurvey, setSelectedSurvey] = useState(null)
  const [paymentModalOpen, setPaymentModalOpen] = useState(false)
  const [showDistModal, setShowDistModal] = useState(false)
  const [distLists, setDistLists] = useState([])
  const [selectedListId, setSelectedListId] = useState('')
  const [isSending, setIsSending] = useState(false)
  const [showUpgrade, setShowUpgrade] = useState(false)
  const { data: usage } = useUsage()

  useEffect(() => {
    distributionAPI.getLists().then(r => setDistLists(r.data)).catch(() => {})
  }, [])

  const filtered = (surveys || []).filter((s) => {
    if (filter !== 'All' && s.status !== filter) return false
    if (search && !s.name.toLowerCase().includes(search.toLowerCase())) return false
    return true
  })

  const handleActivate = async (survey) => {
    try {
      await activateSurveyMutation.mutateAsync(survey.id)
      toast.success('Survey activated!')
      refetch()
    } catch (err) {
      const msg = err.response?.data?.message || 'Activation failed.'
      if (msg.includes('fund') || msg.includes('wallet')) {
        setSelectedSurvey(survey)
        setPaymentModalOpen(true)
      } else {
        toast.error(msg)
      }
    }
  }

  const handleCopyLink = (survey) => {
    const url = survey.url_code ? `${window.location.origin}/s/${survey.url_code}` : `${window.location.origin}/survey/${survey.id}`
    navigator.clipboard.writeText(url)
    toast.success('Survey link copied!')
  }

  const handleSendToList = async () => {
    if (!selectedSurvey || !selectedListId) return
    setIsSending(true)
    try {
      await surveyAPI.sendToDistributionList(selectedSurvey.id, selectedListId)
      toast.success('Survey sent to distribution list!')
      setShowDistModal(false)
      setSelectedListId('')
      refetch()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to send.')
    } finally {
      setIsSending(false)
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[var(--text)]">My Surveys</h1>
          <p className="text-sm text-[var(--text-muted)] mt-0.5">Manage and monitor your surveys</p>
        </div>
        <Button as={Link} to="/dashboard/new">
          <HiOutlinePlus className="h-4 w-4" /> Create Survey
        </Button>
      </div>

      {/* Usage banner */}
      {usage && usage.maxSurveys > 0 && (
        <div className={`rounded-xl border p-4 flex items-center justify-between gap-4 ${
          usage.currentSurveys >= usage.maxSurveys
            ? 'border-amber-300/40 bg-amber-500/10'
            : 'border-[var(--border)] bg-[var(--surface)]'
        }`}>
          <div className="flex items-center gap-3">
            <div className={`rounded-lg p-2 ${usage.currentSurveys >= usage.maxSurveys ? 'bg-amber-500/20' : 'bg-brand/10'}`}>
              <HiOutlineSparkles className={`h-5 w-5 ${usage.currentSurveys >= usage.maxSurveys ? 'text-amber-600 dark:text-amber-400' : 'text-brand'}`} />
            </div>
            <div>
              <p className="text-sm font-medium text-[var(--text)]">
                You've used <span className="font-bold">{usage.currentSurveys}/{usage.maxSurveys}</span> surveys on the <span className="font-bold">{usage.planName}</span> plan
              </p>
              {usage.currentSurveys >= usage.maxSurveys && (
                <p className="text-xs text-[var(--text-muted)] mt-0.5">Upgrade your plan to create more surveys</p>
              )}
            </div>
          </div>
          {usage.planName === 'Free' || usage.currentSurveys >= usage.maxSurveys ? (
            <Button size="sm" onClick={() => setShowUpgrade(true)}>
              Upgrade
            </Button>
          ) : null}
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-xs">
          <HiOutlineSearch className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[var(--text-muted)]" />
          <input
            className="input-field pl-9"
            placeholder="Search surveys..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
          />
        </div>
        <div className="flex gap-1.5">
          {statusFilters.map((s) => (
            <button
              key={s}
              onClick={() => setFilter(s)}
              className={`rounded-full px-3 py-1.5 text-xs font-medium transition-colors
                ${filter === s ? 'bg-brand text-white' : 'bg-[var(--surface)] text-[var(--text-muted)] border border-[var(--border)] hover:bg-[var(--surface-hover)]'}`}
            >
              {s}
            </button>
          ))}
        </div>
      </div>

      {/* Grid */}
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {Array.from({ length: 6 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : filtered.length ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((s) => (
            <SurveyCard key={s.id} survey={s} onActivate={handleActivate} onCopyLink={handleCopyLink} />
          ))}
        </div>
      ) : (
        <EmptyState
          icon={HiOutlineClipboardList}
          title={search || filter !== 'All' ? 'No matching surveys' : 'No surveys yet'}
          description={search || filter !== 'All' ? 'Try adjusting your filters.' : 'Create your first survey to get started.'}
          actionLabel={!search && filter === 'All' ? 'Create Survey' : undefined}
          onAction={() => window.location.assign('/dashboard/new')}
        />
      )}

      {/* Payment modal */}
      <PaymentModal
        show={paymentModalOpen}
        onClose={() => { setPaymentModalOpen(false); setSelectedSurvey(null) }}
        survey={selectedSurvey}
        mode={selectedSurvey ? 'SURVEY_ACTIVATION' : 'WALLET_TOPUP'}
        onPaymentSuccess={() => { setPaymentModalOpen(false); refetch() }}
      />

      {/* Upgrade modal */}
      <UpgradeModal
        open={showUpgrade}
        onClose={() => setShowUpgrade(false)}
        currentPlan={usage?.planName || 'Free'}
        highlightPlan={usage?.planName === 'Free' ? 'Basic' : 'Pro'}
        reason={usage?.currentSurveys >= usage?.maxSurveys
          ? `You've used all ${usage.maxSurveys} surveys on your ${usage.planName} plan. Upgrade to create more.`
          : undefined}
      />

      {/* Distribution modal */}
      <Modal open={showDistModal} onClose={() => setShowDistModal(false)} title="Send Survey" size="sm">
        <div className="space-y-4">
          <p className="text-sm text-[var(--text-muted)]">Select a contact list to send SMS invitations.</p>
          <select className="input-field" value={selectedListId} onChange={(e) => setSelectedListId(e.target.value)}>
            <option value="">Select list...</option>
            {distLists.map(l => <option key={l.id} value={l.id}>{l.name} ({l.contacts?.length} contacts)</option>)}
          </select>
          <div className="flex justify-end gap-2">
            <Button variant="secondary" onClick={() => setShowDistModal(false)}>Cancel</Button>
            <Button onClick={handleSendToList} loading={isSending} disabled={!selectedListId}>Send</Button>
          </div>
        </div>
      </Modal>
    </div>
  )
}
