import { useState } from 'react'
import { Card, Button, Select, Badge, TextInput, Spinner, Alert } from 'flowbite-react'
import { 
  LineChart, Line, AreaChart, Area, BarChart, Bar, PieChart, Pie, Cell,
  XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer 
} from 'recharts'
import { useSurveyAnalytics, useRealTimeResponses } from '../hooks/useApi'
import { aiAPI } from '../services/apiServices'
import useSurveyStore from '../stores/surveyStore'
import { HiTrendingUp, HiUsers, HiClock, HiCurrencyDollar, HiSparkles, HiChat, HiExclamationCircle } from 'react-icons/hi'

const AnalyticsDashboard = ({ surveys = [] }) => {
  const [selectedSurveyId, setSelectedSurveyId] = useState('')
  const [viewMode, setViewMode] = useState('real-time') // 'real-time' or 'historical'
  const { selectedSurveyForAnalytics, setSelectedSurveyForAnalytics } = useSurveyStore()
  
  // AI Analysis State
  const [aiQuery, setAiQuery] = useState('')
  const [aiResult, setAiResult] = useState('')
  const [isAiAnalyzing, setIsAiAnalyzing] = useState(false)
  const [aiError, setAiError] = useState('')
  
  const { data: analyticsData, isLoading, error: analyticsError } = useSurveyAnalytics(selectedSurveyId)
  
  // Use real-time hook for SSE connection
  useRealTimeResponses(selectedSurveyId)

  const handleSurveyChange = (surveyId) => {
    setSelectedSurveyId(surveyId)
    const survey = surveys.find(s => s.id === parseInt(surveyId))
    setSelectedSurveyForAnalytics(survey)
    setAiResult('') // Reset AI result on survey change
    setAiError('')
  }

  const handleAiAnalyze = async () => {
    if (!selectedSurveyId || !aiQuery) return
    setIsAiAnalyzing(true)
    setAiResult('')
    setAiError('')
    try {
      const response = await aiAPI.analyzeSurvey({
        surveyId: selectedSurveyId,
        query: aiQuery
      })
      setAiResult(response.data.result)
    } catch (error) {
      console.error("AI Analysis failed", error)
      setAiError("Failed to analyze. Please try again.")
    } finally {
      setIsAiAnalyzing(false)
    }
  }

  // Mock data for demo purposes (keep existing mocks if real data not fully mapped)
  const mockResponseData = [
    { time: '09:00', responses: 45 },
    { time: '10:00', responses: 67 },
    { time: '11:00', responses: 89 },
    { time: '12:00', responses: 123 },
    { time: '13:00', responses: 145 },
    { time: '14:00', responses: 167 },
    { time: '15:00', responses: 189 },
  ]

  const mockSatisfactionData = [
    { name: 'Very Satisfied', value: 35, color: '#84cc16' },
    { name: 'Satisfied', value: 45, color: '#a3e635' },
    { name: 'Neutral', value: 15, color: '#fbbf24' },
    { name: 'Dissatisfied', value: 5, color: '#f87171' },
  ]

  const mockNPSData = [
    { category: 'Detractors (0-6)', count: 23, percentage: 15 },
    { category: 'Passives (7-8)', count: 45, percentage: 30 },
    { category: 'Promoters (9-10)', count: 82, percentage: 55 },
  ]

  if (!surveys.length) {
    return (
      <Card>
        <div className="text-center py-8">
          <p className="text-gray-500">No surveys available for analytics</p>
        </div>
      </Card>
    )
  }

  return (
    <div className="space-y-6">
      {/* Controls */}
      <Card>
        <div className="flex flex-col sm:flex-row gap-4 items-start sm:items-center justify-between">
          <div className="flex flex-col sm:flex-row gap-4">
            <div>
              <Select
                value={selectedSurveyId}
                onChange={(e) => handleSurveyChange(e.target.value)}
              >
                <option value="">Select a survey</option>
                {surveys.map((survey) => (
                  <option key={survey.id} value={survey.id}>
                    {survey.name}
                  </option>
                ))}
              </Select>
            </div>
            
            <div className="flex gap-2">
              <Button
                color={viewMode === 'real-time' ? 'success' : 'gray'}
                size="sm"
                onClick={() => setViewMode('real-time')}
              >
                Real-time
              </Button>
              <Button
                color={viewMode === 'historical' ? 'success' : 'gray'}
                size="sm"
                onClick={() => setViewMode('historical')}
              >
                Historical
              </Button>
            </div>
          </div>
          
          {viewMode === 'real-time' && (
            <Badge color="success" className="flex items-center">
              <div className="w-2 h-2 bg-green-500 rounded-full mr-2 animate-pulse"></div>
              Live
            </Badge>
          )}
        </div>
      </Card>

      {!selectedSurveyId ? (
        <Card>
          <div className="text-center py-8">
            <p className="text-gray-500">Select a survey to view analytics</p>
          </div>
        </Card>
      ) : (
        <>
          {analyticsError && (
             <Alert color="failure" icon={HiExclamationCircle} className="mb-4">
               Failed to load analytics data.
             </Alert>
          )}

          {/* AI Analysis Section */}
          <Card className="bg-gradient-to-r from-purple-50 to-white border-purple-100">
             <div className="flex items-start gap-4">
               <div className="p-3 bg-purple-100 rounded-lg">
                 <HiSparkles className="w-6 h-6 text-purple-600" />
               </div>
               <div className="flex-grow space-y-4">
                 <div>
                   <h3 className="text-lg font-bold text-gray-900">AI Analyst</h3>
                   <p className="text-sm text-gray-600">Ask questions about your survey data in plain English.</p>
                 </div>
                 
                 <div className="flex gap-2">
                   <TextInput 
                     className="flex-grow"
                     placeholder="e.g. What is the general sentiment of the feedback?"
                     value={aiQuery}
                     onChange={(e) => setAiQuery(e.target.value)}
                     onKeyDown={(e) => e.key === 'Enter' && handleAiAnalyze()}
                   />
                   <Button gradientDuoTone="purpleToBlue" onClick={handleAiAnalyze} disabled={isAiAnalyzing}>
                     {isAiAnalyzing ? <Spinner size="sm" /> : 'Analyze'}
                   </Button>
                 </div>

                 {aiError && (
                   <Alert color="failure" icon={HiExclamationCircle} className="mt-4" onDismiss={() => setAiError('')}>
                     {aiError}
                   </Alert>
                 )}

                 {aiResult && (
                   <div className="bg-white p-4 rounded-lg border border-purple-100 shadow-sm mt-4">
                     <div className="flex items-start gap-2">
                       <HiChat className="w-5 h-5 text-purple-500 mt-1" />
                       <p className="text-gray-800 whitespace-pre-wrap">{aiResult}</p>
                     </div>
                   </div>
                 )}
               </div>
             </div>
          </Card>

          {/* Key Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            <Card>
              <div className="flex items-center">
                <div className="p-3 rounded-full bg-primary-100 mr-4">
                  <HiUsers className="w-6 h-6 text-primary-600" />
                </div>
                <div>
                  <div className="text-2xl font-bold text-gray-900">1,247</div>
                  <div className="text-sm text-gray-500">Total Responses</div>
                </div>
              </div>
            </Card>

            <Card>
              <div className="flex items-center">
                <div className="p-3 rounded-full bg-blue-100 mr-4">
                  <HiTrendingUp className="w-6 h-6 text-blue-600" />
                </div>
                <div>
                  <div className="text-2xl font-bold text-gray-900">87%</div>
                  <div className="text-sm text-gray-500">Completion Rate</div>
                </div>
              </div>
            </Card>

            <Card>
              <div className="flex items-center">
                <div className="p-3 rounded-full bg-yellow-100 mr-4">
                  <HiClock className="w-6 h-6 text-yellow-600" />
                </div>
                <div>
                  <div className="text-2xl font-bold text-gray-900">2.3m</div>
                  <div className="text-sm text-gray-500">Avg. Time</div>
                </div>
              </div>
            </Card>

            <Card>
              <div className="flex items-center">
                <div className="p-3 rounded-full bg-green-100 mr-4">
                  <HiCurrencyDollar className="w-6 h-6 text-green-600" />
                </div>
                <div>
                  <div className="text-2xl font-bold text-gray-900">$2,847</div>
                  <div className="text-sm text-gray-500">Rewards Paid</div>
                </div>
              </div>
            </Card>
          </div>

          {/* Charts */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
            {/* Response Timeline */}
            <Card>
              <h3 className="text-lg font-semibold mb-4">Response Timeline</h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={mockResponseData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="time" />
                    <YAxis />
                    <Tooltip />
                    <Area 
                      type="monotone" 
                      dataKey="responses" 
                      stroke="#84cc16" 
                      fill="#84cc16" 
                      fillOpacity={0.3}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            </Card>

            {/* Satisfaction Distribution */}
            <Card>
              <h3 className="text-lg font-semibold mb-4">Satisfaction Distribution</h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={mockSatisfactionData}
                      cx="50%"
                      cy="50%"
                      outerRadius={80}
                      dataKey="value"
                      label={({ name, percentage }) => `${name}: ${percentage}%`}
                    >
                      {mockSatisfactionData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </Card>

            {/* NPS Breakdown */}
            <Card>
              <h3 className="text-lg font-semibold mb-4">NPS Breakdown</h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={mockNPSData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="category" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="count" fill="#84cc16" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            </Card>

            {/* Real-time Activity */}
            <Card>
              <h3 className="text-lg font-semibold mb-4">
                {viewMode === 'real-time' ? 'Live Activity' : 'Historical Trends'}
              </h3>
              <div className="h-64">
                <ResponsiveContainer width="100%" height="100%">
                  <LineChart data={mockResponseData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="time" />
                    <YAxis />
                    <Tooltip />
                    <Legend />
                    <Line 
                      type="monotone" 
                      dataKey="responses" 
                      stroke="#84cc16" 
                      strokeWidth={2}
                      dot={{ fill: '#84cc16' }}
                    />
                  </LineChart>
                </ResponsiveContainer>
              </div>
            </Card>
          </div>
        </>
      )}
    </div>
  )
}

export default AnalyticsDashboard