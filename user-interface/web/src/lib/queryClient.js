import { QueryClient } from '@tanstack/react-query'

export const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: (failureCount, error) => {
        // Don't retry on 401, 403, 404
        if (error?.response?.status === 401 || 
            error?.response?.status === 403 || 
            error?.response?.status === 404) {
          return false
        }
        // Retry up to 3 times for other errors
        return failureCount < 3
      },
      staleTime: 5 * 60 * 1000, // 5 minutes
      cacheTime: 10 * 60 * 1000, // 10 minutes
      refetchOnWindowFocus: false,
      refetchOnReconnect: true,
    },
    mutations: {
      retry: 1,
    },
  },
})

// Query keys for consistent cache management
export const QUERY_KEYS = {
  auth: {
    me: ['auth', 'me'],
    subscription: ['auth', 'subscription'],
  },
  surveys: {
    all: ['surveys'],
    my: ['surveys', 'my'],
    detail: (id) => ['surveys', 'detail', id],
    analytics: (id) => ['surveys', 'analytics', id],
  },
  templates: {
    all: ['templates'],
    byType: (type) => ['templates', 'type', type],
  },
  responses: {
    analytics: (surveyId) => ['responses', 'analytics', surveyId],
  },
  rewards: {
    bySurvey: (surveyId) => ['rewards', 'survey', surveyId],
  },
  payments: {
    history: ['payments', 'history'],
  },
}