import { useState, useEffect } from 'react'
import { useParams, Link, useLocation } from 'react-router-dom'
import { Card, Button, Badge, Tabs, Alert, Spinner } from 'flowbite-react'
import { useSurvey, useActivateSurvey } from '../hooks/useApi'
import AnalyticsDashboard from '../components/AnalyticsDashboard'
import SurveyContacts from '../components/SurveyContacts'
import SurveyIntegrations from '../components/SurveyIntegrations'
import SurveyRewards from '../components/SurveyRewards'
import PaymentModal from '../components/PaymentModal'
import { HiChartPie, HiUserGroup, HiLightningBolt, HiCurrencyDollar, HiPlay, HiStop, HiEye, HiPencil } from 'react-icons/hi'

const SurveyDetails = () => {
  const { surveyId } = useParams()
  const location = useLocation()
  const { data: survey, isLoading, error, refetch } = useSurvey(surveyId)
  const activateSurveyMutation = useActivateSurvey()
  
  const [showPaymentModal, setShowPaymentModal] = useState(false)
  const [activeTab, setActiveTab] = useState(0)

  // Handle tab state from location state if present
  useEffect(() => {
    if (location.state?.tab === 'rewards') setActiveTab(3)
  }, [location.state])

  // Need to wrap the survey in an array for AnalyticsDashboard
  const surveysForAnalytics = survey ? [survey] : []

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner size="xl" />
      </div>
    )
  }

  if (error || !survey) {
    return (
      <div className="text-center py-12">
        <h3 className="text-lg text-gray-900">Survey not found</h3>
        <Button as={Link} to="/dashboard" className="mt-4">Back to Dashboard</Button>
      </div>
    )
  }

  const handleActivate = async () => {
    try {
      await activateSurveyMutation.mutateAsync(survey.id)
      refetch()
    } catch (err) {
      console.error(err)
      // If payment needed, show modal
      if (err.response?.data?.message?.includes('fund')) {
        setShowPaymentModal(true)
      }
    }
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-3xl font-bold text-gray-900">{survey.name}</h1>
            <Badge color={survey.status === 'ACTIVE' ? 'success' : 'gray'}>
              {survey.status}
            </Badge>
          </div>
          <p className="text-gray-600 mt-1">
             Type: {survey.type} | Created: {new Date(survey.createdAt).toLocaleDateString()}
          </p>
        </div>
        
        <div className="flex gap-2">
           <Button color="light" as={Link} to={`/survey/${survey.id}`} target="_blank">
             <HiEye className="mr-2 h-4 w-4" /> Preview
           </Button>
           {survey.status === 'DRAFT' && (
             <>
               <Button color="light" as={Link} to={`/survey-builder?edit=${survey.id}`}>
                 <HiPencil className="mr-2 h-4 w-4" /> Edit
               </Button>
               <Button onClick={handleActivate} disabled={activateSurveyMutation.isLoading}>
                 {activateSurveyMutation.isLoading ? <Spinner size="sm" className="mr-2" /> : <HiPlay className="mr-2 h-4 w-4" />} 
                 Activate
               </Button>
             </>
           )}
        </div>
      </div>

      <Tabs 
        aria-label="Survey details tabs" 
        variant="underline"
        onActiveTabChange={(tab) => setActiveTab(tab)}
      >
        <Tabs.Item active={activeTab === 0} icon={HiChartPie} title="Overview">
           <div className="mt-4">
             <AnalyticsDashboard surveys={surveysForAnalytics} />
           </div>
        </Tabs.Item>

        <Tabs.Item active={activeTab === 1} icon={HiUserGroup} title="Distribution">
           <div className="mt-4">
             <SurveyContacts surveyId={survey.id} />
           </div>
        </Tabs.Item>

        <Tabs.Item active={activeTab === 2} icon={HiLightningBolt} title="Integrations">
           <div className="mt-4">
             <SurveyIntegrations surveyId={survey.id} />
           </div>
        </Tabs.Item>

        <Tabs.Item active={activeTab === 3} icon={HiCurrencyDollar} title="Rewards">
           <div className="mt-4">
             <SurveyRewards survey={survey} onUpdate={refetch} />
           </div>
        </Tabs.Item>
      </Tabs>

      {/* Payment Modal for Rewards/Activation */}
      <PaymentModal 
        show={showPaymentModal}
        onClose={() => setShowPaymentModal(false)}
        survey={survey}
        mode="SURVEY_ACTIVATION"
        onPaymentSuccess={() => {
          setShowPaymentModal(false)
          refetch()
        }}
      />
    </div>
  )
}

export default SurveyDetails
