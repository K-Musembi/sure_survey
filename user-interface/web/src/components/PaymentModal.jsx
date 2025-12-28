import { useState, useEffect } from 'react'
import { Modal, Button, Label, TextInput, Select, Alert } from 'flowbite-react'
import { useCreatePayment, useVerifyPayment } from '../hooks/useApi'
import { paymentAPI } from '../services/apiServices'
import { HiCreditCard, HiCheckCircle, HiExclamationCircle } from 'react-icons/hi'

const PaymentModal = ({ show, onClose, survey, mode = 'SURVEY_ACTIVATION', onPaymentSuccess }) => {
  const [step, setStep] = useState(1) // 1: Details, 2: Processing, 3: Success/Error
  const [paymentData, setPaymentData] = useState({
    amount: '',
    currency: 'USD',
    surveyId: survey?.id || '',
    idempotencyKey: ''
  })
  const [paymentReference, setPaymentReference] = useState('')
  const [error, setError] = useState('')
  const [isLoading, setIsLoading] = useState(false)

  // Use raw API for flexibility based on mode
  // const createPaymentMutation = useCreatePayment() // Legacy hook

  const generateIdempotencyKey = () => {
    return `pay_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`
  }
  
  // Reset form when modal opens
  useEffect(() => {
    if (show) {
      setStep(1)
      setPaymentData({
        amount: '',
        currency: 'USD',
        surveyId: survey?.id || (mode === 'WALLET_TOPUP' ? 'WALLET_TOPUP' : ''),
        idempotencyKey: ''
      })
      setError('')
    }
  }, [show, survey, mode])

  const handlePaymentSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setIsLoading(true)
    
    try {
      const payload = {
        amount: paymentData.amount,
        currency: paymentData.currency,
        surveyId: mode === 'WALLET_TOPUP' ? 'WALLET_TOPUP' : survey?.id?.toString(),
        idempotencyKey: generateIdempotencyKey()
      }
      
      let response;
      if (mode === 'WALLET_TOPUP') {
        response = await paymentAPI.topUpWallet(payload)
      } else {
        response = await paymentAPI.initiatePayment(payload)
      }
      
      // Redirect to Paystack payment page
      if (response.data.authorizationUrl) {
        window.location.href = response.data.authorizationUrl
        setStep(2) // In case they come back, though page reload happens usually
      } else if (response.data.authorization_url) {
        // Handle snake_case response if API differs
        window.location.href = response.data.authorization_url
        setStep(2)
      } else {
        setError('Payment initialization failed: No authorization URL returned.')
        setStep(1)
      }
    } catch (error) {
      console.error("Payment error", error)
      setError(error.response?.data?.message || 'Payment initialization failed')
      setStep(1)
    } finally {
      setIsLoading(false)
    }
  }

  const handleModalClose = () => {
    setStep(1)
    setError('')
    setPaymentReference('')
    onClose()
  }

  // Calculate estimated survey cost
  const estimatedCost = survey?.targetRespondents && survey?.rewardAmount
    ? survey.targetRespondents * survey.rewardAmount
    : 0

  return (
    <Modal show={show} onClose={handleModalClose} size="md">
      <Modal.Header>
        <div className="flex items-center">
          <HiCreditCard className="w-5 h-5 mr-2 text-primary-600" />
          {mode === 'WALLET_TOPUP' ? 'Wallet Top Up' : 'Survey Payment'}
        </div>
      </Modal.Header>

      <Modal.Body>
        {/* Step 1: Payment Details */}
        {step === 1 && (
          <form onSubmit={handlePaymentSubmit} className="space-y-4">
            {error && (
              <Alert color="failure" icon={HiExclamationCircle}>
                {error}
              </Alert>
            )}

            {mode === 'SURVEY_ACTIVATION' && (
              <div className="bg-gray-50 rounded-lg p-4 mb-4">
                <h3 className="font-medium mb-2">Survey Details</h3>
                <div className="text-sm space-y-1">
                  <div><span className="font-medium">Name:</span> {survey?.name}</div>
                  <div><span className="font-medium">Type:</span> {survey?.type}</div>
                  <div><span className="font-medium">Target Responses:</span> {survey?.targetRespondents || 'Not set'}</div>
                  {estimatedCost > 0 && (
                    <div><span className="font-medium">Estimated Cost:</span> ${estimatedCost}</div>
                  )}
                </div>
              </div>
            )}

            {mode === 'WALLET_TOPUP' && (
               <div className="bg-blue-50 rounded-lg p-4 mb-4">
                 <p className="text-sm text-blue-800">
                   Add funds to your wallet to pay for survey rewards and premium features.
                 </p>
               </div>
            )}

            <div>
              <Label htmlFor="amount">Payment Amount</Label>
              <TextInput
                id="amount"
                type="number"
                step="0.01"
                min="1"
                placeholder="Enter amount"
                value={paymentData.amount}
                onChange={(e) => setPaymentData(prev => ({...prev, amount: e.target.value}))}
                required
              />
              <p className="text-xs text-gray-500 mt-1">
                {mode === 'WALLET_TOPUP' 
                  ? 'Enter the amount you wish to add to your wallet.' 
                  : 'This covers survey activation and reward distribution costs.'}
              </p>
            </div>

            <div>
              <Label htmlFor="currency">Currency</Label>
              <Select
                id="currency"
                value={paymentData.currency}
                onChange={(e) => setPaymentData(prev => ({...prev, currency: e.target.value}))}
              >
                <option value="USD">USD - US Dollar</option>
                <option value="KES">KES - Kenyan Shilling</option>
                <option value="NGN">NGN - Nigerian Naira</option>
                <option value="GHS">GHS - Ghanaian Cedi</option>
                <option value="ZAR">ZAR - South African Rand</option>
              </Select>
            </div>

            <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
              <div className="flex items-start">
                <HiExclamationCircle className="w-5 h-5 text-blue-600 mr-2 mt-0.5" />
                <div className="text-sm text-blue-800">
                  <p className="font-medium">Payment powered by Paystack</p>
                  <p>You will be redirected to Paystack's secure payment page to complete this transaction.</p>
                </div>
              </div>
            </div>

            <div className="flex justify-end space-x-3 pt-4">
              <Button color="gray" onClick={handleModalClose}>
                Cancel
              </Button>
              <Button 
                type="submit" 
                disabled={!paymentData.amount || isLoading}
                className="bg-primary-500 hover:bg-primary-600"
              >
                {isLoading ? 'Processing...' : 'Continue to Payment'}
              </Button>
            </div>
          </form>
        )}

        {/* Step 2: Processing */}
        {step === 2 && (
          <div className="text-center py-8">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500 mx-auto mb-4"></div>
            <h3 className="text-lg font-medium mb-2">Redirecting to Payment</h3>
            <p className="text-gray-600">Please wait while we redirect you to Paystack...</p>
          </div>
        )}
        
        {/* NOTE: Success step is usually handled by callback URL page, but keeping basic structure just in case */}
      </Modal.Body>
    </Modal>
  )
}

export default PaymentModal