import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Button, Card, Badge, Tabs, TabItem, Dropdown, DropdownItem, DropdownDivider } from 'flowbite-react'
import NavBar from '../components/NavBar'
import AnalyticsDashboard from '../components/AnalyticsDashboard'
import PaymentModal from '../components/PaymentModal'
import { useMySurveys, useActivateSurvey } from '../hooks/useApi'
import { HiPlus, HiEye, HiPencil, HiPlay, HiStop, HiCurrencyDollar, HiShare, HiDotsVertical } from 'react-icons/hi'

const Dashboard = () => {
  const { data: surveys, isLoading, refetch } = useMySurveys()
  const [paymentModalOpen, setPaymentModalOpen] = useState(false)
  const [selectedSurvey, setSelectedSurvey] = useState(null)
  
  const activateSurveyMutation = useActivateSurvey()

  const handleActivateSurvey = async (survey) => {
    // Check if payment is required
    if (survey.requiresPayment) {
      setSelectedSurvey(survey)
      setPaymentModalOpen(true)
    } else {
      try {
        await activateSurveyMutation.mutateAsync(survey.id)
        refetch()
      } catch (error) {
        console.error('Failed to activate survey:', error)
      }
    }
  }

  const handlePaymentSuccess = () => {
    setPaymentModalOpen(false)
    setSelectedSurvey(null)
    refetch()
  }

  const getSurveyStatusBadge = (status) => {
    const statusConfig = {
      DRAFT: { color: 'gray', text: 'Draft' },
      ACTIVE: { color: 'success', text: 'Active' },
      PAUSED: { color: 'warning', text: 'Paused' },
      COMPLETED: { color: 'info', text: 'Completed' },
      EXPIRED: { color: 'failure', text: 'Expired' }
    }
    
    const config = statusConfig[status] || statusConfig.DRAFT
    return <Badge color={config.color}>{config.text}</Badge>
  }

  const getSurveyUrl = (survey) => {
    // Use short code if available, otherwise use ID
    return survey.shortCode 
      ? `${window.location.origin}/s/${survey.shortCode}`
      : `${window.location.origin}/survey/${survey.id}`
  }

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text)
    // You could add a toast notification here
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <NavBar />
      
      <main className="flex-grow max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="flex items-center justify-between mb-8">
          <div>
            <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
            <p className="text-gray-600 mt-1">Manage your surveys and view analytics</p>
          </div>
          <Button
            as={Link}
            to="/survey-builder"
            className="bg-primary-500 hover:bg-primary-600 text-white"
          >
            <HiPlus className="w-4 h-4 mr-2" />
            Create Survey
          </Button>
        </div>

        {/* Tabs */}
        <Tabs aria-label="Dashboard tabs" variant="underline">
          <TabItem active title="My Surveys" icon={HiEye}>
            {/* Surveys Tab Content */}
            <div className="space-y-6">
              {isLoading ? (
                <div className="text-center py-8">
                  <p className="text-gray-500">Loading surveys...</p>
                </div>
              ) : surveys?.length ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  {surveys.map((survey) => (
                    <Card key={survey.id} className="hover:shadow-lg transition-shadow">
                      <div className="flex items-start justify-between mb-4">
                        <div className="flex-1">
                          <h3 className="text-lg font-semibold text-gray-900 mb-2">
                            {survey.name}
                          </h3>
                          <div className="flex items-center gap-2 mb-2">
                            <Badge color="info" size="sm">{survey.type}</Badge>
                            {getSurveyStatusBadge(survey.status)}
                          </div>
                        </div>
                        
                        <Dropdown
                          arrowIcon={false}
                          inline
                          label={<HiDotsVertical className="w-5 h-5 text-gray-400" />}
                        >
                          <DropdownItem as={Link} to={`/survey/${survey.id}/edit`}>
                            <HiPencil className="w-4 h-4 mr-2" />
                            Edit
                          </DropdownItem>
                          <DropdownItem onClick={() => copyToClipboard(getSurveyUrl(survey))}>
                            <HiShare className="w-4 h-4 mr-2" />
                            Copy Link
                          </DropdownItem>
                          <DropdownDivider />
                          {survey.status === 'DRAFT' ? (
                            <DropdownItem onClick={() => handleActivateSurvey(survey)}>
                              <HiPlay className="w-4 h-4 mr-2" />
                              Activate
                            </DropdownItem>
                          ) : (
                            <DropdownItem>
                              <HiStop className="w-4 h-4 mr-2" />
                              Pause
                            </DropdownItem>
                          )}
                        </Dropdown>
                      </div>

                      <div className="space-y-3">
                        <div className="flex justify-between text-sm">
                          <span className="text-gray-600">Responses</span>
                          <span className="font-medium">{survey.responseCount || 0}</span>
                        </div>
                        
                        <div className="flex justify-between text-sm">
                          <span className="text-gray-600">Completion Rate</span>
                          <span className="font-medium">{survey.completionRate || 0}%</span>
                        </div>
                        
                        {survey.rewardAmount && (
                          <div className="flex justify-between text-sm">
                            <span className="text-gray-600">Reward per Response</span>
                            <span className="font-medium text-green-600">
                              ${survey.rewardAmount}
                            </span>
                          </div>
                        )}

                        <div className="pt-3 border-t border-gray-200">
                          <div className="flex gap-2">
                            <Button
                              as={Link}
                              to={`/survey/${survey.id}`}
                              size="sm"
                              color="gray"
                              className="flex-1"
                            >
                              <HiEye className="w-4 h-4 mr-1" />
                              View
                            </Button>
                            
                            {survey.status === 'DRAFT' && (
                              <Button
                                size="sm"
                                onClick={() => handleActivateSurvey(survey)}
                                disabled={activateSurveyMutation.isLoading}
                                className="bg-primary-500 hover:bg-primary-600 flex-1"
                              >
                                <HiPlay className="w-4 h-4 mr-1" />
                                Activate
                              </Button>
                            )}
                            
                            {!survey.rewardAmount && survey.status !== 'DRAFT' && (
                              <Button
                                size="sm"
                                color="warning"
                                onClick={() => {
                                  setSelectedSurvey(survey)
                                  setPaymentModalOpen(true)
                                }}
                                className="flex-1"
                              >
                                <HiCurrencyDollar className="w-4 h-4 mr-1" />
                                Add Rewards
                              </Button>
                            )}
                          </div>
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>
              ) : (
                <Card>
                  <div className="text-center py-12">
                    <HiEye className="w-12 h-12 text-gray-400 mx-auto mb-4" />
                    <h3 className="text-lg font-medium text-gray-900 mb-2">
                      No surveys yet
                    </h3>
                    <p className="text-gray-600 mb-6">
                      Create your first survey to get started with collecting responses and insights.
                    </p>
                    <Button
                      as={Link}
                      to="/survey-builder"
                      className="bg-primary-500 hover:bg-primary-600"
                    >
                      <HiPlus className="w-4 h-4 mr-2" />
                      Create Your First Survey
                    </Button>
                  </div>
                </Card>
              )}
            </div>
          </TabItem>

          <TabItem title="Analytics" icon={HiCurrencyDollar}>
            {/* Analytics Tab Content */}
            <AnalyticsDashboard surveys={surveys || []} />
          </TabItem>
        </Tabs>
      </main>

      {/* Payment Modal */}
      <PaymentModal
        show={paymentModalOpen}
        onClose={() => {
          setPaymentModalOpen(false)
          setSelectedSurvey(null)
        }}
        survey={selectedSurvey}
        onPaymentSuccess={handlePaymentSuccess}
      />
    </div>
  )
}

export default Dashboard