import AnalyticsDashboard from '../components/AnalyticsDashboard'
import { useMySurveys } from '../hooks/useApi'

const DashboardAnalytics = () => {
  const { data: surveys } = useMySurveys()

  return (
    <div className="space-y-6">
      <h1 className="text-3xl font-bold text-gray-900">Analytics</h1>
      <p className="text-gray-600 mt-1">View real-time and historical survey analytics</p>
      <AnalyticsDashboard surveys={surveys || []} />
    </div>
  )
}

export default DashboardAnalytics
