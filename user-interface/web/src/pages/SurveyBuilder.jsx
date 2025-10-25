import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, Badge, Radio, Label, TextInput, Select, Textarea } from 'flowbite-react'
import NavBar from '../components/NavBar'
import { useTemplatesByType, useCreateSurvey } from '../hooks/useApi'
import useSurveyStore from '../stores/surveyStore'
import { HiPlus, HiTrash, HiLightningBolt } from 'react-icons/hi'

const SurveyBuilder = () => {
  const navigate = useNavigate()
  const [step, setStep] = useState(1) // 1: Type, 2: Templates, 3: Questions, 4: Settings, 5: Rewards
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
  const createSurveyMutation = useCreateSurvey()
  const { data: templates, isLoading: templatesLoading } = useTemplatesByType(surveyType)

  useEffect(() => {
    return () => {
      // Reset survey builder on unmount
      resetSurveyBuilder()
    }
  }, [resetSurveyBuilder])

  const handleTypeSelection = (type) => {
    setSurveyType(type)
    updateSurvey({ type })
    setStep(2)
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
      type: 'TEXT',
      required: true,
      options: []
    }
    addQuestion(newQuestion)
  }

  const handleCreateSurvey = async () => {
    try {
      const surveyData = {
        ...currentSurvey,
        questions: currentSurvey.questions.map(q => ({
          text: q.text,
          type: q.type,
          required: q.required,
          options: q.options || []
        }))
      }
      await createSurveyMutation.mutateAsync(surveyData)
      navigate('/dashboard')
    } catch (error) {
      console.error('Failed to create survey:', error)
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-gray-50">
      <NavBar />
      
      <main className="flex-grow max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <h1 className="text-3xl font-bold text-gray-900">Create Survey</h1>
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

        {/* Step 1: Survey Type */}
        {step === 1 && (
          <Card className="max-w-2xl mx-auto">
            <h2 className="text-xl font-semibold mb-4">Choose Survey Type</h2>
            <div className="space-y-4">
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
                    <Radio name="surveyType" value={type} checked={surveyType === type} />
                  </div>
                </div>
              ))}
            </div>
          </Card>
        )}

        {/* Step 2: Templates */}
        {step === 2 && (
          <div>
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold">Choose Template</h2>
              <Button 
                color="gray" 
                onClick={() => handleTemplateSelection(null)}
                className="text-sm"
              >
                Skip Templates
              </Button>
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
                
                {/* Empty state */}
                {!templates?.length && (
                  <div className="col-span-full text-center py-8 text-gray-500">
                    No templates available for {surveyType}
                  </div>
                )}
              </div>
            )}
          </div>
        )}

        {/* Step 3: Questions */}
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
                          <option value="TEXT">Text</option>
                          <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                          <option value="SCALE">Scale (1-10)</option>
                          <option value="YES_NO">Yes/No</option>
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

                    {question.type === 'MULTIPLE_CHOICE' && (
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

        {/* Step 5: Rewards & Create */}
        {step === 5 && (
          <Card className="max-w-2xl mx-auto">
            <h2 className="text-xl font-semibold mb-6">Rewards & Launch</h2>
            
            <div className="space-y-6">
              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
                <div className="flex items-center">
                  <HiLightningBolt className="w-5 h-5 text-yellow-600 mr-2" />
                  <span className="text-sm font-medium text-yellow-800">
                    Rewards can be added after creating the survey
                  </span>
                </div>
              </div>

              <div className="bg-gray-50 rounded-lg p-4">
                <h3 className="font-medium mb-2">Survey Summary</h3>
                <div className="text-sm space-y-1">
                  <div><span className="font-medium">Name:</span> {currentSurvey.name}</div>
                  <div><span className="font-medium">Type:</span> {currentSurvey.type}</div>
                  <div><span className="font-medium">Questions:</span> {currentSurvey.questions.length}</div>
                  <div><span className="font-medium">Access:</span> {currentSurvey.accessType}</div>
                </div>
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
                {createSurveyMutation.isLoading ? 'Creating...' : 'Create Survey'}
              </Button>
            </div>
          </Card>
        )}
      </main>
    </div>
  )
}

export default SurveyBuilder