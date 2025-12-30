import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { Button, Card, Badge, Dropdown, DropdownItem, DropdownDivider, Alert, Modal, ModalHeader, ModalBody, ModalContext, ModalFooter, modalTheme, Select, Label, Spinner } from 'flowbite-react'
import PaymentModal from '../components/PaymentModal'
import { useMySurveys, useActivateSurvey } from '../hooks/useApi'
import { billingAPI, distributionAPI, surveyAPI } from '../services/apiServices'
import { HiPlus, HiEye, HiPencil, HiPlay, HiStop, HiCurrencyDollar, HiShare, HiDotsVertical, HiExclamationCircle, HiPaperAirplane } from 'react-icons/hi'

const Dashboard = () => {
  const { data: surveys, isLoading, refetch, error: surveysError } = useMySurveys()
  const [paymentModalOpen, setPaymentModalOpen] = useState(false)
  const [selectedSurvey, setSelectedSurvey] = useState(null)
  const [activationError, setActivationError] = useState('')
  
  // Financial State
  const [subscription, setSubscription] = useState(null)
  const [walletBalance, setWalletBalance] = useState(0)
  
  // Distribution State
  const [showDistModal, setShowDistModal] = useState(false)
  const [distLists, setDistLists] = useState([])
  const [selectedListId, setSelectedListId] = useState('')
  const [isSending, setIsSending] = useState(false)

  const activateSurveyMutation = useActivateSurvey()

  useEffect(() => {
    // Prefetch billing info for checks
    billingAPI.getSubscription().then(res => setSubscription(res.data)).catch(console.error)
    billingAPI.getWalletBalance().then(res => setWalletBalance(res.data)).catch(console.error)
    distributionAPI.getLists().then(res => setDistLists(res.data)).catch(console.error)
  }, [])

  const handleActivateSurvey = async (survey) => {
    setActivationError('')
    
    // Check 1: Free Tier Limits
    const isFreeTier = !subscription || subscription.plan?.name === 'Free' || subscription.status !== 'ACTIVE'
    if (isFreeTier) {
      if (survey.targetRespondents > 25) {
        setActivationError('Free tier is limited to 25 respondents. Please upgrade your plan.')
        return
      }
    }

    // Check 2: Financials (Rewards)
    const estimatedCost = (survey.targetRespondents || 0) * (survey.rewardAmount || 0)
    
    if (estimatedCost > 0) {
      if (walletBalance < estimatedCost) {
        // Prompt for payment/topup
        setSelectedSurvey(survey)
        setPaymentModalOpen(true)
        return
      }
    }

    // Attempt Activation
    try {
      await activateSurveyMutation.mutateAsync(survey.id)
      refetch()
    } catch (error) {
      console.error('Failed to activate survey:', error)
      const msg = error.response?.data?.message || 'Failed to activate survey.'
      
      // If backend says insufficient funds
      if (msg.includes('fund') || msg.includes('wallet')) {
         setSelectedSurvey(survey)
         setPaymentModalOpen(true)
      } else {
         setActivationError(msg)
      }
    }
  }

  const handlePaymentSuccess = () => {
    setPaymentModalOpen(false)
    // If we just paid/topped up, try activating again if survey is selected
    if (selectedSurvey) {
       // Refresh balance first
       billingAPI.getWalletBalance().then(res => {
         setWalletBalance(res.data)
         // Then activate
         activateSurveyMutation.mutateAsync(selectedSurvey.id)
           .then(() => {
             setSelectedSurvey(null)
             refetch()
           })
           .catch(err => setActivationError(err.response?.data?.message || 'Activation failed after payment'))
       })
    } else {
      refetch()
    }
  }
  
  const openDistModal = (survey) => {
    setSelectedSurvey(survey)
    setShowDistModal(true)
  }
  
  const handleSendToDistList = async () => {
    if (!selectedSurvey) return
    // Note: If backend requires list ID, pass it. Assuming current endpoint sends to *linked* list.
    // If we need to link it first, we'd call an update endpoint.
    // For now, assuming standard flow.
    setIsSending(true)
        try {
          await surveyAPI.sendToDistributionList(selectedSurvey.id, selectedListId)
          setShowDistModal(false)
          setSelectedListId('')
          // Set a success state or just refetch
          refetch()
        } catch (error) {
          console.error('Distribution failed', error)
          setActivationError('Failed to send: ' + (error.response?.data?.message || error.message))
        } finally {
          setIsSending(false)
        }  }

  const getSurveyStatusBadge = (status) => {
    const statusConfig = {
      DRAFT: { color: 'gray', text: 'Draft' },
      ACTIVE: { color: 'success', text: 'Active' },
      PAUSED: { color: 'warning', text: 'Paused' },
      CLOSED: { color: 'failure', text: 'Closed' },
      COMPLETED: { color: 'info', text: 'Completed' },
      EXPIRED: { color: 'failure', text: 'Expired' }
    }
    
    const config = statusConfig[status] || statusConfig.DRAFT
    return <Badge color={config.color}>{config.text}</Badge>
  }

  const getSurveyUrl = (survey) => {
    // Use short code if available, otherwise use ID
    return survey.url_code 
      ? `${window.location.origin}/s/${survey.url_code}`
      : `${window.location.origin}/survey/${survey.id}`
  }

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text)
    // You could add a toast notification here
  }

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">My Surveys</h1>
          <p className="text-gray-600 mt-1">Manage your surveys</p>
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

      {activationError && (
        <Alert color="failure" icon={HiExclamationCircle} onDismiss={() => setActivationError('')}>
          {activationError}
          {activationError.includes('upgrade') && (
            <Link to="/subscriptions" className="font-bold underline ml-2">Go to Subscriptions</Link>
          )}
        </Alert>
      )}

      {surveysError && (
        <Alert color="failure" icon={HiExclamationCircle}>
          Failed to load surveys. Please try refreshing the page.
        </Alert>
      )}

      {/* Surveys List */}
      <div className="space-y-6">
        {isLoading ? (
          <div className="text-center py-8">
            <Spinner size="xl" />
            <p className="text-gray-500 mt-2">Loading surveys...</p>
          </div>
        ) : surveys?.length ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {surveys.map((survey) => (
              <Card key={survey.id} className="hover:shadow-lg transition-shadow">
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <h3 className="text-lg font-semibold text-gray-900 mb-2 truncate" title={survey.name}>
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
                    {survey.status === 'DRAFT' && (
                      <DropdownItem as={Link} to={`/survey-builder?edit=${survey.id}`}>
                        <HiPencil className="w-4 h-4 mr-2" />
                        Edit
                      </DropdownItem>
                    )}
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
                      <>
                        <DropdownItem>
                          <HiStop className="w-4 h-4 mr-2" />
                          Pause
                        </DropdownItem>
                        {survey.status === 'ACTIVE' && (
                           <DropdownItem onClick={() => openDistModal(survey)}>
                             <HiPaperAirplane className="w-4 h-4 mr-2" />
                             Send to List
                           </DropdownItem>
                        )}
                      </>
                    )}
                  </Dropdown>
                </div>

                <div className="space-y-3">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Responses</span>
                    <span className="font-medium">{survey.responseCount || 0}</span>
                  </div>
                  
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Created</span>
                    <span className="font-medium">{new Date(survey.createdAt).toLocaleDateString()}</span>
                  </div>
                  
                  {survey.rewardAmount && (
                    <div className="flex justify-between text-sm">
                      <span className="text-gray-600">Reward</span>
                      <span className="font-medium text-green-600">
                        ${survey.rewardAmount}
                      </span>
                    </div>
                  )}

                  <div className="pt-3 border-t border-gray-200">
                    <div className="flex gap-2">
                      <Button
                        as={Link}
                        to={`/dashboard/survey/${survey.id}`}
                        size="sm"
                        color="gray"
                        className="flex-1"
                      >
                        <HiEye className="w-4 h-4 mr-1" />
                        Manage
                      </Button>
                      
                      {survey.status === 'DRAFT' && (
                        <Button
                          size="sm"
                          onClick={() => handleActivateSurvey(survey)}
                          disabled={activateSurveyMutation.isLoading}
                          className="bg-primary-500 hover:bg-primary-600 flex-1"
                        >
                          {activateSurveyMutation.isLoading && selectedSurvey?.id === survey.id ? (
                            <Spinner size="sm" className="mr-1" />
                          ) : (
                            <HiPlay className="w-4 h-4 mr-1" />
                          )}
                          Activate
                        </Button>
                      )}
                      
                      {!survey.rewardAmount && survey.status !== 'DRAFT' && (
                        <Button
                          as={Link}
                          to={`/dashboard/survey/${survey.id}`}
                          state={{ tab: 'rewards' }}
                          size="sm"
                          color="warning"
                          className="flex-1"
                        >
                          <HiCurrencyDollar className="w-4 h-4 mr-1" />
                          Rewards
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

      {/* Payment Modal */}
      <PaymentModal
        show={paymentModalOpen}
        onClose={() => {
          setPaymentModalOpen(false)
          setSelectedSurvey(null)
        }}
        survey={selectedSurvey}
        mode={selectedSurvey ? 'SURVEY_ACTIVATION' : 'WALLET_TOPUP'}
        onPaymentSuccess={handlePaymentSuccess}
      />
      
      {/* Distribution Modal */}
      <Modal show={showDistModal} onClose={() => setShowDistModal(false)} size="md">
        <ModalHeader>Send Survey</ModalHeader>
        <ModalBody>
          <div className="space-y-4">
             <p className="text-gray-600">Select a contact list to send SMS invitations to.</p>
             <div>
               <Label htmlFor="dList" value="Distribution List" />
               <Select 
                 id="dList"
                 value={selectedListId}
                 onChange={(e) => setSelectedListId(e.target.value)}
               >
                 <option value="">-- Select List --</option>
                 {distLists.map(l => (
                   <option key={l.id} value={l.id}>{l.name} ({l.contacts?.length} contacts)</option>
                 ))}
               </Select>
             </div>
             <div className="flex justify-end gap-2">
               <Button color="gray" onClick={() => setShowDistModal(false)}>Cancel</Button>
               <Button onClick={handleSendToDistList} disabled={!selectedListId || isSending}>
                 {isSending ? <Spinner size="sm" className="mr-2" /> : <HiPaperAirplane className="mr-2 h-5 w-5" />}
                 {isSending ? 'Sending...' : 'Send'}
               </Button>
             </div>
          </div>
        </ModalBody>
      </Modal>
    </div>
  )
}

export default Dashboard
