import { create } from 'zustand'
import { devtools } from 'zustand/middleware'

const useSurveyStore = create()(
  devtools(
    (set, get) => ({
      // Survey Builder State
      currentSurvey: {
        name: '',
        type: null, // NPS, CES, CSAT
        accessType: 'PUBLIC', // PUBLIC, PRIVATE
        startDate: null,
        endDate: null,
        questions: [],
        rewards: null,
        budget: null,
        targetRespondents: null,
      },
      
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
        selectedTemplate: null,
        isBuilding: false,
      }, false, 'resetSurveyBuilder'),
      
      setBuilding: (isBuilding) => set({ isBuilding }, false, 'setBuilding'),
      
      // Getters
      getCurrentSurvey: () => get().currentSurvey,
      getSelectedTemplate: () => get().selectedTemplate,
      getUserSurveys: () => get().userSurveys,
    }),
    { name: 'survey-store' }
  )
)

export default useSurveyStore