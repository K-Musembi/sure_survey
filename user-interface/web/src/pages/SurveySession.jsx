import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Button, Card, Label, TextInput, Radio, Textarea, Progress } from 'flowbite-react'
import { useSurvey, useSubmitResponse } from '../hooks/useApi'
import { participantAPI } from '../services/apiServices'
import { HiCheckCircle, HiGift } from 'react-icons/hi'

const SurveySession = () => {
  const { surveyId, shortCode } = useParams()
  const navigate = useNavigate()
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0)
  const [responses, setResponses] = useState({})
  const [participant, setParticipant] = useState(null)
  const [showRewardForm, setShowRewardForm] = useState(false)
  const [participantForm, setParticipantForm] = useState({
    fullName: '',
    phoneNumber: '',
    email: ''
  })
  const [isCompleted, setIsCompleted] = useState(false)

  // Fetch survey by ID or short code
  const { data: survey, isLoading } = useSurvey(surveyId || shortCode)
  const submitResponseMutation = useSubmitResponse()

  const currentQuestion = survey?.questions?.[currentQuestionIndex]
  const progress = survey?.questions?.length 
    ? ((currentQuestionIndex + 1) / survey.questions.length) * 100 
    : 0

  const handleResponseChange = (value) => {
    setResponses(prev => ({
      ...prev,
      [currentQuestion.id]: value
    }))
  }

  const handleNext = () => {
    if (currentQuestionIndex < survey.questions.length - 1) {
      setCurrentQuestionIndex(prev => prev + 1)
    } else {
      // Survey completed
      if (survey.hasRewards && !participant) {
        setShowRewardForm(true)
      } else {
        handleSubmitSurvey()
      }
    }
  }

  const handlePrevious = () => {
    if (currentQuestionIndex > 0) {
      setCurrentQuestionIndex(prev => prev - 1)
    }
  }

  const handleParticipantSubmit = async (e) => {
    e.preventDefault()
    try {
      const response = await participantAPI.register(participantForm)
      setParticipant(response.data)
      setShowRewardForm(false)
      handleSubmitSurvey()
    } catch (error) {
      console.error('Failed to register participant:', error)
    }
  }

  const handleSubmitSurvey = async () => {
    try {
      const answersData = Object.entries(responses).map(([questionId, answer]) => ({
        questionId: parseInt(questionId),
        answer: typeof answer === 'string' ? answer : answer.toString()
      }))

      await submitResponseMutation.mutateAsync({
        surveyId: survey.id,
        responseData: { answers: answersData }
      })
      
      setIsCompleted(true)
    } catch (error) {
      console.error('Failed to submit survey:', error)
    }
  }

  const renderQuestionInput = () => {
    if (!currentQuestion) return null

    const currentResponse = responses[currentQuestion.id] || ''

    switch (currentQuestion.type) {
      case 'TEXT':
        return (
          <Textarea
            value={currentResponse}
            onChange={(e) => handleResponseChange(e.target.value)}
            placeholder="Type your answer here..."
            rows={4}
            className="w-full"
          />
        )

      case 'MULTIPLE_CHOICE':
        return (
          <div className="space-y-3">
            {currentQuestion.options?.map((option, index) => (
              <div key={index} className="flex items-center">
                <Radio
                  id={`option-${index}`}
                  name={`question-${currentQuestion.id}`}
                  value={option}
                  checked={currentResponse === option}
                  onChange={(e) => handleResponseChange(e.target.value)}
                />
                <Label htmlFor={`option-${index}`} className="ml-2">
                  {option}
                </Label>
              </div>
            ))}
          </div>
        )

      case 'SCALE':
        return (
          <div className="space-y-4">
            <div className="flex justify-between text-sm text-gray-600">
              <span>1 (Very Poor)</span>
              <span>10 (Excellent)</span>
            </div>
            <div className="flex justify-between">
              {[1, 2, 3, 4, 5, 6, 7, 8, 9, 10].map((num) => (
                <button
                  key={num}
                  type="button"
                  onClick={() => handleResponseChange(num)}
                  className={`w-10 h-10 rounded-full border-2 font-medium transition-colors ${
                    currentResponse === num
                      ? 'border-primary-500 bg-primary-500 text-white'
                      : 'border-gray-300 text-gray-700 hover:border-primary-300'
                  }`}
                >
                  {num}
                </button>
              ))}
            </div>
          </div>
        )

      case 'YES_NO':
        return (
          <div className="flex gap-4">
            <Button
              color={currentResponse === 'Yes' ? 'success' : 'gray'}
              onClick={() => handleResponseChange('Yes')}
              className="flex-1"
            >
              Yes
            </Button>
            <Button
              color={currentResponse === 'No' ? 'failure' : 'gray'}
              onClick={() => handleResponseChange('No')}
              className="flex-1"
            >
              No
            </Button>
          </div>
        )

      default:
        return (
          <TextInput
            value={currentResponse}
            onChange={(e) => handleResponseChange(e.target.value)}
            placeholder="Your answer..."
            className="w-full"
          />
        )
    }
  }

  if (isLoading) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-primary-500 mx-auto mb-4"></div>
          <p className="text-gray-600">Loading survey...</p>
        </div>
      </div>
    )
  }

  if (!survey) {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <Card className="max-w-md mx-auto">
          <div className="text-center">
            <h2 className="text-xl font-semibold mb-2">Survey Not Found</h2>
            <p className="text-gray-600">This survey may have been removed or is no longer available.</p>
          </div>
        </Card>
      </div>
    )
  }

  if (isCompleted) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="max-w-md mx-auto">
          <div className="text-center">
            <HiCheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4" />
            <h2 className="text-2xl font-bold mb-4">Thank You!</h2>
            <p className="text-gray-600 mb-6">
              Your response has been submitted successfully.
            </p>
            {survey.hasRewards && participant && (
              <div className="bg-green-50 border border-green-200 rounded-lg p-4 mb-6">
                <div className="flex items-center justify-center mb-2">
                  <HiGift className="w-5 h-5 text-green-600 mr-2" />
                  <span className="font-medium text-green-800">Reward Earned!</span>
                </div>
                <p className="text-sm text-green-700">
                  Your reward will be sent to the contact details you provided.
                </p>
              </div>
            )}
            <Button onClick={() => navigate('/')} className="bg-primary-500 hover:bg-primary-600">
              Return Home
            </Button>
          </div>
        </Card>
      </div>
    )
  }

  if (showRewardForm) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="max-w-md mx-auto">
          <div className="text-center mb-6">
            <HiGift className="w-12 h-12 text-primary-500 mx-auto mb-4" />
            <h2 className="text-xl font-bold mb-2">Claim Your Reward</h2>
            <p className="text-gray-600">
              Provide your details to receive rewards for completing this survey.
            </p>
          </div>

          <form onSubmit={handleParticipantSubmit} className="space-y-4">
            <div>
              <Label htmlFor="fullName">Full Name</Label>
              <TextInput
                id="fullName"
                value={participantForm.fullName}
                onChange={(e) => setParticipantForm(prev => ({...prev, fullName: e.target.value}))}
                required
              />
            </div>

            <div>
              <Label htmlFor="phoneNumber">Phone Number</Label>
              <TextInput
                id="phoneNumber"
                value={participantForm.phoneNumber}
                onChange={(e) => setParticipantForm(prev => ({...prev, phoneNumber: e.target.value}))}
                required
              />
            </div>

            <div>
              <Label htmlFor="email">Email (Optional)</Label>
              <TextInput
                id="email"
                type="email"
                value={participantForm.email}
                onChange={(e) => setParticipantForm(prev => ({...prev, email: e.target.value}))}
              />
            </div>

            <div className="flex gap-3 pt-4">
              <Button
                color="gray"
                onClick={() => {
                  setShowRewardForm(false)
                  handleSubmitSurvey()
                }}
                className="flex-1"
              >
                Skip Rewards
              </Button>
              <Button type="submit" className="bg-primary-500 hover:bg-primary-600 flex-1">
                Claim Reward
              </Button>
            </div>
          </form>
        </Card>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-8">
      <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-2xl font-bold text-gray-900 mb-2">{survey.name}</h1>
          <p className="text-gray-600">Question {currentQuestionIndex + 1} of {survey.questions?.length || 0}</p>
        </div>

        {/* Progress Bar */}
        <div className="mb-8">
          <Progress progress={progress} color="green" className="mb-2" />
          <div className="flex justify-between text-sm text-gray-600">
            <span>{Math.round(progress)}% Complete</span>
            {survey.hasRewards && (
              <span className="flex items-center">
                <HiGift className="w-4 h-4 mr-1" />
                Rewards Available
              </span>
            )}
          </div>
        </div>

        {/* Question Card */}
        <Card className="mb-8">
          <div className="mb-6">
            <h2 className="text-lg font-medium text-gray-900 mb-4">
              {currentQuestion?.text}
            </h2>
            {currentQuestion?.required && (
              <span className="text-sm text-red-600">* Required</span>
            )}
          </div>

          <div className="mb-6">
            {renderQuestionInput()}
          </div>

          <div className="flex justify-between">
            <Button
              color="gray"
              onClick={handlePrevious}
              disabled={currentQuestionIndex === 0}
            >
              Previous
            </Button>

            <Button
              onClick={handleNext}
              disabled={
                currentQuestion?.required && 
                (!responses[currentQuestion.id] || responses[currentQuestion.id] === '')
              }
              className="bg-primary-500 hover:bg-primary-600"
            >
              {currentQuestionIndex === survey.questions.length - 1 ? 'Complete' : 'Next'}
            </Button>
          </div>
        </Card>
      </div>
    </div>
  )
}

export default SurveySession