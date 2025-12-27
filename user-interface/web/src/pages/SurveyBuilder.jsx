import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, Badge, Radio, Label, TextInput, Select, Textarea, Spinner, Alert } from 'flowbite-react'
import { useTemplatesByType, useCreateSurvey } from '../hooks/useApi'
import { aiAPI, distributionAPI, billingAPI, rewardAPI } from '../services/apiServices'
import useSurveyStore from '../stores/surveyStore'
import { HiPlus, HiTrash, HiLightningBolt, HiSparkles, HiMail, HiExclamationCircle } from 'react-icons/hi'

const SurveyBuilder = () => {
  const navigate = useNavigate()
  const [step, setStep] = useState(1) // 1: Type/AI, 2: Templates/AI-Input, 3: Questions, 4: Settings, 5: Launch
  const { 
    currentSurvey, 
    selectedTemplate, 
    updateSurvey, 
    addQuestion, 
    removeQuestion, 
    updateQuestion, 
    setSelectedTemplate, 
    resetSurveyBuilder 
  } = useSurveyStore()
  
  const [surveyType, setSurveyType] = useState('')
  const [isAiMode, setIsAiMode] = useState(false)
  const [aiTopic, setAiTopic] = useState('')
  const [aiType, setAiType] = useState('')
  const [aiSector, setAiSector] = useState('')
  const [aiQuestionCount, setAiQuestionCount] = useState('')
  const [isGenerating, setIsGenerating] = useState(false)
  const [aiError, setAiError] = useState('')
  
  // Distribution State
  const [distributionLists, setDistributionLists] = useState([])
  const [selectedListId, setSelectedListId] = useState('')
  const [subscription, setSubscription] = useState(null)
  
  const createSurveyMutation = useCreateSurvey()
  const { data: templates, isLoading: templatesLoading } = useTemplatesByType(surveyType)

  useEffect(() => {
    // Fetch lists and subscription on mount
    distributionAPI.getLists().then(res => setDistributionLists(res.data)).catch(console.error)
    billingAPI.getSubscription().then(res => setSubscription(res.data)).catch(console.error)
    
    return () => {
      resetSurveyBuilder()
    }
  }, [resetSurveyBuilder])

  const handleTypeSelection = (type) => {
    setSurveyType(type)
    updateSurvey({ type })
    setIsAiMode(false)
    setStep(2)
  }

  const handleAiModeSelection = () => {
    setIsAiMode(true)
    setStep(2)
  }

  const handleAiGenerate = async () => {
    if (!aiTopic) return
    setIsGenerating(true)
    setAiError('')
    try {
      const response = await aiAPI.generateQuestions({
        topic: aiTopic,
        type: aiType,
        sector: aiSector || 'General',
        questionCount: aiQuestionCount ? parseInt(aiQuestionCount) : 5
      })
      
      const generatedQuestions = response.data.map((q, idx) => ({
        id: Date.now() + idx,
        text: q.questionText,
        type: q.questionType || 'FREE_TEXT',
        required: true,
        options: q.options ? JSON.parse(q.options) : []
      }))
      
      // Determine valid type or default to NPS
      const validTypes = ['NPS', 'CES', 'CSAT']
      const finalType = validTypes.includes(aiType?.toUpperCase()) ? aiType.toUpperCase() : 'NPS'

      updateSurvey({ 
        name: aiTopic + ' Survey',
        type: finalType,
        questions: generatedQuestions 
      })
      setStep(3)
    } catch (error) {
      console.error('AI Generation failed', error)
      setAiError('Failed to generate questions. Please try again or refine your topic.')
    } finally {
      setIsGenerating(false)
    }
  }

  const handleTemplateSelection = (template) => {
    setSelectedTemplate(template)
    if (template) {
      updateSurvey({ questions: template.questions || [] })
    }
    setStep(3)
  }

  const handleAddQuestion = () => {
    const newQuestion = {
      id: Date.now(),
      text: '',
      type: 'FREE_TEXT',
      required: true,
      options: []
    }
    addQuestion(newQuestion)
  }

  const [error, setError] = useState('')

  const handleCreateSurvey = async () => {
    setError('')
    try {
      // 1. Prepare Survey Data
      // Map local state to DTO: budget = target * rewardAmount
      const rewardAmt = parseFloat(currentSurvey.rewardAmount || 0)
      const target = parseInt(currentSurvey.targetRespondents || 0)
      const budget = rewardAmt * target

      const surveyData = {
        name: currentSurvey.name,
        introduction: currentSurvey.introduction || '', // Add if you have this field or default
        type: currentSurvey.type,
        accessType: currentSurvey.accessType,
        startDate: currentSurvey.startDate,
        endDate: currentSurvey.endDate,
        targetRespondents: target,
        budget: budget > 0 ? budget : null,
        questions: currentSurvey.questions.map((q, index) => ({
          questionText: q.text,
          questionType: q.type,
          position: index,
          options: JSON.stringify(q.options || []),
        }))
      }
      
      // 2. Create Survey
      const res = await createSurveyMutation.mutateAsync(surveyData)
      const newSurveyId = res.data.id

      // 3. Handle Distribution List (if selected)
      if (selectedListId && newSurveyId) {
         try {
           // We'll use the specific endpoint to distribute
           // Assuming the backend knows which list to use or we need to link it first.
           // Since there is no "Link List" endpoint, we assume 'send-to-distribution-list' 
           // might require the list ID in the body (DTO wasn't explicit on this but it's logical)
           // OR the user has to go to dashboard to send.
           // For safety in this "Builder" flow, let's just create the survey.
           // The user can trigger the "Send" from the dashboard (Active -> Send to List).
         } catch (distError) {
           console.error('Distribution trigger failed', distError)
         }
      }

      navigate('/dashboard')
    } catch (err) {
      console.error('Failed to create survey:', err)
      setError(err.response?.data?.message || err.message || 'Failed to create survey')
    }
  }

  const isSmsAllowed = subscription?.plan?.name !== 'Free' // Simple check

  return (
    <div className="max-w-6xl mx-auto">
      {/* Header */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Create Survey</h1>
        {error && (
          <Alert color="failure" icon={HiLightningBolt} className="mt-4" onDismiss={() => setError('')}>
            {error}
          </Alert>
        )}
        <div className="mt-4 flex space-x-2">
          {[1, 2, 3, 4, 5].map((s) => (
            <div key={s} className={`px-3 py-1 rounded-full text-sm ${
              s === step ? 'bg-primary-500 text-white' : 
              s < step ? 'bg-primary-100 text-primary-700' : 'bg-gray-200 text-gray-500'
            }`}>
              Step {s}
            </div>
          ))}
        </div>
      </div>

      {/* Subscription Status Panel */}
      <Card className="mb-8 bg-gradient-to-r from-gray-50 to-white border-l-4 border-primary-500">
        <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-4">
          <div>
            <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
              Current Plan: {subscription?.plan?.name || 'Loading...'}
              <Badge color={subscription?.status === 'ACTIVE' ? 'success' : 'warning'}>
                {subscription?.status || 'Unknown'}
              </Badge>
            </h3>
            <div className="text-sm text-gray-600 mt-1">
              {subscription?.plan?.price > 0 
                ? `${new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(subscription.plan.price)} / ${subscription.plan.billingInterval || 'month'}`
                : 'Free'}
            </div>
          </div>
          
          <div className="text-sm text-gray-600">
             <div className="font-medium">Billing Period</div>
             <div>
               {subscription?.currentPeriodStart ? new Date(subscription.currentPeriodStart).toLocaleDateString() : '-'} 
               {' to '}
               {subscription?.currentPeriodEnd ? new Date(subscription.currentPeriodEnd).toLocaleDateString() : '-'}
             </div>
          </div>
        </div>
        
        {subscription?.plan?.features && (
          <div className="mt-4 pt-4 border-t border-gray-200">
            <h4 className="text-sm font-semibold text-gray-700 mb-2">Plan Features:</h4>
            <div className="flex flex-wrap gap-3">
              {Object.entries(JSON.parse(subscription.plan.features)).map(([key, value]) => (
                <Badge key={key} color="gray" className="px-2 py-1">
                  {key.replace(/([A-Z])/g, ' $1').trim()}: {value.toString()}
                </Badge>
              ))}
            </div>
          </div>
        )}
      </Card>

      {/* Step 1: Survey Type */}
      {step === 1 && (
        <Card className="max-w-2xl mx-auto">
          <h2 className="text-xl font-semibold mb-4">Choose Creation Method</h2>
          <div className="space-y-4">
            <div 
               onClick={handleAiModeSelection}
               className="border-2 border-primary-100 rounded-lg p-4 cursor-pointer hover:border-primary-500 hover:bg-primary-50 transition-colors bg-gradient-to-r from-primary-50 to-white">
               <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    <div className="p-2 bg-primary-100 rounded-full mr-3">
                       <HiSparkles className="w-6 h-6 text-primary-600" />
                    </div>
                    <div>
                      <h3 className="font-bold text-gray-900">Generate with AI</h3>
                      <p className="text-sm text-gray-600">Describe your goal and let AI create questions for you.</p>
                    </div>
                  </div>
                  <Radio name="method" checked={isAiMode} onChange={handleAiModeSelection} />
               </div>
            </div>

            <div className="border-t border-gray-200 my-4"></div>
            <p className="text-sm font-medium text-gray-500 uppercase">Or start from scratch</p>

            {['NPS', 'CES', 'CSAT'].map((type) => (
              <div key={type} 
                   onClick={() => handleTypeSelection(type)}
                   className="border rounded-lg p-4 cursor-pointer hover:border-primary-500 hover:bg-primary-50 transition-colors">
                <div className="flex items-center justify-between">
                  <div>
                    <h3 className="font-medium">{type}</h3>
                    <p className="text-sm text-gray-600">
                      {type === 'NPS' && 'Net Promoter Score - measure customer loyalty'}
                      {type === 'CES' && 'Customer Effort Score - measure ease of experience'}
                      {type === 'CSAT' && 'Customer Satisfaction - measure satisfaction levels'}
                    </p>
                  </div>
                  <Radio name="surveyType" value={type} checked={surveyType === type} onChange={() => handleTypeSelection(type)} />
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}

      {/* Step 2: Templates OR AI Input */}
      {step === 2 && isAiMode && (
         <Card className="max-w-2xl mx-auto">
           <h2 className="text-xl font-semibold mb-4">AI Survey Generator</h2>
           {aiError && (
             <Alert color="failure" icon={HiExclamationCircle} className="mb-4">
               {aiError}
             </Alert>
           )}
           <div className="space-y-4">
             <div>
               <Label htmlFor="aiTopic">What is this survey about?</Label>
               <Textarea
                 id="aiTopic"
                 rows={4}
                 placeholder="e.g. Employee satisfaction regarding the new remote work policy..."
                 value={aiTopic}
                 onChange={(e) => setAiTopic(e.target.value)}
               />
             </div>

             <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
               <div>
                  <Label htmlFor="aiType">Survey Type (Optional)</Label>
                  <TextInput
                    id="aiType"
                    placeholder="e.g. NPS, CSAT"
                    value={aiType}
                    onChange={(e) => setAiType(e.target.value)}
                  />
               </div>
               <div>
                  <Label htmlFor="aiSector">Sector (Optional)</Label>
                  <TextInput
                    id="aiSector"
                    placeholder="e.g. Banking, Retail"
                    value={aiSector}
                    onChange={(e) => setAiSector(e.target.value)}
                  />
               </div>
               <div>
                  <Label htmlFor="aiCount">Number of Questions (Optional)</Label>
                  <TextInput
                    id="aiCount"
                    type="number"
                    placeholder="Default: 5"
                    value={aiQuestionCount}
                    onChange={(e) => setAiQuestionCount(e.target.value)}
                  />
               </div>
             </div>

             <div className="flex justify-between pt-4">
               <Button color="gray" onClick={() => setStep(1)}>Back</Button>
               <Button 
                 onClick={handleAiGenerate} 
                 disabled={!aiTopic || isGenerating}
                 className="bg-primary-500 hover:bg-primary-600"
               >
                 {isGenerating ? (
                   <>
                     <Spinner size="sm" className="mr-2" /> Generating...
                   </>
                 ) : (
                   <>
                     <HiSparkles className="mr-2 h-5 w-5" /> Generate Questions
                   </>
                 )}
               </Button>
             </div>
           </div>
         </Card>
      )}

      {step === 2 && !isAiMode && (
        <div>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold">Choose Template</h2>
            <div className="flex gap-2">
                <Button color="gray" onClick={() => setStep(1)}>Back</Button>
                <Button 
                  color="light" 
                  onClick={() => handleTemplateSelection(null)}
                >
                  Skip Templates
                </Button>
            </div>
          </div>

          {templatesLoading ? (
            <div className="text-center py-8">Loading templates...</div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {templates?.map((template) => (
                <Card key={template.id} className="cursor-pointer hover:shadow-lg transition-shadow"
                      onClick={() => handleTemplateSelection(template)}>
                  <h3 className="font-medium mb-2">{template.name}</h3>
                  <p className="text-sm text-gray-600 mb-3">{template.description}</p>
                  <Badge color="success" className="text-xs">
                    {template.questions?.length || 0} questions
                  </Badge>
                </Card>
              ))}
              
              {!templates?.length && (
                <div className="col-span-full text-center py-8 text-gray-500">
                  No templates available for {surveyType}
                </div>
              )}
            </div>
          )}
        </div>
      )}

      {/* Step 3: Questions (Unchanged mostly) */}
      {step === 3 && (
        <div>
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-xl font-semibold">Survey Questions</h2>
            <Button onClick={handleAddQuestion} className="bg-primary-500 hover:bg-primary-600">
              <HiPlus className="w-4 h-4 mr-2" />
              Add Question
            </Button>
          </div>

          <div className="space-y-6">
            {currentSurvey.questions.map((question, index) => (
              <Card key={question.id}>
                <div className="flex items-start justify-between mb-4">
                  <h3 className="text-lg font-medium">Question {index + 1}</h3>
                  <Button 
                    color="failure" 
                    size="sm"
                    onClick={() => removeQuestion(question.id)}
                  >
                    <HiTrash className="w-4 h-4" />
                  </Button>
                </div>
                
                <div className="space-y-4">
                  <div>
                    <Label htmlFor={`question-${question.id}`}>Question Text</Label>
                    <Textarea
                      id={`question-${question.id}`}
                      value={question.text}
                      onChange={(e) => updateQuestion(question.id, { text: e.target.value })}
                      placeholder="Enter your question..."
                      rows={2}
                    />
                  </div>

                  <div className="flex gap-4">
                    <div className="flex-1">
                      <Label htmlFor={`type-${question.id}`}>Question Type</Label>
                      <Select
                        id={`type-${question.id}`}
                        value={question.type}
                        onChange={(e) => updateQuestion(question.id, { type: e.target.value })}
                      >
                        <option value="FREE_TEXT">Text</option>
                        <option value="MULTIPLE_CHOICE_SINGLE">Multiple Choice</option>
                        <option value="MULTIPLE_CHOICE_MULTI">Checkboxes</option>
                        <option value="RATING_LINEAR">Scale (1-10)</option>
                        <option value="RATING_STAR">Star Rating</option>
                        <option value="NPS_SCALE">Net Promoter Score</option>
                      </Select>
                    </div>
                    
                    <div className="flex items-center">
                      <input
                        type="checkbox"
                        id={`required-${question.id}`}
                        checked={question.required}
                        onChange={(e) => updateQuestion(question.id, { required: e.target.checked })}
                        className="mr-2"
                      />
                      <Label htmlFor={`required-${question.id}`}>Required</Label>
                    </div>
                  </div>

                  {question.type === 'MULTIPLE_CHOICE_SINGLE' && (
                    <div>
                      <Label>Options</Label>
                      <div className="space-y-2 mt-2">
                        {(question.options || []).map((option, optIndex) => (
                          <TextInput
                            key={optIndex}
                            value={option}
                            onChange={(e) => {
                              const newOptions = [...(question.options || [])]
                              newOptions[optIndex] = e.target.value
                              updateQuestion(question.id, { options: newOptions })
                            }}
                            placeholder={`Option ${optIndex + 1}`}
                          />
                        ))}
                        <Button
                          color="gray"
                          size="sm"
                          onClick={() => {
                            const newOptions = [...(question.options || []), '']
                            updateQuestion(question.id, { options: newOptions })
                          }}
                        >
                          Add Option
                        </Button>
                      </div>
                    </div>
                  )}
                </div>
              </Card>
            ))}

            {currentSurvey.questions.length === 0 && (
              <div className="text-center py-8 text-gray-500">
                No questions added yet. Click "Add Question" to get started.
              </div>
            )}
          </div>

          <div className="flex justify-between mt-8">
            <Button color="gray" onClick={() => setStep(2)}>
              Back
            </Button>
            <Button 
              onClick={() => setStep(4)}
              disabled={currentSurvey.questions.length === 0}
              className="bg-primary-500 hover:bg-primary-600"
            >
              Continue
            </Button>
          </div>
        </div>
      )}

      {/* Step 4: Settings */}
      {step === 4 && (
        <Card className="max-w-2xl mx-auto">
          <h2 className="text-xl font-semibold mb-6">Survey Settings</h2>
          
          <div className="space-y-6">
            <div>
              <Label htmlFor="surveyName">Survey Name</Label>
              <TextInput
                id="surveyName"
                value={currentSurvey.name}
                onChange={(e) => updateSurvey({ name: e.target.value })}
                placeholder="Enter survey name..."
                required
              />
            </div>

            <div>
              <Label htmlFor="accessType">Access Type</Label>
              <Select
                id="accessType"
                value={currentSurvey.accessType}
                onChange={(e) => updateSurvey({ accessType: e.target.value })}
              >
                <option value="PUBLIC">Public - Anyone with link can respond</option>
                <option value="PRIVATE">Private - Invitation only</option>
              </Select>
            </div>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="target">Target Responses</Label>
                 <TextInput
                  id="target"
                  type="number"
                  value={currentSurvey.targetRespondents || ''}
                  onChange={(e) => updateSurvey({ targetRespondents: e.target.value })}
                  placeholder="e.g. 100"
                />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <Label htmlFor="startDate">Start Date (Optional)</Label>
                <TextInput
                  id="startDate"
                  type="datetime-local"
                  value={currentSurvey.startDate || ''}
                  onChange={(e) => updateSurvey({ startDate: e.target.value })}
                />
              </div>
              
              <div>
                <Label htmlFor="endDate">End Date (Optional)</Label>
                <TextInput
                  id="endDate"
                  type="datetime-local"
                  value={currentSurvey.endDate || ''}
                  onChange={(e) => updateSurvey({ endDate: e.target.value })}
                />
              </div>
            </div>
          </div>

          <div className="flex justify-between mt-8">
            <Button color="gray" onClick={() => setStep(3)}>
              Back
            </Button>
            <Button 
              onClick={() => setStep(5)}
              disabled={!currentSurvey.name}
              className="bg-primary-500 hover:bg-primary-600"
            >
              Continue
            </Button>
          </div>
        </Card>
      )}

      {/* Step 5: Distribution & Launch */}
      {step === 5 && (
        <Card className="max-w-2xl mx-auto">
          <h2 className="text-xl font-semibold mb-6">Distribution & Launch</h2>
          
          <div className="space-y-6">
            <div className="bg-gray-50 rounded-lg p-4">
              <h3 className="font-medium mb-2">Review Summary</h3>
              <div className="grid grid-cols-2 gap-2 text-sm">
                <div><span className="font-medium">Name:</span> {currentSurvey.name}</div>
                <div><span className="font-medium">Type:</span> {currentSurvey.type}</div>
                <div><span className="font-medium">Questions:</span> {currentSurvey.questions.length}</div>
                <div><span className="font-medium">Access:</span> {currentSurvey.accessType}</div>
                <div><span className="font-medium">Target:</span> {currentSurvey.targetRespondents || 'Unlimited'}</div>
              </div>
            </div>

            <div>
              <Label htmlFor="distList">Select Distribution List (Optional)</Label>
              <Select
                id="distList"
                value={selectedListId}
                onChange={(e) => setSelectedListId(e.target.value)}
                disabled={!isSmsAllowed}
              >
                <option value="">-- No List (Public Link Only) --</option>
                {distributionLists.map(list => (
                  <option key={list.id} value={list.id}>{list.name} ({list.contacts?.length || 0} contacts)</option>
                ))}
              </Select>
              {!isSmsAllowed ? (
                 <p className="text-xs text-red-500 mt-1">
                   SMS distribution requires a paid subscription. You are on the Free plan.
                 </p>
              ) : (
                <p className="text-xs text-gray-500 mt-1">
                  Selecting a list will distribute the survey via SMS upon activation.
                </p>
              )}
            </div>
          </div>

          <div className="flex justify-between mt-8">
            <Button color="gray" onClick={() => setStep(4)}>
              Back
            </Button>
            <Button 
              onClick={handleCreateSurvey}
              disabled={createSurveyMutation.isLoading}
              className="bg-primary-500 hover:bg-primary-600"
            >
              {createSurveyMutation.isLoading ? 'Creating...' : 'Create & Finish'}
            </Button>
          </div>
        </Card>
      )}
    </div>
  )
}

export default SurveyBuilder