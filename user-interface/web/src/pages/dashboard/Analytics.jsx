import { useMySurveys } from '../../hooks/useApi'
import AnalyticsDashboard from '../../components/AnalyticsDashboard'
import { SkeletonCard } from '../../components/ui/Skeleton'

export default function Analytics() {
  const { data: surveys, isLoading } = useMySurveys()

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-[var(--text)]">Analytics</h1>
        <p className="text-sm text-[var(--text-muted)] mt-0.5">Real-time and historical survey analytics</p>
      </div>
      {isLoading ? (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}
        </div>
      ) : (
        <AnalyticsDashboard surveys={surveys || []} />
      )}
    </div>
  )
}
