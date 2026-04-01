import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useMySurveys, useUsage } from '../../hooks/useApi'
import { integrationAPI } from '../../services/apiServices'
import SurveyCard from '../../components/survey/SurveyCard'
import { SkeletonCard } from '../../components/ui/Skeleton'
import EmptyState from '../../components/ui/EmptyState'
import Button from '../../components/ui/Button'
import UpgradeModal from '../../components/UpgradeModal'
import PrerequisitesBanner from '../../components/PrerequisitesBanner'
import { HiOutlinePlus, HiOutlineFire, HiOutlineSearch, HiOutlineLockClosed, HiOutlineCog } from 'react-icons/hi'

const statusFilters = ['All', 'DRAFT', 'ACTIVE', 'PAUSED', 'CLOSED']

export default function Competitions() {
  const navigate = useNavigate()
  const { data: surveys, isLoading } = useMySurveys()
  const { data: usage } = useUsage()
  const [filter, setFilter] = useState('All')
  const [search, setSearch] = useState('')
  const [showUpgrade, setShowUpgrade] = useState(false)
  const [integrations, setIntegrations] = useState(null)

  useEffect(() => {
    integrationAPI.getIntegrations()
      .then(r => setIntegrations(r.data || []))
      .catch(() => setIntegrations([]))
  }, [])

  const isLocked = !usage?.performanceSurvey

  // Filter to only performance-type surveys (surveys with type containing "PERFORMANCE" or tagged)
  const competitionSurveys = (surveys || []).filter(s => s.type === 'PERFORMANCE')

  const filtered = competitionSurveys.filter((s) => {
    if (filter !== 'All' && s.status !== filter) return false
    if (search && !s.name.toLowerCase().includes(search.toLowerCase())) return false
    return true
  })

  if (isLocked) {
    return (
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-bold text-[var(--text)]">Competitions</h1>
          <p className="text-sm text-[var(--text-muted)] mt-0.5">Performance-based competitive surveys</p>
        </div>
        <div className="text-center py-16 card">
          <HiOutlineLockClosed className="h-12 w-12 text-[var(--text-muted)] mx-auto mb-4" />
          <h2 className="text-xl font-bold text-[var(--text)] mb-2">Competitions require Pro or higher</h2>
          <p className="text-[var(--text-muted)] max-w-md mx-auto mb-6">
            Create competitive surveys with leaderboards, scoring, and performance tracking.
            Upgrade to Pro to unlock this feature.
          </p>
          <Button onClick={() => setShowUpgrade(true)}>Upgrade to Pro</Button>
          <UpgradeModal
            open={showUpgrade}
            onClose={() => setShowUpgrade(false)}
            currentPlan={usage?.planName || 'Free'}
            highlightPlan="Pro"
            reason="Competition surveys require the Pro plan or higher."
          />
        </div>
      </div>
    )
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-[var(--text)]">Competitions</h1>
          <p className="text-sm text-[var(--text-muted)] mt-0.5">Performance-based competitive surveys</p>
        </div>
        <Button as={Link} to="/dashboard/new?type=PERFORMANCE">
          <HiOutlinePlus className="h-4 w-4" /> New Competition
        </Button>
      </div>

      <PrerequisitesBanner checks={[
        {
          label: 'Business integration configured',
          passed: integrations?.length > 0,
          actionLabel: 'Set up integration',
          action: () => navigate('/dashboard/integrations'),
        },
        {
          label: 'At least one competition survey created',
          passed: competitionSurveys.length > 0,
          actionLabel: 'Create competition',
          action: () => navigate('/dashboard/new?type=PERFORMANCE'),
        },
      ]} />

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1 max-w-xs">
          <HiOutlineSearch className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-[var(--text-muted)]" />
          <input
            className="input-field pl-9"
            placeholder="Search competitions..."
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
          {Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : filtered.length ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filtered.map((s) => (
            <div key={s.id}>
              <SurveyCard survey={s} />
              {s.status === 'DRAFT' && (
                <div className="mt-2">
                  <Button size="sm" variant="secondary" className="w-full" as={Link} to={`/dashboard/competitions/setup/${s.id}`}>
                    <HiOutlineCog className="h-4 w-4" /> Configure Scoring & Subjects
                  </Button>
                </div>
              )}
            </div>
          ))}
        </div>
      ) : (
        <EmptyState
          icon={HiOutlineFire}
          title={search || filter !== 'All' ? 'No matching competitions' : 'No competitions yet'}
          description={search || filter !== 'All' ? 'Try adjusting your filters.' : 'Create your first competition survey to get started.'}
          actionLabel={!search && filter === 'All' ? 'New Competition' : undefined}
          onAction={() => window.location.assign('/dashboard/new?type=PERFORMANCE')}
        />
      )}
    </div>
  )
}
