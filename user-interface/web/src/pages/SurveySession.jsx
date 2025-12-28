import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { Button, Card, Label, TextInput, Radio, Textarea, Progress, Alert, Spinner } from 'flowbite-react'
import { useSurvey, useSubmitResponse } from '../hooks/useApi'
import { participantAPI } from '../services/apiServices'
import { HiCheckCircle, HiGift, HiExclamationCircle, HiArrowLeft, HiArrowRight } from 'react-icons/hi'

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
  const [submissionError, setSubmissionError] = useState('')

  // Fetch survey by ID or short code
  const { data: survey, isLoading } = useSurvey(surveyId || shortCode)
  const submitResponseMutation = useSubmitResponse()

  const currentQuestion = survey?.questions?.[currentQuestionIndex]
  const totalQuestions = survey?.questions?.length || 0
  const progress = totalQuestions ? ((currentQuestionIndex + 1) / totalQuestions) * 100 : 0

  const handleResponseChange = (value) => {
    setResponses(prev => ({
      ...prev,
      [currentQuestion.id]: value
    }))
  }

  const handleNext = () => {
    if (currentQuestionIndex < totalQuestions - 1) {
      setCurrentQuestionIndex(prev => prev + 1)
    } else {
      // Logic flow: If rewards -> Show Form -> Submit. If no rewards -> Submit.
      if (survey.hasRewards || survey.rewardAmount) { // Check both potential flags from backend
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
    setSubmissionError('')
    try {
      const response = await participantAPI.register(participantForm)
      setParticipant(response.data)
      setShowRewardForm(false)
      handleSubmitSurvey()
    } catch (error) {
      console.error('Failed to register participant:', error)
      setSubmissionError('Failed to register details: ' + (error.response?.data?.message || error.message))
    }
  }

  const handleSubmitSurvey = async () => {
    setSubmissionError('')
    try {
      const answersData = Object.entries(responses).map(([questionId, answer]) => ({
        questionId: parseInt(questionId),
        answerValue: typeof answer === 'string' ? answer : answer.toString()
      }))

      await submitResponseMutation.mutateAsync({
        surveyId: survey.id,
        responseData: { answers: answersData }
      })
      
      setIsCompleted(true)
    } catch (error) {
      console.error('Failed to submit survey:', error)
      setSubmissionError('Failed to submit survey: ' + (error.response?.data?.message || error.message))
    }
  }

  const renderQuestionInput = () => {
    if (!currentQuestion) return null

    const currentResponse = responses[currentQuestion.id] || ''
    const parsedOptions = currentQuestion.options ? JSON.parse(currentQuestion.options) : []

    switch (currentQuestion.questionType) {
      case 'FREE_TEXT':
        return (
          <Textarea
            value={currentResponse}
            onChange={(e) => handleResponseChange(e.target.value)}
            placeholder="Type your answer here..."
            rows={4}
            className="w-full focus:ring-primary-500 focus:border-primary-500"
          />
        )

      case 'MULTIPLE_CHOICE_SINGLE':
        return (
          <div className="space-y-3">
            {parsedOptions.map((option, index) => (
              <div key={index} className="flex items-center p-3 border border-gray-200 rounded-lg hover:bg-gray-50 cursor-pointer" onClick={() => handleResponseChange(option)}>
                <Radio
                  id={`option-${index}`}
                  name={`question-${currentQuestion.id}`}
                  value={option}
                  checked={currentResponse === option}
                  onChange={(e) => handleResponseChange(e.target.value)}
                  className="text-primary-600 focus:ring-primary-500"
                />
                <Label htmlFor={`option-${index}`} className="ml-3 cursor-pointer w-full">
                  {option}
                </Label>
              </div>
            ))}
          </div>
        )
      
      case 'MULTIPLE_CHOICE_MULTI':
         return (
            <div className="p-4 bg-yellow-50 text-yellow-800 rounded">
               Multi-select support coming soon. Please select one best option.
               <div className="space-y-3 mt-2">
                {parsedOptions.map((option, index) => (
                  <div key={index} className="flex items-center">
                    <Radio
                      id={`option-${index}`}
                      name={`question-${currentQuestion.id}`}
                      value={option}
                      checked={currentResponse === option}
                      onChange={(e) => handleResponseChange(e.target.value)}
                    />
                    <Label htmlFor={`option-${index}`} className="ml-2">{option}</Label>
                  </div>
                ))}
              </div>
            </div>
         )

      case 'RATING_LINEAR':
      case 'NPS_SCALE':
        const max = currentQuestion.questionType === 'NPS_SCALE' ? 10 : 10;
        const min = currentQuestion.questionType === 'NPS_SCALE' ? 0 : 1;
        const range = Array.from({length: (max - min) + 1}, (_, i) => i + min);
        
        return (
          <div className="space-y-4">
            <div className="flex justify-between text-sm text-gray-500">
              <span>{min} (Not Likely/Poor)</span>
              <span>{max} (Very Likely/Excellent)</span>
            </div>
            <div className="flex flex-wrap justify-center gap-2">
              {range.map((num) => (
                <button
                  key={num}
                  type="button"
                  onClick={() => handleResponseChange(num)}
                  className={`w-10 h-10 md:w-12 md:h-12 rounded-full border-2 font-bold transition-all transform hover:scale-110 ${
                    currentResponse === num
                      ? 'border-primary-500 bg-primary-500 text-white shadow-lg'
                      : 'border-gray-200 text-gray-600 hover:border-primary-300 hover:text-primary-600 bg-white'
                  }`}
                >
                  {num}
                </button>
              ))}
            </div>
          </div>
        )

      case 'RATING_STAR':
        return (
          <div className="flex items-center justify-center space-x-2 py-4">
            {[1, 2, 3, 4, 5].map((star) => (
              <svg
                key={star}
                className={`w-10 h-10 cursor-pointer transition-colors ${
                  currentResponse >= star ? 'text-yellow-400' : 'text-gray-300 hover:text-yellow-200'
                }`}
                fill="currentColor"
                viewBox="0 0 20 20"
                xmlns="http://www.w3.org/2000/svg"
                onClick={() => handleResponseChange(star)}
              >
                <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.538 1.118l-2.8-2.034a1 1 0 00-1.176 0l-2.8 2.034c-.783.57-1.838-.197-1.538-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.381-1.81.588-1.81h3.462a1 1 0 00.95-.69l1.07-3.292z"></path>
              </svg>
            ))}
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
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <Spinner size="xl" aria-label="Loading survey" />
          <p className="mt-4 text-gray-500">Loading survey...</p>
        </div>
      </div>
    )
  }

  if (!survey) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50">
        <Card className="max-w-md mx-auto">
          <div className="text-center">
            <HiExclamationCircle className="w-12 h-12 text-red-500 mx-auto mb-4" />
            <h2 className="text-xl font-semibold mb-2">Survey Not Found</h2>
            <p className="text-gray-600 mb-6">This survey may have been removed or the link is incorrect.</p>
            <Button color="gray" onClick={() => window.location.href = '/'}>Go Home</Button>
          </div>
        </Card>
      </div>
    )
  }

  if (isCompleted) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-green-50 p-4">
        <Card className="max-w-md w-full shadow-lg border-t-4 border-green-500">
          <div className="text-center py-6">
            <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-6">
               <HiCheckCircle className="w-12 h-12 text-green-600" />
            </div>
            <h2 className="text-3xl font-bold text-gray-900 mb-2">Thank You!</h2>
            <p className="text-gray-600 mb-8">
              Your response has been successfully recorded.
            </p>
            
            {survey.hasRewards && (
              <div className="bg-white border border-green-200 rounded-xl p-6 mb-8 shadow-sm">
                <div className="flex flex-col items-center">
                  <HiGift className="w-10 h-10 text-primary-500 mb-2" />
                  <h4 className="font-bold text-lg text-gray-900">Reward Unlocked!</h4>
                  <p className="text-sm text-gray-500 mt-1">
                    Check your phone/email for your reward details.
                  </p>
                </div>
              </div>
            )}
            
            <Button color="success" onClick={() => window.location.href = '/'}>
              Back to Home
            </Button>
          </div>
        </Card>
      </div>
    )
  }

  if (showRewardForm) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-gray-50 p-4">
        <Card className="max-w-md w-full">
          <div className="text-center mb-6">
            <div className="w-16 h-16 bg-primary-100 rounded-full flex items-center justify-center mx-auto mb-4">
               <HiGift className="w-8 h-8 text-primary-600" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900">Claim Your Reward</h2>
            <p className="text-gray-600 mt-2">
              You've completed the survey! Please provide your details to receive your reward.
            </p>
          </div>

          {submissionError && (
            <Alert color="failure" icon={HiExclamationCircle} className="mb-4">
              {submissionError}
            </Alert>
          )}

          <form onSubmit={handleParticipantSubmit} className="space-y-4">
            <div>
              <Label htmlFor="fullName" value="Full Name" />
              <TextInput
                id="fullName"
                placeholder="Jane Doe"
                value={participantForm.fullName}
                onChange={(e) => setParticipantForm(prev => ({...prev, fullName: e.target.value}))}
                required
              />
            </div>

            <div>
              <Label htmlFor="phoneNumber" value="Phone Number (for Airtime/M-Pesa)" />
              <TextInput
                id="phoneNumber"
                placeholder="+254..."
                value={participantForm.phoneNumber}
                onChange={(e) => setParticipantForm(prev => ({...prev, phoneNumber: e.target.value}))}
                required
              />
            </div>

            <div>
              <Label htmlFor="email" value="Email Address (Optional)" />
              <TextInput
                id="email"
                type="email"
                placeholder="jane@example.com"
                value={participantForm.email}
                onChange={(e) => setParticipantForm(prev => ({...prev, email: e.target.value}))}
              />
            </div>

            <div className="flex gap-3 pt-4">
              <Button
                color="light"
                onClick={() => {
                  if(window.confirm("Are you sure? You will skip the reward.")) {
                    setShowRewardForm(false)
                    handleSubmitSurvey()
                  }
                }}
                className="flex-1"
              >
                Skip
              </Button>
              <Button type="submit" color="purple" className="flex-1">
                Claim Reward
              </Button>
            </div>
          </form>
        </Card>
      </div>
    )
  }

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-2xl mx-auto">
        {/* Header */}
        <div className="text-center mb-10">
          <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight sm:text-4xl">{survey.name}</h1>
          {survey.introduction && <p className="mt-2 text-lg text-gray-600">{survey.introduction}</p>}
        </div>

        {submissionError && (
          <Alert color="failure" icon={HiExclamationCircle} className="mb-6">
            {submissionError}
          </Alert>
        )}

        {/* Progress */}
        <div className="bg-white rounded-full h-2.5 mb-8 overflow-hidden shadow-sm">
          <div 
            className="bg-primary-600 h-2.5 rounded-full transition-all duration-500 ease-out" 
            style={{ width: `${progress}%` }}
          ></div>
        </div>

        {/* Question Card */}
        <Card className="shadow-lg border-0">
          <div className="mb-6">
            <div className="flex justify-between items-center mb-4">
               <span className="text-xs font-semibold tracking-wide uppercase text-gray-500">
                 Question {currentQuestionIndex + 1} of {totalQuestions}
               </span>
               {currentQuestion?.required && (
                 <span className="text-xs font-medium text-red-500 bg-red-50 px-2 py-1 rounded-full">Required</span>
               )}
            </div>
            <h2 className="text-xl font-medium text-gray-900 leading-relaxed">
              {currentQuestion?.questionText}
            </h2>
          </div>

          <div className="mb-8">
            {renderQuestionInput()}
          </div>

          <div className="flex justify-between pt-6 border-t border-gray-100">
            <Button
              color="light"
              onClick={handlePrevious}
              disabled={currentQuestionIndex === 0}
              className={`transition-opacity ${currentQuestionIndex === 0 ? 'opacity-0' : 'opacity-100'}`}
            >
              <HiArrowLeft className="mr-2 h-4 w-4" /> Previous
            </Button>

            <Button
              onClick={handleNext}
              disabled={
                currentQuestion?.required && 
                (!responses[currentQuestion.id] || responses[currentQuestion.id] === '')
              }
              color="purple"
              className="px-6"
            >
              {currentQuestionIndex === totalQuestions - 1 ? 'Finish' : 'Next'} <HiArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </div>
        </Card>
        
        <div className="mt-8 text-center text-sm text-gray-400">
          Powered by Sure Survey
        </div>
      </div>
    </div>
  )
}

export default SurveySession
