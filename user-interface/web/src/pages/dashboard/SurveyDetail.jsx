import { useState, useEffect } from 'react'
import { useParams, Link, useLocation } from 'react-router-dom'
import { useSurvey, useActivateSurvey } from '../../hooks/useApi'
import AnalyticsDashboard from '../../components/AnalyticsDashboard'
import SurveyContacts from '../../components/SurveyContacts'
import SurveyIntegrations from '../../components/SurveyIntegrations'
import SurveyRewards from '../../components/SurveyRewards'
import BranchRuleEditor from '../../components/survey/BranchRuleEditor'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import { SkeletonText } from '../../components/ui/Skeleton'
import PaymentModal from '../../components/PaymentModal'
import useToast from '../../hooks/useToast'
import { surveyAPI } from '../../services/apiServices'
import {
  HiOutlineChartPie, HiOutlineUserGroup, HiOutlineLightningBolt,
  HiOutlineCurrencyDollar, HiOutlinePlay, HiOutlineEye, HiOutlinePencil, HiOutlineCode,
} from 'react-icons/hi'

const tabs = [
  { key: 'overview', label: 'Overview', icon: HiOutlineChartPie },
  { key: 'distribution', label: 'Distribution', icon: HiOutlineUserGroup },
  { key: 'integrations', label: 'Integrations', icon: HiOutlineLightningBolt },
  { key: 'rewards', label: 'Rewards', icon: HiOutlineCurrencyDollar },
  { key: 'branch-rules', label: 'Branch Rules', icon: HiOutlineCode },
]

const statusColors = { DRAFT: 'gray', ACTIVE: 'success', PAUSED: 'accent', CLOSED: 'error' }

export default function SurveyDetail() {
  const { id: surveyId } = useParams()
  const location = useLocation()
  const { data: survey, isLoading, error, refetch } = useSurvey(surveyId)
  const activateMutation = useActivateSurvey()
  const toast = useToast()

  const [activeTab, setActiveTab] = useState('overview')
  const [showPayment, setShowPayment] = useState(false)
  const [branchRules, setBranchRules] = useState([])

  useEffect(() => { if (location.state?.tab) setActiveTab(location.state.tab) }, [location.state])
  useEffect(() => {
    if (surveyId) {
      surveyAPI.getBranchRules?.(surveyId)?.then(r => setBranchRules(r.data)).catch(() => {})
    }
  }, [surveyId])

  const handleActivate = async () => {
    try {
      await activateMutation.mutateAsync(survey.id)
      toast.success('Survey activated!')
      refetch()
    } catch (err) {
      if (err.response?.data?.message?.includes('fund')) setShowPayment(true)
      else toast.error(err.response?.data?.message || 'Activation failed.')
    }
  }

  if (isLoading) return <div className="space-y-4"><SkeletonText lines={2} /><SkeletonText lines={6} /></div>
  if (error || !survey) return (
    <div className="text-center py-16">
      <h3 className="text-lg font-semibold text-[var(--text)]">Survey not found</h3>
      <Button as={Link} to="/dashboard" className="mt-4">Back to Surveys</Button>
    </div>
  )

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-[var(--text)]">{survey.name}</h1>
            <Badge color={statusColors[survey.status] || 'gray'}>{survey.status}</Badge>
          </div>
          <p className="text-sm text-[var(--text-muted)] mt-0.5">
            {survey.type} &middot; Created {new Date(survey.createdAt).toLocaleDateString()}
          </p>
        </div>
        <div className="flex gap-2">
          <Button variant="secondary" size="sm" as={Link} to={`/survey/${survey.id}`} target="_blank">
            <HiOutlineEye className="h-4 w-4" /> Preview
          </Button>
          {survey.status === 'DRAFT' && (
            <>
              <Button variant="secondary" size="sm" as={Link} to={`/dashboard/new?edit=${survey.id}`}>
                <HiOutlinePencil className="h-4 w-4" /> Edit
              </Button>
              <Button size="sm" onClick={handleActivate} loading={activateMutation.isPending}>
                <HiOutlinePlay className="h-4 w-4" /> Activate
              </Button>
            </>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="flex gap-1 border-b border-[var(--border)] overflow-x-auto">
        {tabs.map((tab) => (
          <button
            key={tab.key}
            onClick={() => setActiveTab(tab.key)}
            className={`flex items-center gap-2 whitespace-nowrap border-b-2 px-4 py-3 text-sm font-medium transition-colors
              ${activeTab === tab.key ? 'border-brand text-brand' : 'border-transparent text-[var(--text-muted)] hover:text-[var(--text)] hover:border-[var(--border)]'}`}
          >
            <tab.icon className="h-4 w-4" /> {tab.label}
          </button>
        ))}
      </div>

      {/* Tab content */}
      <div>
        {activeTab === 'overview' && <AnalyticsDashboard surveys={[survey]} />}
        {activeTab === 'distribution' && <SurveyContacts surveyId={survey.id} />}
        {activeTab === 'integrations' && <SurveyIntegrations surveyId={survey.id} />}
        {activeTab === 'rewards' && <SurveyRewards survey={survey} onUpdate={refetch} />}
        {activeTab === 'branch-rules' && (
          <BranchRuleEditor
            rules={branchRules}
            questions={survey.questions || []}
            onCreate={(data) => {
              surveyAPI.createBranchRule?.(surveyId, data)
                ?.then(r => { setBranchRules(prev => [...prev, r.data]); toast.success('Rule added!') })
                .catch(() => toast.error('Failed to add rule.'))
            }}
            onDelete={(ruleId) => {
              surveyAPI.deleteBranchRule?.(surveyId, ruleId)
                ?.then(() => { setBranchRules(prev => prev.filter(r => r.id !== ruleId)); toast.success('Rule deleted.') })
                .catch(() => toast.error('Failed to delete rule.'))
            }}
            onSuggest={() => {
              // Will be wired in Phase 3
              toast.info('AI suggestions coming soon.')
            }}
          />
        )}
      </div>

      <PaymentModal
        show={showPayment}
        onClose={() => setShowPayment(false)}
        survey={survey}
        mode="SURVEY_ACTIVATION"
        onPaymentSuccess={() => { setShowPayment(false); refetch() }}
      />
    </div>
  )
}
