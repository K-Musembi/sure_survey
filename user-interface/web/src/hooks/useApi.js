import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { useEffect } from 'react'
import { authAPI, surveyAPI, templateAPI, responseAPI, paymentAPI } from '../services/apiServices'
import { QUERY_KEYS } from '../lib/queryClient'
import useAuthStore from '../stores/authStore'

// Auth hooks
export const useAuth = () => {
  const { login, logout, setUser } = useAuthStore()
  
  return {
    login,
    logout,
    setUser,
  }
}

export const useMe = () => {
  const { setUser, logout } = useAuthStore()
  
  return useQuery({
    queryKey: QUERY_KEYS.auth.me,
    queryFn: async () => {
      try {
        const response = await authAPI.me()
        setUser(response.data)
        return response.data
      } catch (error) {
        if (error.response?.status === 401) {
          logout()
        }
        throw error
      }
    },
    retry: false,
  })
}

export const useLogin = () => {
  const queryClient = useQueryClient()
  const { login } = useAuthStore()
  
  return useMutation({
    mutationFn: authAPI.login,
    onSuccess: (response) => {
      login(response.data)
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.auth.me })
    },
    onError: (error) => {
      console.error('Login failed:', error.response?.data || error.message)
    },
  })
}

export const useSignup = () => {
  return useMutation({
    mutationFn: authAPI.signup,
    onError: (error) => {
      console.error('Signup failed:', error.response?.data || error.message)
    },
  })
}

export const useLogout = () => {
  const queryClient = useQueryClient()
  const { logout } = useAuthStore()
  
  return useMutation({
    mutationFn: authAPI.logout,
    onSuccess: () => {
      logout()
      queryClient.clear()
    },
    onError: () => {
      // Even if the API call fails, clear local state
      logout()
      queryClient.clear()
    },
  })
}

// Survey hooks
export const useMySurveys = () => {
  return useQuery({
    queryKey: QUERY_KEYS.surveys.my,
    queryFn: async () => {
      const response = await surveyAPI.getMySurveys()
      return response.data
    },
  })
}

export const useSurvey = (surveyId) => {
  return useQuery({
    queryKey: QUERY_KEYS.surveys.detail(surveyId),
    queryFn: async () => {
      const response = await surveyAPI.getSurvey(surveyId)
      return response.data
    },
    enabled: !!surveyId,
  })
}

export const useCreateSurvey = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: surveyAPI.createSurvey,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.surveys.my })
    },
  })
}

export const useActivateSurvey = () => {
  const queryClient = useQueryClient()
  
  return useMutation({
    mutationFn: surveyAPI.activateSurvey,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.surveys.my })
    },
  })
}

// Template hooks
export const useTemplatesByType = (type) => {
  return useQuery({
    queryKey: QUERY_KEYS.templates.byType(type),
    queryFn: async () => {
      const response = await templateAPI.getTemplatesByType(type)
      return response.data
    },
    enabled: !!type,
  })
}

// Analytics hooks
export const useSurveyAnalytics = (surveyId) => {
  return useQuery({
    queryKey: QUERY_KEYS.responses.analytics(surveyId),
    queryFn: async () => {
      const response = await responseAPI.getAnalytics(surveyId)
      return response.data
    },
    enabled: !!surveyId,
    refetchInterval: 30000, // Refetch every 30 seconds for semi-real-time data
  })
}

export const useSubmitResponse = () => {
  return useMutation({
    mutationFn: ({ surveyId, responseData }) => 
      responseAPI.submitResponse(surveyId, responseData),
  })
}

// Payment hooks
export const useCreatePayment = () => {
  return useMutation({
    mutationFn: paymentAPI.createPayment,
  })
}

export const useVerifyPayment = (reference) => {
  return useQuery({
    queryKey: ['payments', 'verify', reference],
    queryFn: async () => {
      const response = await paymentAPI.verifyPayment(reference)
      return response.data
    },
    enabled: !!reference,
    retry: false,
  })
}

// Real-time hooks
export const useRealTimeResponses = (surveyId) => {
  const queryClient = useQueryClient()
  
  useEffect(() => {
    if (!surveyId) return
    
    const eventSource = responseAPI.streamResponses()
    
    eventSource.onmessage = (event) => {
      const data = JSON.parse(event.data)
      
      // Update analytics cache with new data
      queryClient.setQueryData(
        QUERY_KEYS.responses.analytics(surveyId),
        (oldData) => {
          if (!oldData) return data
          // Merge new data with existing data
          return { ...oldData, ...data }
        }
      )
    }
    
    eventSource.onerror = (error) => {
      console.error('SSE Error:', error)
    }
    
    return () => {
      eventSource.close()
    }
  }, [surveyId, queryClient])
}