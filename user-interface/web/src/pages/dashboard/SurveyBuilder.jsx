import { useState, useEffect } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useTemplatesByType, useCreateSurvey } from '../../hooks/useApi'
import { aiAPI, distributionAPI, billingAPI, surveyAPI } from '../../services/apiServices'
import useSurveyStore from '../../stores/surveyStore'
import Stepper from '../../components/ui/Stepper'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import { SkeletonCard } from '../../components/ui/Skeleton'
import QuestionEditor from '../../components/survey/QuestionEditor'
import SurveySettingsPanel from '../../components/survey/SurveySettingsPanel'
import SurveyReviewPanel from '../../components/survey/SurveyReviewPanel'
import ConsentSettings from '../../components/survey/ConsentSettings'
import PaymentModal from '../../components/PaymentModal'
import useToast from '../../hooks/useToast'
import { HiOutlineSparkles, HiOutlinePlus, HiOutlineArrowRight } from 'react-icons/hi'

const STEPS = ['Method', 'Content', 'Questions', 'Settings', 'Review']

export default function SurveyBuilder() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const editSurveyId = searchParams.get('edit')
  const toast = useToast()

  const [step, setStep] = useState(0)
  const {
    currentSurvey, updateSurvey, addQuestion, removeQuestion, updateQuestion,
    setSelectedTemplate, resetSurveyBuilder,
  } = useSurveyStore()

  const [surveyType, setSurveyType] = useState('')
  const [isAiMode, setIsAiMode] = useState(false)
  const [aiTopic, setAiTopic] = useState('')
  const [aiType, setAiType] = useState('')
  const [aiSector, setAiSector] = useState('')
  const [aiQuestionCount, setAiQuestionCount] = useState('')
  const [isGenerating, setIsGenerating] = useState(false)

  const [distributionLists, setDistributionLists] = useState([])
  const [selectedListId, setSelectedListId] = useState('')
  const [subscription, setSubscription] = useState(null)
  const [enableSmsDispatch, setEnableSmsDispatch] = useState(false)

  const [budget, setBudget] = useState('')
  const [costCalculation, setCostCalculation] = useState(null)
  const [isCalculatingCost, setIsCalculatingCost] = useState(false)
  const [paymentModalOpen, setPaymentModalOpen] = useState(false)

  const [saveError, setSaveError] = useState('')
  const [isLaunching, setIsLaunching] = useState(false)
  const [isSavingDraft, setIsSavingDraft] = useState(false)

  const createSurveyMutation = useCreateSurvey()
  const { data: templates, isLoading: templatesLoading } = useTemplatesByType(surveyType)

  const isSmsAllowed = subscription?.plan?.name !== 'Free' && subscription?.plan?.name != null
  const selectedList = distributionLists.find(l => String(l.id) === String(selectedListId))
  const smsContactCount = enableSmsDispatch && selectedList ? (selectedList.contacts?.length ?? 0) : null

  useEffect(() => {
    if (editSurveyId) {
      surveyAPI.getSurvey(editSurveyId).then(res => {
        const s = res.data
        updateSurvey({
          name: s.name, introduction: s.introduction, type: s.type,
          accessType: s.accessType, startDate: s.startDate, endDate: s.endDate,
          targetRespondents: s.targetRespondents, budget: s.budget,
          questions: s.questions?.map(q => ({
            id: q.id, text: q.questionText, type: q.questionType,
            required: true, options: q.options ? JSON.parse(q.options) : [],
          })) || [],
        })
        setSurveyType(s.type)
        if (s.budget) setBudget(s.budget)
        setStep(2)
      }).catch(console.error)
    } else {
      resetSurveyBuilder()
    }
  }, [editSurveyId])

  useEffect(() => {
    distributionAPI.getLists().then(r => setDistributionLists(r.data)).catch(() => {})
    billingAPI.getSubscription().then(r => setSubscription(r.data)).catch(() => {})
  }, [])

  useEffect(() => {
    const calc = async () => {
      const has = (currentSurvey.targetRespondents && parseInt(currentSurvey.targetRespondents) > 0) || (budget && parseFloat(budget) > 0) || (smsContactCount && smsContactCount > 0)
      if (!has) { setCostCalculation(null); return }
      setIsCalculatingCost(true)
      try {
        const res = await surveyAPI.calculateCost({
          targetRespondents: currentSurvey.targetRespondents ? parseInt(currentSurvey.targetRespondents) : null,
          budget: budget ? parseFloat(budget) : null,
          smsContactCount: smsContactCount || null,
        })
        setCostCalculation(res.data)
        if (budget && !currentSurvey.targetRespondents && res.data.targetRespondents) {
          updateSurvey({ targetRespondents: res.data.targetRespondents })
        }
      } catch { /* ignore */ } finally { setIsCalculatingCost(false) }
    }
    const t = setTimeout(calc, 600)
    return () => clearTimeout(t)
  }, [currentSurvey.targetRespondents, budget, smsContactCount])

  const handleAiGenerate = async () => {
    if (!aiTopic) return
    setIsGenerating(true)
    try {
      const res = await aiAPI.generateQuestions({ topic: aiTopic, type: aiType, sector: aiSector || 'General', questionCount: aiQuestionCount ? parseInt(aiQuestionCount) : 5 })
      const qs = res.data.map((q, i) => ({ id: Date.now() + i, text: q.questionText, type: q.questionType || 'FREE_TEXT', required: true, options: q.options ? JSON.parse(q.options) : [] }))
      const validTypes = ['NPS', 'CES', 'CSAT']
      updateSurvey({ name: aiTopic + ' Survey', type: validTypes.includes(aiType?.toUpperCase()) ? aiType.toUpperCase() : 'NPS', questions: qs })
      setStep(2)
    } catch { toast.error('Failed to generate questions. Try refining your topic.') }
    finally { setIsGenerating(false) }
  }

  const handleTemplateSelection = (template) => {
    setSelectedTemplate(template)
    if (template) {
      updateSurvey({ questions: template.questions?.map((q, i) => ({ id: Date.now() + i, text: q.questionText, type: q.questionType, required: true, options: q.options ? JSON.parse(q.options) : [] })) || [] })
    }
    setStep(2)
  }

  const buildPayload = () => ({
    name: currentSurvey.name, introduction: currentSurvey.introduction || '', type: currentSurvey.type,
    accessType: currentSurvey.accessType, startDate: currentSurvey.startDate, endDate: currentSurvey.endDate,
    targetRespondents: parseInt(currentSurvey.targetRespondents || 0), budget: budget ? parseFloat(budget) : null,
    questions: currentSurvey.questions.map((q, i) => ({ questionText: q.text, questionType: q.type, position: i, options: JSON.stringify(q.options || []) })),
  })

  const handleSaveDraft = async () => {
    setSaveError(''); setIsSavingDraft(true)
    try {
      let id = editSurveyId ? parseInt(editSurveyId) : null
      if (editSurveyId) await surveyAPI.updateSurvey(editSurveyId, buildPayload())
      else { const r = await createSurveyMutation.mutateAsync(buildPayload()); id = r.data.id }
      toast.success('Survey saved as draft!')
      if (currentSurvey.type === 'PERFORMANCE' && id) navigate(`/dashboard/competitions/setup/${id}`)
      else navigate('/dashboard')
    } catch (err) { setSaveError(err.response?.data?.message || 'Failed to save survey.') }
    finally { setIsSavingDraft(false) }
  }

  const handleLaunch = async () => {
    setSaveError(''); setIsLaunching(true)
    try {
      let id = editSurveyId ? parseInt(editSurveyId) : null
      if (editSurveyId) await surveyAPI.updateSurvey(editSurveyId, buildPayload())
      else { const r = await createSurveyMutation.mutateAsync(buildPayload()); id = r.data.id }
      if (currentSurvey.type === 'PERFORMANCE') {
        toast.success('Survey saved! Configure scoring and subjects.')
        navigate(`/dashboard/competitions/setup/${id}`)
        return
      }
      await surveyAPI.activateSurvey(id)
      if (enableSmsDispatch && selectedListId) await surveyAPI.sendToDistributionList(id, selectedListId)
      toast.success('Survey launched!')
      navigate('/dashboard')
    } catch (err) { setSaveError(err.response?.data?.message || 'Failed to launch.') }
    finally { setIsLaunching(false) }
  }

  return (
    <div className="max-w-5xl mx-auto space-y-8">
      <Stepper steps={STEPS} current={step} />
      <h1 className="text-2xl font-bold text-[var(--text)]">{editSurveyId ? 'Edit Survey' : 'Create Survey'}</h1>

      {/* Step 0: Method */}
      {step === 0 && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div onClick={() => { setIsAiMode(true); setStep(1) }} className="card-hover text-center py-10 cursor-pointer group">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-purple-500/10 group-hover:scale-110 transition-transform">
              <HiOutlineSparkles className="h-7 w-7 text-purple-500" />
            </div>
            <h3 className="text-lg font-bold text-[var(--text)]">Generate with AI</h3>
            <p className="mt-2 text-sm text-[var(--text-muted)]">Describe your goal and let AI build the survey.</p>
          </div>
          <div className="card-hover text-center py-10">
            <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-brand/10">
              <HiOutlinePlus className="h-7 w-7 text-brand" />
            </div>
            <h3 className="text-lg font-bold text-[var(--text)]">Build Manually</h3>
            <p className="mt-2 text-sm text-[var(--text-muted)]">Choose a type and build your questions.</p>
            <div className="mt-4 flex flex-wrap justify-center gap-2">
              {['NPS', 'CES', 'CSAT'].map(t => (
                <button key={t} onClick={() => { setSurveyType(t); updateSurvey({ type: t }); setIsAiMode(false); setStep(1) }}
                  className="rounded-full border border-brand px-3 py-1 text-xs font-medium text-brand hover:bg-brand hover:text-white transition-colors">
                  {t}
                </button>
              ))}
            </div>
          </div>
        </div>
      )}

      {/* Step 1: AI or Templates */}
      {step === 1 && isAiMode && (
        <div className="card space-y-5">
          <h2 className="text-xl font-semibold text-[var(--text)] flex items-center gap-2">
            <HiOutlineSparkles className="text-purple-500" /> AI Generator
          </h2>
          <div>
            <label className="mb-1.5 block text-sm font-medium text-[var(--text)]">What is this survey about?</label>
            <textarea className="input-field min-h-[100px]" placeholder="e.g. Gather feedback about new health insurance benefits..." value={aiTopic} onChange={(e) => setAiTopic(e.target.value)} />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="mb-1.5 block text-sm font-medium text-[var(--text)]">Survey Type</label>
              <select className="input-field" value={aiType} onChange={(e) => setAiType(e.target.value)}>
                <option value="">Select type...</option>
                <option value="NPS">NPS</option><option value="CES">CES</option><option value="CSAT">CSAT</option>
              </select>
            </div>
            <div>
              <label className="mb-1.5 block text-sm font-medium text-[var(--text)]">Sector</label>
              <input className="input-field" placeholder="e.g. Healthcare" value={aiSector} onChange={(e) => setAiSector(e.target.value)} />
            </div>
            <div>
              <label className="mb-1.5 block text-sm font-medium text-[var(--text)]">Questions</label>
              <input className="input-field" type="number" placeholder="Default: 5" value={aiQuestionCount} onChange={(e) => setAiQuestionCount(e.target.value)} />
            </div>
          </div>
          <div className="flex justify-between pt-2">
            <Button variant="secondary" onClick={() => setStep(0)}>Back</Button>
            <Button onClick={handleAiGenerate} loading={isGenerating} disabled={!aiTopic}>
              <HiOutlineSparkles className="h-4 w-4" /> Generate Questions
            </Button>
          </div>
        </div>
      )}

      {step === 1 && !isAiMode && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-[var(--text)]">Templates for {surveyType}</h2>
            <Button variant="secondary" size="sm" onClick={() => handleTemplateSelection(null)}>Skip — Blank</Button>
          </div>
          {templatesLoading ? (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">{Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}</div>
          ) : templates?.length ? (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {templates.map(t => (
                <div key={t.id} className="card-hover cursor-pointer" onClick={() => handleTemplateSelection(t)}>
                  <h3 className="font-bold text-[var(--text)]">{t.name}</h3>
                  <p className="mt-1 text-sm text-[var(--text-muted)] line-clamp-3">{t.description}</p>
                  <Badge className="mt-2">{t.questions?.length} Questions</Badge>
                </div>
              ))}
            </div>
          ) : (
            <div className="card text-center py-8">
              <p className="text-[var(--text-muted)]">No templates for {surveyType}.</p>
              <Button size="sm" className="mt-3" onClick={() => handleTemplateSelection(null)}>Create Blank Survey</Button>
            </div>
          )}
          <Button variant="secondary" onClick={() => setStep(0)}>Back</Button>
        </div>
      )}

      {/* Step 2: Questions */}
      {step === 2 && (
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <h2 className="text-xl font-semibold text-[var(--text)]">Edit Questions</h2>
            <div className="flex gap-2">
              <Button variant="secondary" size="sm" onClick={() => setStep(1)}>Back</Button>
              <Button size="sm" onClick={() => setStep(3)} disabled={!currentSurvey.questions.length}>
                Settings <HiOutlineArrowRight className="h-4 w-4" />
              </Button>
            </div>
          </div>
          {currentSurvey.questions.map((q, i) => (
            <QuestionEditor key={q.id} question={q} index={i} onChange={updateQuestion} onRemove={() => removeQuestion(q.id)} />
          ))}
          <button
            onClick={() => addQuestion({ id: Date.now(), text: '', type: 'FREE_TEXT', required: true, options: [] })}
            className="w-full rounded-xl border-2 border-dashed border-[var(--border)] py-4 text-sm font-medium text-[var(--text-muted)] hover:border-brand hover:text-brand transition-colors"
          >
            <HiOutlinePlus className="inline h-4 w-4 mr-1" /> Add Question
          </button>

          {/* Consent */}
          <ConsentSettings
            requiresConsent={currentSurvey.requiresConsent}
            consentMessage={currentSurvey.consentMessage}
            onChange={updateSurvey}
          />
        </div>
      )}

      {/* Step 3: Settings */}
      {step === 3 && (
        <SurveySettingsPanel
          survey={currentSurvey} onUpdate={updateSurvey}
          budget={budget} setBudget={setBudget}
          costCalculation={costCalculation} isCalculatingCost={isCalculatingCost}
          onBack={() => setStep(2)} onNext={() => setStep(4)}
        />
      )}

      {/* Step 4: Review */}
      {step === 4 && (
        <SurveyReviewPanel
          survey={currentSurvey} costCalculation={costCalculation}
          enableSmsDispatch={enableSmsDispatch} setEnableSmsDispatch={setEnableSmsDispatch}
          isSmsAllowed={isSmsAllowed}
          distributionLists={distributionLists} selectedListId={selectedListId} setSelectedListId={setSelectedListId}
          isLaunching={isLaunching} isSavingDraft={isSavingDraft}
          onSaveDraft={handleSaveDraft} onLaunch={handleLaunch}
          onTopUp={() => setPaymentModalOpen(true)}
          onBack={() => setStep(3)}
          error={saveError} onDismissError={() => setSaveError('')}
        />
      )}

      <PaymentModal
        show={paymentModalOpen}
        onClose={() => setPaymentModalOpen(false)}
        mode="WALLET_TOPUP"
        amount={costCalculation?.requiredTopUpAmount}
        onPaymentSuccess={() => {
          setPaymentModalOpen(false)
          // Recalculate
          if (currentSurvey.targetRespondents || budget || smsContactCount) {
            surveyAPI.calculateCost({
              targetRespondents: currentSurvey.targetRespondents ? parseInt(currentSurvey.targetRespondents) : null,
              budget: budget ? parseFloat(budget) : null, smsContactCount,
            }).then(r => setCostCalculation(r.data)).catch(() => {})
          }
        }}
      />
    </div>
  )
}
