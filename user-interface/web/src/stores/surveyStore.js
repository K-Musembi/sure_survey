import { create } from 'zustand'
import { devtools, persist } from 'zustand/middleware'

const initialChannels = { web: true, sms: false, whatsapp: false }
const initialReward = { enabled: false, type: 'LOYALTY_POINTS', amount: '' }

const useSurveyStore = create()(
  devtools(
    persist(
    (set, get) => ({
      // Survey Builder State
      currentSurvey: {
        name: '',
        type: null, // NPS, CES, CSAT, PERFORMANCE
        accessType: 'PUBLIC', // PUBLIC, PRIVATE
        startDate: null,
        endDate: null,
        questions: [],
        rewards: null,
        budget: null,
        targetRespondents: null,
      },

      // Channel & reward state (persisted separately for builder workflow)
      channels: { ...initialChannels },
      reward: { ...initialReward },

      selectedTemplate: null,
      availableTemplates: [],
      isBuilding: false,

      // Survey Management
      userSurveys: [],
      selectedSurveyForAnalytics: null,

      // Actions
      updateSurvey: (updates) => set((state) => ({
        currentSurvey: { ...state.currentSurvey, ...updates }
      }), false, 'updateSurvey'),

      updateChannels: (updates) => set((state) => ({
        channels: { ...state.channels, ...updates }
      }), false, 'updateChannels'),

      updateReward: (updates) => set((state) => ({
        reward: { ...state.reward, ...updates }
      }), false, 'updateReward'),

      addQuestion: (question) => set((state) => ({
        currentSurvey: {
          ...state.currentSurvey,
          questions: [...state.currentSurvey.questions, question]
        }
      }), false, 'addQuestion'),

      removeQuestion: (questionId) => set((state) => ({
        currentSurvey: {
          ...state.currentSurvey,
          questions: state.currentSurvey.questions.filter(q => q.id !== questionId)
        }
      }), false, 'removeQuestion'),

      updateQuestion: (questionId, updates) => set((state) => ({
        currentSurvey: {
          ...state.currentSurvey,
          questions: state.currentSurvey.questions.map(q =>
            q.id === questionId ? { ...q, ...updates } : q
          )
        }
      }), false, 'updateQuestion'),

      setSelectedTemplate: (template) => set({ selectedTemplate: template }, false, 'setSelectedTemplate'),

      setAvailableTemplates: (templates) => set({ availableTemplates: templates }, false, 'setAvailableTemplates'),

      setUserSurveys: (surveys) => set({ userSurveys: surveys }, false, 'setUserSurveys'),

      setSelectedSurveyForAnalytics: (survey) => set({ selectedSurveyForAnalytics: survey }, false, 'setSelectedSurveyForAnalytics'),

      resetSurveyBuilder: () => set({
        currentSurvey: {
          name: '',
          type: null,
          accessType: 'PUBLIC',
          startDate: null,
          endDate: null,
          questions: [],
          rewards: null,
          budget: null,
          targetRespondents: null,
        },
        channels: { ...initialChannels },
        reward: { ...initialReward },
        selectedTemplate: null,
        isBuilding: false,
      }, false, 'resetSurveyBuilder'),

      setBuilding: (isBuilding) => set({ isBuilding }, false, 'setBuilding'),

      // Getters
      getCurrentSurvey: () => get().currentSurvey,
      getSelectedTemplate: () => get().selectedTemplate,
      getUserSurveys: () => get().userSurveys,
    }),
    { name: 'survey-store', partialize: (state) => ({ currentSurvey: state.currentSurvey, selectedTemplate: state.selectedTemplate, isBuilding: state.isBuilding, channels: state.channels, reward: state.reward }) }
    ),
    { name: 'survey-store-devtools' }
  )
)

export default useSurveyStore
