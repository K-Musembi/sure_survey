import { useState, useEffect } from 'react'
import { Card, Button, Label, TextInput, Select, Table, Badge, Alert } from 'flowbite-react'
import { integrationAPI } from '../services/apiServices'
import { HiLightningBolt, HiClipboardCopy, HiCheck, HiExclamationCircle } from 'react-icons/hi'

const SurveyIntegrations = ({ surveyId }) => {
  const [integrations, setIntegrations] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [showCreateForm, setShowCreateForm] = useState(false)
  const [copySuccess, setCopySuccess] = useState('')
  const [fetchError, setFetchError] = useState('')

  // Form State
  const [formData, setFormData] = useState({
    businessName: '',
    type: 'MPESA_C2B',
    shortcode: '',
    consumerKey: '',
    consumerSecret: ''
  })
  const [createError, setCreateError] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const fetchIntegrations = async () => {
    setIsLoading(true)
    setFetchError('')
    try {
      const response = await integrationAPI.getIntegrations()
      // Filter for this survey if possible, assuming response data structure allows it.
      // Since DTO doesn't explicitly have surveyId in response, we might show all,
      // OR we assume the user only cares about integrations created FOR this survey context.
      // I'll filter if 'surveyId' field exists in the returned objects.
      const allIntegrations = Array.isArray(response.data) ? response.data : []
      const surveyIntegrations = allIntegrations.filter(i => i.surveyId === surveyId || i.surveyId === parseInt(surveyId))
      
      // Fallback: if 'surveyId' is not returned in listing, we show all (or none if we want to be strict).
      // Given the requirement "tied to a survey", showing all might be clutter.
      // But if filtering returns empty because field is missing, that's bad.
      // For safety, if any item has surveyId, we filter. If none do, we show all (legacy behavior).
      const hasSurveyIdField = allIntegrations.some(i => i.surveyId !== undefined)
      
      setIntegrations(hasSurveyIdField ? surveyIntegrations : allIntegrations)
    } catch (error) {
      console.error('Failed to fetch integrations', error)
      setFetchError('Failed to load integrations.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    if (surveyId) {
      fetchIntegrations()
    }
  }, [surveyId])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setCreateError('')
    setIsSubmitting(true)
    
    try {
      await integrationAPI.createIntegration({
        ...formData,
        surveyId: surveyId // Automatically link to this survey
      })
      setShowCreateForm(false)
      setFormData({
        businessName: '',
        type: 'MPESA_C2B',
        shortcode: '',
        consumerKey: '',
        consumerSecret: ''
      })
      fetchIntegrations()
    } catch (error) {
      setCreateError(error.response?.data?.message || 'Failed to create integration')
    } finally {
      setIsSubmitting(false)
    }
  }

  const copyToClipboard = (text) => {
    navigator.clipboard.writeText(text)
    setCopySuccess(text)
    setTimeout(() => setCopySuccess(''), 2000)
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
           <h3 className="text-xl font-bold text-gray-900">Business Integrations</h3>
           <p className="text-gray-600 text-sm">Connect payment systems to this survey.</p>
        </div>
        <Button onClick={() => setShowCreateForm(!showCreateForm)} size="sm">
          {showCreateForm ? 'Cancel' : 'New Integration'}
        </Button>
      </div>

      {fetchError && (
        <Alert color="failure" icon={HiExclamationCircle} onDismiss={() => setFetchError('')}>
          {fetchError}
        </Alert>
      )}

      {showCreateForm && (
        <Card className="border-l-4 border-primary-500">
          <h4 className="text-lg font-bold mb-4">Add Integration for Survey #{surveyId}</h4>
          {createError && <Alert color="failure" className="mb-4">{createError}</Alert>}
          
          <form onSubmit={handleSubmit} className="space-y-4">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="businessName">Business Name</Label>
                <TextInput
                  id="businessName"
                  placeholder="e.g. Main Branch Shop"
                  required
                  value={formData.businessName}
                  onChange={(e) => setFormData({...formData, businessName: e.target.value})}
                />
              </div>
              
              <div>
                <Label htmlFor="type">Integration Type</Label>
                <Select
                  id="type"
                  value={formData.type}
                  onChange={(e) => setFormData({...formData, type: e.target.value})}
                >
                  <option value="MPESA_C2B">M-Pesa Paybill/Buy Goods</option>
                  <option value="POS_GENERIC">Generic POS (API)</option>
                </Select>
              </div>
              
              <div>
                <Label htmlFor="shortcode">Shortcode / Till Number</Label>
                <TextInput
                  id="shortcode"
                  placeholder="e.g. 174379"
                  required
                  value={formData.shortcode}
                  onChange={(e) => setFormData({...formData, shortcode: e.target.value})}
                />
              </div>
            </div>

            <div className="bg-gray-50 p-4 rounded-lg border border-gray-200">
              <h4 className="font-medium text-gray-900 mb-2">Daraja API Credentials (Optional)</h4>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="consumerKey">Consumer Key</Label>
                  <TextInput
                    id="consumerKey"
                    placeholder="Key from Daraja"
                    value={formData.consumerKey}
                    onChange={(e) => setFormData({...formData, consumerKey: e.target.value})}
                  />
                </div>
                <div>
                  <Label htmlFor="consumerSecret">Consumer Secret</Label>
                  <TextInput
                    id="consumerSecret"
                    type="password"
                    placeholder="Secret from Daraja"
                    value={formData.consumerSecret}
                    onChange={(e) => setFormData({...formData, consumerSecret: e.target.value})}
                  />
                </div>
              </div>
            </div>

            <div className="flex justify-end">
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting ? 'Creating...' : 'Create Integration'}
              </Button>
            </div>
          </form>
        </Card>
      )}

      {/* Integrations List */}
      <div className="grid grid-cols-1 gap-4">
          {integrations.map((int) => (
            <Card key={int.id}>
              <div className="flex justify-between items-start">
                 <div>
                   <h5 className="font-bold text-gray-900">{int.businessName}</h5>
                   <div className="flex gap-2 mt-1">
                     <Badge color="info">{int.type}</Badge>
                     <Badge color={int.isActive ? 'success' : 'gray'}>
                        {int.isActive ? 'Active' : 'Pending'}
                     </Badge>
                   </div>
                 </div>
                 <div className="text-right">
                   <div className="text-sm font-mono bg-gray-100 px-2 py-1 rounded">
                     {int.shortcode}
                   </div>
                 </div>
              </div>
              
              <div className="mt-4 pt-4 border-t border-gray-100">
                <p className="text-xs text-gray-500 mb-1">Callback URL:</p>
                <div className="flex items-center gap-2 bg-gray-50 p-2 rounded">
                   <code className="text-xs text-blue-600 truncate flex-grow">
                     {int.callbackUrl}
                   </code>
                   <button 
                     onClick={() => copyToClipboard(int.callbackUrl)}
                     className="text-gray-500 hover:text-gray-700"
                   >
                     {copySuccess === int.callbackUrl ? <HiCheck className="text-green-500" /> : <HiClipboardCopy />}
                   </button>
                </div>
              </div>
            </Card>
          ))}
          
          {integrations.length === 0 && !isLoading && (
             <div className="text-center py-8 border-2 border-dashed border-gray-200 rounded-lg">
               <HiLightningBolt className="mx-auto h-8 w-8 text-gray-400" />
               <p className="mt-2 text-sm text-gray-500">No integrations configured for this survey.</p>
             </div>
          )}
      </div>
    </div>
  )
}

export default SurveyIntegrations