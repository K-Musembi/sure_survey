import { useState, useEffect } from 'react'
import { intelligenceAPI } from '../../services/apiServices'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import { SkeletonCard } from '../../components/ui/Skeleton'
import EmptyState from '../../components/ui/EmptyState'
import { HiOutlineLightBulb, HiOutlineDocumentReport, HiOutlineClipboardCheck } from 'react-icons/hi'

export default function Intelligence() {
  const [reports, setReports] = useState([])
  const [actionPlans, setActionPlans] = useState([])
  const [loading, setLoading] = useState(true)
  const [activeTab, setActiveTab] = useState('reports')

  useEffect(() => {
    setLoading(true)
    Promise.allSettled([
      intelligenceAPI?.getMyReports?.(),
      intelligenceAPI?.getAllActionPlans?.(),
    ]).then(([r, a]) => {
      if (r?.status === 'fulfilled') setReports(r.value?.data || [])
      if (a?.status === 'fulfilled') setActionPlans(a.value?.data || [])
    }).finally(() => setLoading(false))
  }, [])

  if (loading) return <div className="space-y-4">{Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}</div>

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-[var(--text)]">Intelligence</h1>
        <p className="text-sm text-[var(--text-muted)] mt-0.5">AI-generated insight reports and action plans</p>
      </div>

      <div className="flex gap-1 border-b border-[var(--border)]">
        {[{ key: 'reports', label: 'Reports', icon: HiOutlineDocumentReport }, { key: 'actions', label: 'Action Plans', icon: HiOutlineClipboardCheck }].map((tab) => (
          <button key={tab.key} onClick={() => setActiveTab(tab.key)}
            className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors ${activeTab === tab.key ? 'border-brand text-brand' : 'border-transparent text-[var(--text-muted)] hover:text-[var(--text)]'}`}>
            <tab.icon className="h-4 w-4" />{tab.label}
          </button>
        ))}
      </div>

      {activeTab === 'reports' && (
        reports.length ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {reports.map((r) => (
              <div key={r.id} className="card">
                <div className="flex items-start justify-between mb-2">
                  <h3 className="font-semibold text-[var(--text)]">{r.title || 'Insight Report'}</h3>
                  <Badge color={r.status === 'COMPLETED' ? 'success' : 'accent'}>{r.status}</Badge>
                </div>
                <p className="text-sm text-[var(--text-muted)] line-clamp-3">{r.summary || 'Processing...'}</p>
                <p className="text-xs text-[var(--text-muted)] mt-3">{new Date(r.createdAt).toLocaleDateString()}</p>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState icon={HiOutlineLightBulb} title="No reports yet" description="Request an AI insight report from any survey's detail page." />
        )
      )}

      {activeTab === 'actions' && (
        actionPlans.length ? (
          <div className="space-y-3">
            {actionPlans.map((ap) => (
              <div key={ap.id} className="card flex items-center justify-between">
                <div>
                  <h3 className="font-semibold text-[var(--text)]">{ap.title}</h3>
                  <p className="text-sm text-[var(--text-muted)]">{ap.description}</p>
                </div>
                <Badge color={ap.status === 'COMPLETED' ? 'success' : ap.status === 'IN_PROGRESS' ? 'blue' : 'gray'}>{ap.status}</Badge>
              </div>
            ))}
          </div>
        ) : (
          <EmptyState icon={HiOutlineClipboardCheck} title="No action plans" description="Action plans are generated from insight reports." />
        )
      )}
    </div>
  )
}
