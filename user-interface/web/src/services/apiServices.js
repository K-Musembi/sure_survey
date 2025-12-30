import axios from 'axios'
import useAuthStore from '../stores/authStore'
import useErrorStore from '../stores/errorStore'

// Create axios instance with base configuration
// Force relative path to use Vite proxy, solving cookie/CORS issues
const API_BASE_URL = '' 
const API_VERSION = import.meta.env.VITE_API_VERSION || 'v1'

const api = axios.create({
  baseURL: `${API_BASE_URL}/api/${API_VERSION}`,
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
    
    // Authorization header is NOT attached here. 
    // The application relies on HttpOnly cookies set by the backend.
    
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
    
    // Global Error Handling for other statuses
    const errorMessage = error.response?.data?.message || error.message || 'An unexpected error occurred.'
    // Avoid showing modal for 404s if handled locally, or generic network errors we want to be noisy about?
    // For now, show all except 401.
    useErrorStore.getState().showError(errorMessage, `Error ${error.response?.status || ''}`)
    
    console.error('[API] Response error:', error.response?.data || error.message)
    return Promise.reject(error)
  }
)

// Auth API calls
export const authAPI = {
  signup: (userData) => api.post('/auth/signup', userData, { withCredentials: false }),
  login: async (credentials) => {
    const response = await fetch(`${API_BASE_URL}/api/${API_VERSION}/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify(credentials),
    })

    if (!response.ok) {
       const errorData = await response.json().catch(() => ({}));
       const error = new Error('Login failed');
       error.response = { status: response.status, data: errorData };
       throw error;
    }

    const data = await response.json();
    return { data }; 
  },
  logout: () => api.post('/auth/logout'),
  me: () => api.get('/auth/me'), // Note: Backend implementation for /auth/me might be missing, usually we rely on login response or dedicated user endpoint
  checkTenant: (tenantName) => api.post('/auth/check-tenant', { tenantName }, { withCredentials: false }),
  refreshToken: () => api.post('/auth/refresh'),
}

// User & Tenant API calls
export const userAPI = {
  getUser: (id) => api.get(`/users/${id}`),
  updateUser: (id, data) => api.put(`/users/${id}`, data),
  getUserByEmail: (email) => api.get(`/users/email/${email}`),
  getTenantUsers: (tenantId) => api.get(`/users/tenant`, { params: { tenantId } }),
  deleteUser: (id) => api.delete(`/users/${id}`),
}

export const tenantAPI = {
  createTenant: (data) => api.post('/tenants', data),
  getTenant: (id) => api.get(`/tenants/${id}`),
  updateTenant: (id, data) => api.put(`/tenants/${id}`, data),
  getAllTenants: () => api.get('/tenants'),
  deleteTenant: (id) => api.delete(`/tenants/${id}`),
}

// Admin API calls
export const adminAPI = {
  login: (credentials) => api.post('/admin/login', credentials, { withCredentials: false }),
  getAllTenants: () => api.get('/admin/tenants'),
  getTenantSurveys: (tenantId) => api.get(`/admin/tenants/${tenantId}/surveys`),
  getSettings: () => api.get('/admin/settings'),
  updateSettings: (settings) => api.put('/admin/settings', settings),
  createPlan: (planData) => api.post('/admin/plans', planData),
  updatePlan: (planData) => api.put('/admin/plans', planData),
  configurePlanGateway: (planId, data) => api.post(`/admin/plans/${planId}/gateways`, data),
  restockSystemWallet: (type, amount) => api.post('/admin/system-wallet/restock', null, { params: { type, amount } }),
}

// Survey API calls
export const surveyAPI = {
  getMySurveys: () => api.get('/surveys/my-surveys'),
  getTeamSurveys: () => api.get('/surveys/my-team'),
  createSurvey: (surveyData) => api.post('/surveys', surveyData),
  getSurvey: (surveyId) => api.get(`/surveys/${surveyId}`),
  updateSurvey: (surveyId, surveyData) => api.put(`/surveys/${surveyId}`, surveyData),
  deleteSurvey: (surveyId) => api.delete(`/surveys/${surveyId}`),
  activateSurvey: (surveyId) => api.post(`/surveys/${surveyId}/activate`),
  closeSurvey: (surveyId) => api.post(`/surveys/${surveyId}/close`),
  sendToDistributionList: (surveyId, distributionListId) => api.post(`/surveys/${surveyId}/send-to-distribution-list`, { distributionListId }),
  
  // Questions
  addQuestion: (surveyId, questionData) => api.post(`/surveys/${surveyId}/questions`, questionData),
  getQuestions: (surveyId) => api.get(`/surveys/${surveyId}/questions`),
  getQuestion: (surveyId, questionId) => api.get(`/surveys/${surveyId}/questions/${questionId}`),
  updateQuestion: (surveyId, questionId, data) => api.put(`/surveys/${surveyId}/questions/${questionId}`, data),
  deleteQuestion: (surveyId, questionId) => api.delete(`/surveys/${surveyId}/questions/${questionId}`),
}

// Template API calls
export const templateAPI = {
  createTemplate: (data) => api.post('/templates', data),
  getAllTemplates: () => api.get('/templates'),
  getTemplate: (id) => api.get(`/templates/${id}`),
  getTemplatesByType: (type) => api.get('/templates/filter/type', { params: { type } }),
  getTemplatesBySector: (sector) => api.get('/templates/filter/sector', { params: { sector } }),
  updateTemplate: (id, data) => api.put(`/templates/${id}`, data),
  deleteTemplate: (id) => api.delete(`/templates/${id}`),
  
  // Template Questions
  addQuestion: (templateId, data) => api.post(`/templates/${templateId}/questions`, data),
  getQuestions: (templateId) => api.get(`/templates/${templateId}/questions`),
  updateQuestion: (templateId, questionId, data) => api.put(`/templates/${templateId}/questions/${questionId}`, data),
  deleteQuestion: (templateId, questionId) => api.delete(`/templates/${templateId}/questions/${questionId}`),
}

// Response API calls
export const responseAPI = {
  submitResponse: (surveyId, responseData) => api.post(`/surveys/${surveyId}/responses`, responseData),
  getSurveyResponses: (surveyId) => api.get(`/surveys/${surveyId}/responses`),
  getResponse: (surveyId, responseId) => api.get(`/surveys/${surveyId}/responses/${responseId}`),
  deleteResponse: (surveyId, responseId) => api.delete(`/surveys/${surveyId}/responses/${responseId}`),
  getAnalytics: (surveyId) => api.get(`/responses/analytics`, { params: { surveyId } }), // Mock or real depending on backend
  streamResponses: () => {
    const eventSource = new EventSource(
      `${API_BASE_URL}/api/${API_VERSION}/responses/stream`,
      { withCredentials: true }
    )
    return eventSource
  },
  getAnswers: (responseId) => api.get(`/responses/${responseId}/answers`),
}

// Distribution List API calls
export const distributionAPI = {
  createList: (data) => api.post('/distribution-lists', data),
  uploadCsv: (file, name) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('name', name);
    return api.post('/distribution-lists/upload-csv', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
  getLists: () => api.get('/distribution-lists'),
  getList: (id) => api.get(`/distribution-lists/${id}`),
  addContacts: (id, contacts) => api.post(`/distribution-lists/${id}/contacts`, contacts),
}

// AI Analysis API calls
export const aiAPI = {
  generateQuestions: (data) => api.post('/ai/generate', data),
  analyzeSurvey: (data) => api.post('/ai/analyze', data),
}

// Billing & Wallet API calls
export const billingAPI = {
  getWalletBalance: () => api.get('/billing/wallet/balance'),
  getWalletTransactions: () => api.get('/billing/wallet/transactions'),
  getSubscription: () => api.get('/billing/subscription'),
  getAllPlans: () => api.get('/billing/plans'),
  createSubscription: (data) => api.post('/billing/subscription', data),
  cancelSubscription: (id) => api.delete(`/billing/subscription/${id}`),
  getInvoices: () => api.get('/billing/invoices'),
}

// Payment API calls
export const paymentAPI = {
  initiatePayment: (data) => api.post('/payments', data),
  topUpWallet: (data) => api.post('/payments/top-up', data),
  getMyPayments: () => api.get('/payments/my-payments'),
  getPaymentDetails: (id) => api.get(`/payments/${id}`),
  getTransaction: (id) => api.get(`/transactions/${id}`),
  getTransactionsByPayment: (paymentId) => api.get(`/transactions/by-payment/${paymentId}`),
  verifyPayment: (reference) => api.get(`/payments/verify/${reference}`),
}

// Rewards API calls
export const rewardAPI = {
  configureReward: (data) => api.post('/rewards', data),
  getSurveyReward: (surveyId) => api.get(`/rewards/survey/${surveyId}`),
  getMyRewards: () => api.get('/rewards/my-rewards'),
  cancelReward: (id) => api.post(`/rewards/${id}/cancel`),
  getRewardTransactions: (rewardId) => api.get(`/rewards/reward-transactions/reward/${rewardId}`),
  getUserLoyalty: (userId) => api.get(`/rewards/loyalty-accounts/user/${userId}`),
  getUserBalance: (userId) => api.get(`/rewards/loyalty-accounts/user/${userId}/balance`),
  redeemPoints: (data) => api.post('/rewards/loyalty-accounts/me/debit', data),
  getLoyaltyTransactions: (accountId) => api.get(`/rewards/loyalty-transactions/account/${accountId}`),
}

// Business Integration API calls
export const integrationAPI = {
  createIntegration: (data) => api.post('/integrations', data),
  getIntegrations: () => api.get('/integrations'),
  getTransactions: (integrationId) => api.get(`/business-transactions/integration/${integrationId}`),
}

// Participant API calls
export const participantAPI = {
  register: (participantData) => api.post('/participants', participantData),
  getParticipant: (participantId) => api.get(`/participants/${participantId}`),
  deleteParticipant: (id) => api.delete(`/participants/${id}`),
}

// Subscription API calls (Legacy/Wrapper for consistency)
export const subscriptionAPI = {
  getSubscription: billingAPI.getSubscription,
  createSubscription: billingAPI.createSubscription,
  cancelSubscription: billingAPI.cancelSubscription,
}

// Generic API utility functions
export const apiUtils = {
  get: (url, config = {}) => api.get(url, config),
  post: (url, data = {}, config = {}) => api.post(url, data, config),
  put: (url, data = {}, config = {}) => api.put(url, data, config),
  delete: (url, config = {}) => api.delete(url, config),
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
