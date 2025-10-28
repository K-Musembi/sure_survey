import axios from 'axios'
import useAuthStore from '../stores/authStore'

// Create axios instance with base configuration
const api = axios.create({
  baseURL: `${import.meta.env.VITE_API_BASE_URL}/api/${import.meta.env.VITE_API_VERSION}`,
  timeout: 30000,
  withCredentials: true, // Enable httpOnly cookies
  headers: {
    'Content-Type': 'application/json',
  },
})

// Request interceptor
api.interceptors.request.use(
  (config) => {
    // Add any request modifications here
    console.log(`[API] ${config.method?.toUpperCase()} ${config.url}`)
    return config
  },
  (error) => {
    console.error('[API] Request error:', error)
    return Promise.reject(error)
  }
)

// Response interceptor
api.interceptors.response.use(
  (response) => {
    return response
  },
  async (error) => {
    const originalRequest = error.config
    
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true
      
      // On unauthorized, logout the user from the store
      console.warn('[API] Unauthorized access. Logging out.')
      useAuthStore.getState().logout()
      return Promise.reject(error)
    }
    
    console.error('[API] Response error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

// Auth API calls
export const authAPI = {
  signup: (userData) => api.post('/auth/signup', userData),
  login: (credentials) => api.post('/auth/login', credentials),
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'),
  refreshToken: () => api.post('/auth/refresh'),
}

// Tenant API calls
export const tenantAPI = {
  create: (tenantData) => api.post('/tenants', tenantData),
  get: (tenantId) => api.get(`/tenants/${tenantId}`),
}

// Survey API calls
export const surveyAPI = {
  getMySurveys: () => api.get('/surveys/my-surveys'),
  createSurvey: (surveyData) => api.post('/surveys', surveyData),
  getSurvey: (surveyId) => api.get(`/surveys/${surveyId}`),
  updateSurvey: (surveyId, surveyData) => api.put(`/surveys/${surveyId}`, surveyData),
  deleteSurvey: (surveyId) => api.delete(`/surveys/${surveyId}`),
  activateSurvey: (surveyId) => api.post(`/surveys/${surveyId}/activate`),
  deactivateSurvey: (surveyId) => api.post(`/surveys/${surveyId}/deactivate`),
}

// Template API calls
export const templateAPI = {
  getTemplatesByType: (type) => api.get('/templates/filter/type', { params: { type } }),
  getAllTemplates: () => api.get('/templates'),
}

// Response API calls
export const responseAPI = {
  submitResponse: (surveyId, responseData) => api.post(`/surveys/${surveyId}/responses`, responseData),
  getAnalytics: (surveyId) => api.get(`/responses/analytics`, { params: { surveyId } }),
  streamResponses: () => {
    // SSE connection for real-time responses
    const eventSource = new EventSource(
      `${import.meta.env.VITE_API_BASE_URL}/api/${import.meta.env.VITE_API_VERSION}/responses/stream`,
      { withCredentials: true }
    )
    return eventSource
  },
}

// Reward API calls
export const rewardAPI = {
  createReward: (rewardData) => api.post('/rewards', rewardData),
  getRewards: (surveyId) => api.get(`/rewards`, { params: { surveyId } }),
  updateReward: (rewardId, rewardData) => api.put(`/rewards/${rewardId}`, rewardData),
  deleteReward: (rewardId) => api.delete(`/rewards/${rewardId}`),
}

// Participant API calls
export const participantAPI = {
  register: (participantData) => api.post('/participants', participantData),
  getParticipant: (participantId) => api.get(`/participants/${participantId}`),
}

// Payment API calls
export const paymentAPI = {
  createPayment: (paymentData) => api.post('/payments', paymentData),
  verifyPayment: (reference) => api.get(`/payments/verify/${reference}`),
  getPaymentHistory: () => api.get('/payments/history'),
}

// Subscription API calls (for future implementation)
export const subscriptionAPI = {
  getSubscription: () => api.get('/subscriptions/current'),
  createSubscription: (subscriptionData) => api.post('/subscriptions', subscriptionData),
  cancelSubscription: () => api.delete('/subscriptions/current'),
}

// Generic API utility functions
export const apiUtils = {
  // Generic GET request
  get: (url, config = {}) => api.get(url, config),
  
  // Generic POST request  
  post: (url, data = {}, config = {}) => api.post(url, data, config),
  
  // Generic PUT request
  put: (url, data = {}, config = {}) => api.put(url, data, config),
  
  // Generic DELETE request
  delete: (url, config = {}) => api.delete(url, config),
  
  // File upload utility
  uploadFile: (url, file, onProgress = null) => {
    const formData = new FormData()
    formData.append('file', file)
    
    return api.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
      onUploadProgress: (progressEvent) => {
        if (onProgress) {
          const percentCompleted = Math.round(
            (progressEvent.loaded * 100) / progressEvent.total
          )
          onProgress(percentCompleted)
        }
      },
    })
  },
}

export default api