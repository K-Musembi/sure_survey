import { useState, useEffect } from 'react'
import { Card, Button, Label, TextInput, Select, Alert, Badge } from 'flowbite-react'
import { rewardAPI, billingAPI } from '../services/apiServices'
import PaymentModal from './PaymentModal'
import { HiCurrencyDollar, HiLightningBolt, HiExclamationCircle, HiCheckCircle } from 'react-icons/hi'

const SurveyRewards = ({ survey, onUpdate }) => {
  const [rewardConfig, setRewardConfig] = useState(null)
  const [walletBalance, setWalletBalance] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState('')
  const [showTopUpModal, setShowTopUpModal] = useState(false)

  // Form State
  const [formData, setFormData] = useState({
    rewardType: 'AIRTIME',
    amountPerRecipient: '',
    maxRecipients: survey.targetRespondents || 100,
    currency: 'KES',
    provider: 'AFRICASTALKING'
  })
  const [isSubmitting, setIsSubmitting] = useState(false)

  const fetchData = async () => {
    setIsLoading(true)
    try {
      const [balanceRes, rewardRes] = await Promise.allSettled([
        billingAPI.getWalletBalance(),
        rewardAPI.getSurveyReward(survey.id)
      ])

      if (balanceRes.status === 'fulfilled') {
        setWalletBalance(balanceRes.value.data)
      }
      
      if (rewardRes.status === 'fulfilled' && rewardRes.value.data) {
        setRewardConfig(rewardRes.value.data)
      }
    } catch (err) {
      console.error("Error fetching reward data", err)
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [survey.id])

  const calculateTotalCost = () => {
    const amount = parseFloat(formData.amountPerRecipient) || 0
    const count = parseInt(formData.maxRecipients) || 0
    return amount * count
  }

  const handleConfigureReward = async () => {
    setError('')
    setIsSubmitting(true)
    const totalCost = calculateTotalCost()

    if (totalCost > walletBalance) {
      setError(`Insufficient wallet balance. You need ${formData.currency} ${totalCost - walletBalance} more.`)
      setIsSubmitting(false)
      return
    }

    try {
      await rewardAPI.configureReward({
        surveyId: survey.id,
        ...formData,
        amountPerRecipient: parseFloat(formData.amountPerRecipient),
        maxRecipients: parseInt(formData.maxRecipients)
      })
      await fetchData()
      if (onUpdate) onUpdate()
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to configure rewards')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleCancelReward = async () => {
    if (!rewardConfig) return
    setIsSubmitting(true)
    try {
      await rewardAPI.cancelReward(rewardConfig.id)
      setRewardConfig(null)
      fetchData()
      if (onUpdate) onUpdate()
    } catch (err) {
      setError('Failed to cancel reward campaign')
    } finally {
      setIsSubmitting(false)
    }
  }

  if (isLoading) {
    return <div className="p-4 text-center">Loading reward details...</div>
  }

  const totalCost = calculateTotalCost()
  const isBalanceSufficient = walletBalance >= totalCost

  return (
    <div className="space-y-6">
      {/* Wallet Status */}
      <Card className="bg-gradient-to-r from-gray-50 to-white">
        <div className="flex justify-between items-center">
          <div>
            <h5 className="text-sm font-medium text-gray-500">Wallet Balance</h5>
            <div className="text-2xl font-bold text-gray-900">
              {new Intl.NumberFormat('en-KE', { style: 'currency', currency: 'KES' }).format(walletBalance)}
            </div>
          </div>
          <Button size="sm" gradientDuoTone="purpleToBlue" onClick={() => setShowTopUpModal(true)}>
            Top Up
          </Button>
        </div>
      </Card>

      {error && (
        <Alert color="failure" icon={HiExclamationCircle} onDismiss={() => setError('')}>
          {error}
        </Alert>
      )}

      {rewardConfig ? (
        <Card className="border-l-4 border-green-500">
          <div className="flex justify-between items-start">
             <div>
               <h3 className="text-xl font-bold text-gray-900 flex items-center gap-2">
                 Active Reward Campaign
                 <Badge color="success">Active</Badge>
               </h3>
               <div className="mt-4 grid grid-cols-2 gap-x-8 gap-y-2 text-sm text-gray-600">
                 <div>Type: <span className="font-medium text-gray-900">{rewardConfig.rewardType}</span></div>
                 <div>Value: <span className="font-medium text-gray-900">{rewardConfig.amountPerRecipient} {rewardConfig.currency}</span></div>
                 <div>Remaining: <span className="font-medium text-gray-900">{rewardConfig.remainingRewards}</span></div>
                 <div>Provider: <span className="font-medium text-gray-900">{rewardConfig.provider}</span></div>
               </div>
             </div>
             {rewardConfig.status === 'ACTIVE' && (
               <Button color="failure" size="xs" onClick={handleCancelReward} isProcessing={isSubmitting}>
                 Cancel Campaign
               </Button>
             )}
          </div>
        </Card>
      ) : (
        <Card>
          <h3 className="text-lg font-bold mb-4">Configure New Rewards</h3>
          <div className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="rType">Reward Type</Label>
                <Select
                  id="rType"
                  value={formData.rewardType}
                  onChange={(e) => setFormData({...formData, rewardType: e.target.value})}
                >
                  <option value="AIRTIME">Airtime</option>
                  <option value="DATA_BUNDLE">Data Bundle</option>
                  <option value="LOYALTY_POINTS">Loyalty Points</option>
                  <option value="VOUCHER">Voucher/Coupon</option>
                </Select>
              </div>
              
              <div>
                <Label htmlFor="currency">Currency</Label>
                <Select
                  id="currency"
                  value={formData.currency}
                  onChange={(e) => setFormData({...formData, currency: e.target.value})}
                >
                  <option value="KES">KES (Kenyan Shilling)</option>
                  <option value="USD">USD (US Dollar)</option>
                  <option value="NGN">NGN (Nigerian Naira)</option>
                </Select>
              </div>
              
              <div>
                <Label htmlFor="amount">Amount Per Person</Label>
                <TextInput
                  id="amount"
                  type="number"
                  placeholder="e.g. 50"
                  required
                  value={formData.amountPerRecipient}
                  onChange={(e) => setFormData({...formData, amountPerRecipient: e.target.value})}
                />
              </div>

              <div>
                <Label htmlFor="max">Max Recipients</Label>
                <TextInput
                  id="max"
                  type="number"
                  placeholder="e.g. 100"
                  required
                  value={formData.maxRecipients}
                  onChange={(e) => setFormData({...formData, maxRecipients: e.target.value})}
                />
              </div>
            </div>

            <div className={`p-4 rounded-lg border ${isBalanceSufficient ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
               <div className="flex justify-between items-center">
                 <div>
                   <span className="text-sm font-medium text-gray-600">Total Estimated Cost:</span>
                   <div className={`text-xl font-bold ${isBalanceSufficient ? 'text-green-700' : 'text-red-700'}`}>
                     {formData.currency} {totalCost}
                   </div>
                 </div>
                 {!isBalanceSufficient && (
                   <div className="text-right">
                      <div className="text-xs text-red-600 mb-1">Insufficient Balance</div>
                      <Button size="xs" gradientDuoTone="pinkToOrange" onClick={() => setShowTopUpModal(true)}>
                        Add Funds
                      </Button>
                   </div>
                 )}
                 {isBalanceSufficient && totalCost > 0 && (
                   <HiCheckCircle className="w-8 h-8 text-green-500" />
                 )}
               </div>
            </div>

            <Button 
              className="w-full" 
              disabled={!isBalanceSufficient || totalCost === 0 || isSubmitting}
              isProcessing={isSubmitting}
              onClick={handleConfigureReward}
            >
              <HiLightningBolt className="mr-2 h-4 w-4" />
              Activate Rewards
            </Button>
          </div>
        </Card>
      )}

      <PaymentModal
        show={showTopUpModal}
        onClose={() => setShowTopUpModal(false)}
        mode="WALLET_TOPUP"
        onPaymentSuccess={() => {
          setShowTopUpModal(false)
          fetchData() // Refresh balance
        }}
      />
    </div>
  )
}

export default SurveyRewards