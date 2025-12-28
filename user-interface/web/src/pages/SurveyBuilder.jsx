import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { Button, Card, Badge, Radio, Label, TextInput, Select, Textarea, Spinner, Alert, Progress } from 'flowbite-react'
import { useTemplatesByType, useCreateSurvey, useSurvey } from '../hooks/useApi'
import { aiAPI, distributionAPI, billingAPI, surveyAPI } from '../services/apiServices'
import useSurveyStore from '../stores/surveyStore'
import { HiPlus, HiTrash, HiLightningBolt, HiSparkles, HiArrowLeft, HiArrowRight, HiCheck } from 'react-icons/hi'

const SurveyBuilder = () => {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const editSurveyId = searchParams.get('edit')
  
  const [step, setStep] = useState(1) // 1: Type/AI, 2: Templates/AI-Input, 3: Questions, 4: Settings, 5: Launch
  const { 
    currentSurvey, 
    updateSurvey, 
    addQuestion, 
    removeQuestion, 
    updateQuestion, 
    setSelectedTemplate, 
    resetSurveyBuilder 
  } = useSurveyStore()
  
  // Local state for UI
  const [surveyType, setSurveyType] = useState('')
  const [isAiMode, setIsAiMode] = useState(false)
  const [aiTopic, setAiTopic] = useState('')
  const [aiType, setAiType] = useState('')
  const [aiSector, setAiSector] = useState('')
  const [aiQuestionCount, setAiQuestionCount] = useState('')
  const [isGenerating, setIsGenerating] = useState(false)
  const [aiError, setAiError] = useState('')
  const [distributionLists, setDistributionLists] = useState([])
  const [selectedListId, setSelectedListId] = useState('')
  const [subscription, setSubscription] = useState(null)
  const [creationError, setCreationError] = useState('')

  const createSurveyMutation = useCreateSurvey()
  const { data: templates, isLoading: templatesLoading } = useTemplatesByType(surveyType)
  
  // Load existing survey if editing
  useEffect(() => {
    if (editSurveyId) {
      surveyAPI.getSurvey(editSurveyId).then(res => {
        const s = res.data
        updateSurvey({
          name: s.name,
          introduction: s.introduction,
          type: s.type,
          accessType: s.accessType,
          startDate: s.startDate,
          endDate: s.endDate,
          targetRespondents: s.targetRespondents,
          questions: s.questions?.map(q => ({
            id: q.id,
            text: q.questionText,
            type: q.questionType,
            required: true, // Assuming default
            options: q.options ? JSON.parse(q.options) : []
          })) || []
        })
        setSurveyType(s.type)
        setStep(3) // Jump to questions
      }).catch(console.error)
    } else {
      resetSurveyBuilder()
    }
  }, [editSurveyId])

  useEffect(() => {
    distributionAPI.getLists().then(res => setDistributionLists(res.data)).catch(console.error)
    billingAPI.getSubscription().then(res => setSubscription(res.data)).catch(console.error)
  }, [])

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
      const qs = template.questions?.map((q, idx) => ({
        id: Date.now() + idx,
        text: q.questionText,
        type: q.questionType,
        required: true,
        options: q.options ? JSON.parse(q.options) : []
      })) || []
      updateSurvey({ questions: qs })
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

  const handleSave = async () => {
    setCreationError('')
    try {
      const surveyData = {
        name: currentSurvey.name,
        introduction: currentSurvey.introduction || '',
        type: currentSurvey.type,
        accessType: currentSurvey.accessType,
        startDate: currentSurvey.startDate,
        endDate: currentSurvey.endDate,
        targetRespondents: parseInt(currentSurvey.targetRespondents || 0),
        budget: null, // Budget handled on activation
        questions: currentSurvey.questions.map((q, index) => ({
          questionText: q.text,
          questionType: q.type,
          position: index,
          options: JSON.stringify(q.options || []),
        }))
      }
      
      if (editSurveyId) {
        await surveyAPI.updateSurvey(editSurveyId, surveyData)
      } else {
        const res = await createSurveyMutation.mutateAsync(surveyData)
        const newSurveyId = res.data.id
        if (selectedListId) {
           // If list selected, link it (backend support needed or separate call)
           // For now just logging, as primary distribution is in Dashboard
           console.log("Selected list ID:", selectedListId, "for survey", newSurveyId)
        }
      }

      navigate('/dashboard')
    } catch (err) {
      console.error('Failed to save survey:', err)
      setCreationError(err.response?.data?.message || err.message || 'Failed to save survey')
    }
  }

  const isSmsAllowed = subscription?.plan?.name !== 'Free'

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Progress Stepper */}
      <div className="mb-8">
        <div className="flex justify-between mb-2">
           <span className="text-xs font-medium text-gray-500 uppercase">Method</span>
           <span className="text-xs font-medium text-gray-500 uppercase">Content</span>
           <span className="text-xs font-medium text-gray-500 uppercase">Questions</span>
           <span className="text-xs font-medium text-gray-500 uppercase">Settings</span>
           <span className="text-xs font-medium text-gray-500 uppercase">Review</span>
        </div>
        <Progress progress={(step / 5) * 100} size="sm" color="purple" />
      </div>

      <div className="mb-6 flex justify-between items-center">
        <h1 className="text-3xl font-bold text-gray-900">{editSurveyId ? 'Edit Survey' : 'Create Survey'}</h1>
        {creationError && <Alert color="failure">{creationError}</Alert>}
      </div>

      {/* Step 1: Method Selection */}
      {step === 1 && (
        <Card>
          <h2 className="text-xl font-semibold mb-4">How do you want to start?</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div 
               onClick={handleAiModeSelection}
               className="border-2 border-transparent hover:border-purple-500 bg-purple-50 p-6 rounded-xl cursor-pointer transition-all text-center group">
               <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm group-hover:scale-110 transition-transform">
                  <HiSparkles className="w-8 h-8 text-purple-600" />
               </div>
               <h3 className="font-bold text-gray-900 text-lg">Generate with AI</h3>
               <p className="text-sm text-gray-600 mt-2">Describe your goal and let our AI build the perfect survey for you.</p>
            </div>

            <div className="border-2 border-transparent hover:border-blue-500 bg-blue-50 p-6 rounded-xl cursor-pointer transition-all text-center group">
               <div className="w-16 h-16 bg-white rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm group-hover:scale-110 transition-transform">
                  <HiPlus className="w-8 h-8 text-blue-600" />
               </div>
               <h3 className="font-bold text-gray-900 text-lg">Start from Scratch</h3>
               <p className="text-sm text-gray-600 mt-2">Choose a survey type and build your questions manually.</p>
               <div className="mt-4 flex flex-wrap justify-center gap-2">
                 {['NPS', 'CES', 'CSAT'].map(type => (
                   <Badge key={type} color="gray" className="cursor-pointer" onClick={(e) => { e.stopPropagation(); handleTypeSelection(type); }}>{type}</Badge>
                 ))}
               </div>
            </div>
          </div>
        </Card>
      )}

      {/* Step 2: AI Input or Templates */}
      {step === 2 && isAiMode && (
         <Card>
           <h2 className="text-xl font-semibold mb-4 flex items-center gap-2">
             <HiSparkles className="text-purple-600" /> AI Generator
           </h2>
           <div className="space-y-4">
             <div>
               <Label htmlFor="aiTopic">What is this survey about?</Label>
               <Textarea
                 id="aiTopic"
                 rows={4}
                 placeholder="e.g. Gather feedback from employees about the new health insurance benefits..."
                 value={aiTopic}
                 onChange={(e) => setAiTopic(e.target.value)}
                 className="mt-1"
               />
             </div>
             {aiError && <Alert color="failure">{aiError}</Alert>}
             <div className="flex justify-between pt-4">
               <Button color="gray" onClick={() => setStep(1)}>Back</Button>
               <Button color="purple" onClick={handleAiGenerate} disabled={isGenerating || !aiTopic}>
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
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold">Select a Template for {surveyType}</h2>
            <Button color="light" onClick={() => handleTemplateSelection(null)}>Skip to Blank</Button>
          </div>
          
          {templatesLoading ? <div className="text-center py-8"><Spinner /></div> : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {templates?.map(t => (
                <Card key={t.id} className="cursor-pointer hover:ring-2 hover:ring-blue-500" onClick={() => handleTemplateSelection(t)}>
                  <h3 className="font-bold">{t.name}</h3>
                  <p className="text-sm text-gray-500 line-clamp-3">{t.description}</p>
                  <Badge className="w-fit mt-2">{t.questions?.length} Questions</Badge>
                </Card>
              ))}
              {!templates?.length && (
                <div className="col-span-full text-center py-8 bg-gray-50 rounded-lg border-2 border-dashed">
                  <p className="text-gray-500">No templates found for {surveyType}.</p>
                  <Button size="sm" className="mt-4" onClick={() => handleTemplateSelection(null)}>Create Blank Survey</Button>
                </div>
              )}
            </div>
          )}
          <Button color="gray" onClick={() => setStep(1)} className="mt-4">Back</Button>
        </div>
      )}

      {/* Step 3: Questions Editor */}
      {step === 3 && (
        <div className="space-y-6">
          <Card className="sticky top-4 z-10 bg-white shadow-md border-b-4 border-blue-500">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-bold">Edit Questions</h2>
              <div className="flex gap-2">
                <Button color="gray" size="sm" onClick={() => setStep(2)}>Back</Button>
                <Button onClick={() => setStep(4)} size="sm" disabled={currentSurvey.questions.length === 0}>
                  Next: Settings <HiArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </div>
            </div>
          </Card>

          <div className="space-y-4">
            {currentSurvey.questions.map((q, idx) => (
              <Card key={q.id} className="relative group">
                <div className="absolute right-4 top-4 opacity-0 group-hover:opacity-100 transition-opacity">
                  <Button color="failure" size="xs" onClick={() => removeQuestion(q.id)}><HiTrash /></Button>
                </div>
                
                <div className="flex gap-4">
                  <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center font-bold text-gray-500 flex-shrink-0">
                    {idx + 1}
                  </div>
                  <div className="flex-grow space-y-3">
                    <TextInput 
                      value={q.text} 
                      onChange={(e) => updateQuestion(q.id, { text: e.target.value })} 
                      placeholder="Question text..."
                      required
                    />
                    
                    <div className="flex gap-4 items-center">
                      <Select 
                        value={q.type} 
                        onChange={(e) => updateQuestion(q.id, { type: e.target.value })}
                        className="w-48"
                      >
                        <option value="FREE_TEXT">Free Text</option>
                        <option value="MULTIPLE_CHOICE_SINGLE">Single Choice</option>
                        <option value="MULTIPLE_CHOICE_MULTI">Multiple Choice</option>
                        <option value="RATING_LINEAR">Linear Scale (1-10)</option>
                        <option value="RATING_STAR">Star Rating</option>
                        <option value="NPS_SCALE">NPS (0-10)</option>
                      </Select>
                      <Label className="flex items-center gap-2">
                        <input type="checkbox" checked={q.required} onChange={(e) => updateQuestion(q.id, { required: e.target.checked })} />
                        Required
                      </Label>
                    </div>

                    {(q.type === 'MULTIPLE_CHOICE_SINGLE' || q.type === 'MULTIPLE_CHOICE_MULTI') && (
                      <div className="pl-4 border-l-2 border-gray-200 space-y-2">
                        {q.options?.map((opt, oIdx) => (
                          <TextInput 
                            key={oIdx} 
                            value={opt} 
                            size="sm" 
                            onChange={(e) => {
                              const newOpts = [...q.options];
                              newOpts[oIdx] = e.target.value;
                              updateQuestion(q.id, { options: newOpts });
                            }}
                            placeholder={`Option ${oIdx + 1}`}
                          />
                        ))}
                        <Button size="xs" color="light" onClick={() => updateQuestion(q.id, { options: [...(q.options || []), ''] })}>
                          + Add Option
                        </Button>
                      </div>
                    )}
                  </div>
                </div>
              </Card>
            ))}
            
            <Button color="light" className="w-full border-dashed border-2" onClick={handleAddQuestion}>
              <HiPlus className="mr-2 h-5 w-5" /> Add Question
            </Button>
          </div>
        </div>
      )}

      {/* Step 4: Settings */}
      {step === 4 && (
        <Card className="max-w-2xl mx-auto">
          <h2 className="text-xl font-semibold mb-6">Survey Settings</h2>
          <div className="space-y-4">
            <div>
              <Label>Survey Name</Label>
              <TextInput value={currentSurvey.name} onChange={(e) => updateSurvey({ name: e.target.value })} required />
            </div>
            <div>
              <Label>Introduction / Welcome Message</Label>
              <Textarea 
                value={currentSurvey.introduction || ''} 
                onChange={(e) => updateSurvey({ introduction: e.target.value })} 
                placeholder="Welcome to our survey..." 
                rows={3}
              />
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label>Access Type</Label>
                <Select value={currentSurvey.accessType} onChange={(e) => updateSurvey({ accessType: e.target.value })}>
                  <option value="PUBLIC">Public Link</option>
                  <option value="PRIVATE">Invitation Only</option>
                </Select>
              </div>
              <div>
                <Label>Target Responses (Optional)</Label>
                <TextInput 
                  type="number" 
                  value={currentSurvey.targetRespondents || ''} 
                  onChange={(e) => updateSurvey({ targetRespondents: e.target.value })} 
                  placeholder="Unlimited"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label>Start Date</Label>
                <TextInput type="datetime-local" value={currentSurvey.startDate || ''} onChange={(e) => updateSurvey({ startDate: e.target.value })} />
              </div>
              <div>
                <Label>End Date</Label>
                <TextInput type="datetime-local" value={currentSurvey.endDate || ''} onChange={(e) => updateSurvey({ endDate: e.target.value })} />
              </div>
            </div>
          </div>
          
          <div className="flex justify-between mt-6">
            <Button color="gray" onClick={() => setStep(3)}>Back</Button>
            <Button onClick={() => setStep(5)} disabled={!currentSurvey.name}>Review & Launch</Button>
          </div>
        </Card>
      )}

      {/* Step 5: Review */}
      {step === 5 && (
        <Card className="max-w-2xl mx-auto">
          <div className="text-center mb-6">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <HiCheck className="w-8 h-8 text-green-600" />
            </div>
            <h2 className="text-2xl font-bold">Ready to Launch!</h2>
            <p className="text-gray-600">Review your survey details before creating.</p>
          </div>

          <div className="bg-gray-50 rounded-lg p-6 space-y-3 text-sm">
            <div className="flex justify-between border-b pb-2">
              <span className="font-semibold">Name</span>
              <span>{currentSurvey.name}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="font-semibold">Type</span>
              <Badge>{currentSurvey.type}</Badge>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="font-semibold">Questions</span>
              <span>{currentSurvey.questions.length}</span>
            </div>
            <div className="flex justify-between border-b pb-2">
              <span className="font-semibold">Access</span>
              <span>{currentSurvey.accessType}</span>
            </div>
          </div>

          {/* Distribution List Selection (Optional) */}
          <div className="mt-4">
            <Label>Send via SMS (Optional)</Label>
            <Select 
              value={selectedListId} 
              onChange={(e) => setSelectedListId(e.target.value)}
              disabled={!isSmsAllowed}
            >
              <option value="">-- Do not send automatically --</option>
              {distributionLists.map(l => (
                <option key={l.id} value={l.id}>{l.name} ({l.contacts?.length} contacts)</option>
              ))}
            </Select>
            {!isSmsAllowed && <p className="text-xs text-red-500 mt-1">Upgrade plan to enable SMS distribution.</p>}
          </div>

          <div className="flex justify-between mt-8">
            <Button color="gray" onClick={() => setStep(4)}>Back</Button>
            <Button color="purple" size="xl" onClick={handleSave}>
              {editSurveyId ? 'Update Survey' : 'Create Survey'}
            </Button>
          </div>
        </Card>
      )}
    </div>
  )
}

export default SurveyBuilder
