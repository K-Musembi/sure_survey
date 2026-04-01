import { useState, useEffect, useRef } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useTemplatesByType, useCreateSurvey, useUsage } from '../hooks/useApi'
import { aiAPI, distributionAPI, surveyAPI, rewardAPI } from '../services/apiServices'
import useSurveyStore from '../stores/surveyStore'
import UpgradeModal from '../components/UpgradeModal'
import PaymentModal from '../components/PaymentModal'
import Button from '../components/ui/Button'
import Card from '../components/ui/Card'
import Badge from '../components/ui/Badge'
import Label from '../components/ui/Label'
import Input from '../components/ui/Input'
import Select from '../components/ui/Select'
import Textarea from '../components/ui/Textarea'
import Spinner from '../components/ui/Spinner'
import Alert from '../components/ui/Alert'
import Stepper from '../components/ui/Stepper'
import ToggleSwitch from '../components/ui/ToggleSwitch'
import {
  HiPlus, HiTrash, HiLightningBolt, HiSparkles, HiArrowRight,
  HiCheck, HiExclamationCircle, HiCurrencyDollar, HiPhone, HiInformationCircle,
  HiOutlineLockClosed, HiOutlineGlobeAlt, HiOutlineChat, HiOutlineGift,
} from 'react-icons/hi'

const STEPS = ['Method', 'Content', 'Questions', 'Channels', 'Rewards', 'Settings', 'Review']

const SurveyBuilder = () => {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const editSurveyId = searchParams.get('edit')
  const paymentVerifiedRef = useRef(false)

  const [step, setStep] = useState(1)
  const {
    currentSurvey,
    updateSurvey,
    addQuestion,
    removeQuestion,
    updateQuestion,
    setSelectedTemplate,
    resetSurveyBuilder,
    channels,
    updateChannels,
    reward,
    updateReward,
  } = useSurveyStore()

  // UI state
  const [surveyType, setSurveyType] = useState('')
  const [isAiMode, setIsAiMode] = useState(false)
  const [aiTopic, setAiTopic] = useState('')
  const [aiType, setAiType] = useState('')
  const [aiSector, setAiSector] = useState('')
  const [aiQuestionCount, setAiQuestionCount] = useState('')
  const [isGenerating, setIsGenerating] = useState(false)
  const [aiError, setAiError] = useState('')
  const [distributionLists, setDistributionLists] = useState([])
  const [selectedListId, setSelectedListId] = useState('')
  const [enableSmsDispatch, setEnableSmsDispatch] = useState(false)
  const [showUpgrade, setShowUpgrade] = useState(false)
  const [upgradePlan, setUpgradePlan] = useState('Pro')

  // Cost calculation
  const [budget, setBudget] = useState('')
  const [costCalculation, setCostCalculation] = useState(null)
  const [isCalculatingCost, setIsCalculatingCost] = useState(false)
  const [paymentModalOpen, setPaymentModalOpen] = useState(false)

  // Save / launch state
  const [saveError, setSaveError] = useState('')
  const [isLaunching, setIsLaunching] = useState(false)
  const [isSavingDraft, setIsSavingDraft] = useState(false)

  const createSurveyMutation = useCreateSurvey()
  const { data: templates, isLoading: templatesLoading } = useTemplatesByType(surveyType)
  const { data: usage } = useUsage()

  // Selected distribution list details (for SMS cost calc)
  const selectedList = distributionLists.find(l => String(l.id) === String(selectedListId))
  const smsContactCount = enableSmsDispatch && selectedList ? (selectedList.contacts?.length ?? 0) : null

  // Handle ?type=PERFORMANCE from Competitions page
  useEffect(() => {
    const typeParam = searchParams.get('type')
    if (typeParam && !editSurveyId) {
      setSurveyType(typeParam)
      updateSurvey({ type: typeParam })
      setStep(2) // Skip method selection, go to content
    }
  }, []) // eslint-disable-line react-hooks/exhaustive-deps

  // Handle payment callback redirect
  useEffect(() => {
    const ref = searchParams.get('payment_ref') || searchParams.get('reference')
    if (!ref || paymentVerifiedRef.current) return
    paymentVerifiedRef.current = true
    setSaveError('')
    const newParams = {}
    if (editSurveyId) newParams.edit = editSurveyId
    setSearchParams(newParams, { replace: true })
  }, [searchParams])

  // Load existing survey if editing
  useEffect(() => {
    if (editSurveyId) {
      surveyAPI.getSurvey(editSurveyId).then(res => {
        const s = res.data
        updateSurvey({
          name: s.name,
          introduction: s.introduction,
          type: s.type,
          accessType: s.accessType,
          startDate: s.startDate,
          endDate: s.endDate,
          targetRespondents: s.targetRespondents,
          budget: s.budget,
          questions: s.questions?.map(q => ({
            id: q.id,
            text: q.questionText,
            type: q.questionType,
            required: true,
            options: q.options ? JSON.parse(q.options) : [],
          })) || [],
        })
        setSurveyType(s.type)
        if (s.budget) setBudget(s.budget)
        setStep(3)
      }).catch(console.error)

      // Load existing reward config
      rewardAPI.getSurveyReward(editSurveyId).then(res => {
        if (res.data) {
          updateReward({
            enabled: true,
            type: res.data.type || 'LOYALTY_POINTS',
            amount: String(res.data.amount || ''),
          })
        }
      }).catch(() => {})
    } else {
      resetSurveyBuilder()
    }
  }, [editSurveyId, updateSurvey, resetSurveyBuilder])

  useEffect(() => {
    distributionAPI.getLists().then(res => setDistributionLists(res.data)).catch(console.error)
  }, [])

  // Recalculate cost when respondents, budget, or SMS contact count changes
  useEffect(() => {
    const calculate = async () => {
      const hasRespondents = currentSurvey.targetRespondents && parseInt(currentSurvey.targetRespondents) > 0
      const hasBudget = budget && parseFloat(budget) > 0
      const hasSms = smsContactCount && smsContactCount > 0

      if (!hasRespondents && !hasBudget && !hasSms) {
        setCostCalculation(null)
        return
      }

      setIsCalculatingCost(true)
      try {
        const res = await surveyAPI.calculateCost({
          targetRespondents: hasRespondents ? parseInt(currentSurvey.targetRespondents) : null,
          budget: hasBudget ? parseFloat(budget) : null,
          smsContactCount: hasSms ? smsContactCount : null,
        })
        setCostCalculation(res.data)
        if (hasBudget && !hasRespondents && res.data.targetRespondents) {
          updateSurvey({ targetRespondents: res.data.targetRespondents })
        }
      } catch {
        // silently ignore — cost calc is non-blocking
      } finally {
        setIsCalculatingCost(false)
      }
    }

    const timer = setTimeout(calculate, 600)
    return () => clearTimeout(timer)
  }, [currentSurvey.targetRespondents, budget, smsContactCount, updateSurvey])

  const handleTypeSelection = (type) => {
    setSurveyType(type)
    updateSurvey({ type })
    setIsAiMode(false)
    setStep(2)
  }

  const handleAiModeSelection = () => {
    setIsAiMode(true)
    setStep(2)
  }

  const handleAiGenerate = async () => {
    if (!aiTopic) return
    setIsGenerating(true)
    setAiError('')
    try {
      const response = await aiAPI.generateQuestions({
        topic: aiTopic,
        type: aiType,
        sector: aiSector || 'General',
        questionCount: aiQuestionCount ? parseInt(aiQuestionCount) : 5,
      })
      const generatedQuestions = response.data.map((q, idx) => ({
        id: Date.now() + idx,
        text: q.questionText,
        type: q.questionType || 'FREE_TEXT',
        required: true,
        options: q.options ? JSON.parse(q.options) : [],
      }))
      const validTypes = ['NPS', 'CES', 'CSAT']
      const finalType = validTypes.includes(aiType?.toUpperCase()) ? aiType.toUpperCase() : 'NPS'
      updateSurvey({ name: aiTopic + ' Survey', type: finalType, questions: generatedQuestions })
      setStep(3)
    } catch {
      setAiError('Failed to generate questions. Please try again or refine your topic.')
    } finally {
      setIsGenerating(false)
    }
  }

  const handleTemplateSelection = (template) => {
    setSelectedTemplate(template)
    if (template) {
      const qs = template.questions?.map((q, idx) => ({
        id: Date.now() + idx,
        text: q.questionText,
        type: q.questionType,
        required: true,
        options: q.options ? JSON.parse(q.options) : [],
      })) || []
      updateSurvey({ questions: qs })
    }
    setStep(3)
  }

  const handleAddQuestion = () => {
    addQuestion({ id: Date.now(), text: '', type: 'FREE_TEXT', required: true, options: [] })
  }

  const isChannelAllowed = (channel) => {
    if (!usage) return channel === 'WEB'
    return usage.allowedChannels?.map(c => c.toUpperCase()).includes(channel.toUpperCase())
  }

  const buildSurveyPayload = () => ({
    name: currentSurvey.name,
    introduction: currentSurvey.introduction || '',
    type: currentSurvey.type,
    accessType: currentSurvey.accessType,
    startDate: currentSurvey.startDate,
    endDate: currentSurvey.endDate,
    targetRespondents: parseInt(currentSurvey.targetRespondents || 0),
    budget: budget ? parseFloat(budget) : null,
    questions: currentSurvey.questions.map((q, index) => ({
      questionText: q.text,
      questionType: q.type,
      position: index,
      options: JSON.stringify(q.options || []),
    })),
  })

  // Save reward config, returns error message or null
  const saveRewardConfig = async (surveyId) => {
    if (!reward.enabled || !reward.amount || !surveyId) return null
    try {
      await rewardAPI.configureReward({
        surveyId,
        type: reward.type,
        amount: parseFloat(reward.amount),
      })
      return null
    } catch (err) {
      return err.response?.data?.message || 'Reward configuration failed. The survey was saved but rewards were not applied.'
    }
  }

  // Save as Draft only (no activation, no SMS dispatch)
  const handleSaveDraft = async () => {
    setSaveError('')
    setIsSavingDraft(true)
    try {
      let surveyId = editSurveyId ? parseInt(editSurveyId) : null

      if (editSurveyId) {
        await surveyAPI.updateSurvey(editSurveyId, buildSurveyPayload())
      } else {
        const res = await createSurveyMutation.mutateAsync(buildSurveyPayload())
        surveyId = res.data.id
      }

      const rewardErr = await saveRewardConfig(surveyId)
      if (rewardErr) {
        setSaveError(rewardErr)
        return
      }

      navigate('/dashboard')
    } catch (err) {
      setSaveError(err.response?.data?.message || 'Failed to save survey. Please check your inputs.')
    } finally {
      setIsSavingDraft(false)
    }
  }

  // Launch: create (or update) -> activate -> optionally send via SMS
  const handleLaunch = async () => {
    setSaveError('')
    setIsLaunching(true)
    try {
      let surveyId = editSurveyId ? parseInt(editSurveyId) : null

      if (editSurveyId) {
        await surveyAPI.updateSurvey(editSurveyId, buildSurveyPayload())
      } else {
        const res = await createSurveyMutation.mutateAsync(buildSurveyPayload())
        surveyId = res.data.id
      }

      const rewardErr = await saveRewardConfig(surveyId)
      if (rewardErr) {
        setSaveError(rewardErr)
        return
      }

      // Activate survey
      await surveyAPI.activateSurvey(surveyId)

      // Send via SMS distribution list if opted in
      if (enableSmsDispatch && selectedListId) {
        await surveyAPI.sendToDistributionList(surveyId, selectedListId)
      }

      navigate('/dashboard')
    } catch (err) {
      setSaveError(err.response?.data?.message || 'Failed to launch survey. Please try again.')
    } finally {
      setIsLaunching(false)
    }
  }

  const handlePaymentSuccess = () => {
    setPaymentModalOpen(false)
    if (currentSurvey.targetRespondents || budget || smsContactCount) {
      surveyAPI.calculateCost({
        targetRespondents: currentSurvey.targetRespondents ? parseInt(currentSurvey.targetRespondents) : null,
        budget: budget ? parseFloat(budget) : null,
        smsContactCount,
      }).then(res => setCostCalculation(res.data)).catch(console.error)
    }
  }

  const hasCost = costCalculation && costCalculation.totalCost > 0
  const canLaunch = !hasCost || (costCalculation && costCalculation.isSufficientFunds)

  // Channels summary for review
  const activeChannels = Object.entries(channels).filter(([, v]) => v).map(([k]) => k.toUpperCase())

  return (
    <div className="max-w-5xl mx-auto px-4 py-8">
      {/* Progress Stepper */}
      <div className="mb-8">
        <Stepper steps={STEPS} current={step - 1} />
      </div>

      <div className="mb-6">
        <h1 className="text-3xl font-bold text-[var(--text)]">{editSurveyId ? 'Edit Survey' : 'Create Survey'}</h1>
      </div>

      {/* Step 1: Method */}
      {step === 1 && (
        <Card>
          <h2 className="text-xl font-semibold text-[var(--text)] mb-4">How do you want to start?</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div
              onClick={handleAiModeSelection}
              className="border-2 border-transparent hover:border-purple-500 bg-purple-500/10 p-6 rounded-xl cursor-pointer transition-all text-center group"
            >
              <div className="w-16 h-16 bg-[var(--surface)] rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm group-hover:scale-110 transition-transform">
                <HiSparkles className="w-8 h-8 text-purple-600" />
              </div>
              <h3 className="font-bold text-[var(--text)] text-lg">Generate with AI</h3>
              <p className="text-sm text-[var(--text-muted)] mt-2">Describe your goal and let our AI build the perfect survey.</p>
            </div>
            <div className="border-2 border-transparent hover:border-blue-500 bg-blue-500/10 p-6 rounded-xl cursor-pointer transition-all text-center group">
              <div className="w-16 h-16 bg-[var(--surface)] rounded-full flex items-center justify-center mx-auto mb-4 shadow-sm group-hover:scale-110 transition-transform">
                <HiPlus className="w-8 h-8 text-blue-600" />
              </div>
              <h3 className="font-bold text-[var(--text)] text-lg">Build custom survey</h3>
              <p className="text-sm text-[var(--text-muted)] mt-2">Choose a survey type and build your questions manually.</p>
              <div className="font-bold mt-4 flex flex-wrap justify-center gap-2">
                {['NPS', 'CES', 'CSAT'].map(type => (
                  <Badge
                    key={type}
                    color="blue"
                    className="cursor-pointer"
                    onClick={(e) => { e.stopPropagation(); handleTypeSelection(type) }}
                  >
                    {type}
                  </Badge>
                ))}
              </div>
            </div>
          </div>
        </Card>
      )}

      {/* Step 2: AI Input */}
      {step === 2 && isAiMode && (
        <Card>
          <h2 className="text-xl font-semibold text-[var(--text)] mb-4 flex items-center gap-2">
            <HiSparkles className="text-purple-600" /> AI Generator
          </h2>
          <div className="space-y-4">
            <div>
              <Label htmlFor="aiTopic">What is this survey about?</Label>
              <Textarea
                id="aiTopic"
                rows={4}
                placeholder="e.g. Gather feedback from employees about the new health insurance benefits..."
                value={aiTopic}
                onChange={(e) => setAiTopic(e.target.value)}
              />
            </div>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div>
                <Label htmlFor="aiType">Survey Type</Label>
                <Select id="aiType" value={aiType} onChange={(e) => setAiType(e.target.value)}>
                  <option value="">Select type...</option>
                  <option value="NPS">NPS (Net Promoter Score)</option>
                  <option value="CES">CES (Customer Effort Score)</option>
                  <option value="CSAT">CSAT (Customer Satisfaction)</option>
                </Select>
              </div>
              <div>
                <Label htmlFor="aiSector">Industry / Sector</Label>
                <Input id="aiSector" placeholder="e.g. Healthcare, Retail" value={aiSector} onChange={(e) => setAiSector(e.target.value)} />
              </div>
              <div>
                <Label htmlFor="aiQuestionCount">No. of Questions</Label>
                <Input id="aiQuestionCount" type="number" placeholder="Default: 5" value={aiQuestionCount} onChange={(e) => setAiQuestionCount(e.target.value)} />
              </div>
            </div>
            {aiError && <Alert color="failure">{aiError}</Alert>}
            <div className="flex justify-between pt-4">
              <Button variant="secondary" onClick={() => setStep(1)}>Back</Button>
              <Button onClick={handleAiGenerate} disabled={isGenerating || !aiTopic}>
                {isGenerating ? <><Spinner size="sm" className="mr-2" /> Generating...</> : <><HiSparkles className="mr-2 h-5 w-5" /> Generate Questions</>}
              </Button>
            </div>
          </div>
        </Card>
      )}

      {/* Step 2: Templates */}
      {step === 2 && !isAiMode && (
        <div className="space-y-4">
          <div className="flex justify-between items-center">
            <h2 className="text-xl font-semibold text-[var(--text)]">Select a Template for {surveyType}</h2>
            <Button variant="secondary" onClick={() => handleTemplateSelection(null)}>Skip to Blank</Button>
          </div>
          {templatesLoading ? <div className="text-center py-8"><Spinner size="lg" /></div> : (
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              {templates?.map(t => (
                <Card key={t.id} hover className="cursor-pointer" onClick={() => handleTemplateSelection(t)}>
                  <h3 className="font-bold text-[var(--text)]">{t.name}</h3>
                  <p className="text-sm text-[var(--text-muted)] line-clamp-3">{t.description}</p>
                  <Badge className="w-fit mt-2">{t.questions?.length} Questions</Badge>
                </Card>
              ))}
              {!templates?.length && (
                <div className="col-span-full text-center py-8 bg-[var(--surface-hover)] rounded-lg border-2 border-dashed border-[var(--border)]">
                  <p className="text-[var(--text-muted)]">No templates found for {surveyType}.</p>
                  <Button size="sm" className="mt-4" onClick={() => handleTemplateSelection(null)}>Create Blank Survey</Button>
                </div>
              )}
            </div>
          )}
          <Button variant="secondary" onClick={() => setStep(1)} className="mt-4">Back</Button>
        </div>
      )}

      {/* Step 3: Questions Editor */}
      {step === 3 && (
        <div className="space-y-6">
          <Card className="sticky top-4 z-10 shadow-md border-b-4 border-brand">
            <div className="flex justify-between items-center">
              <h2 className="text-xl font-bold text-[var(--text)]">Edit Questions</h2>
              <div className="flex gap-2">
                <Button variant="secondary" size="sm" onClick={() => setStep(2)}>Back</Button>
                <Button size="sm" onClick={() => setStep(4)} disabled={currentSurvey.questions.length === 0 || currentSurvey.questions.some(q => !q.text?.trim())}>
                  Next: Channels <HiArrowRight className="ml-2 h-4 w-4" />
                </Button>
              </div>
            </div>
          </Card>
          <div className="space-y-4">
            {currentSurvey.questions.map((q, idx) => (
              <Card key={q.id}>
                <div className="flex gap-4">
                  <div className="w-8 h-8 rounded-full bg-[var(--surface-hover)] flex items-center justify-center font-bold text-[var(--text-muted)] flex-shrink-0">{idx + 1}</div>
                  <div className="flex-grow space-y-3">
                    <Input
                      value={q.text}
                      onChange={(e) => updateQuestion(q.id, { text: e.target.value })}
                      placeholder="Question text..."
                      required
                    />
                    <div className="flex justify-between items-center gap-4">
                      <div className="flex gap-4 items-center">
                        <Select value={q.type} onChange={(e) => updateQuestion(q.id, { type: e.target.value })} className="w-48">
                          <option value="FREE_TEXT">Free Text</option>
                          <option value="MULTIPLE_CHOICE_SINGLE">Single Choice</option>
                          <option value="MULTIPLE_CHOICE_MULTI">Multiple Choice</option>
                          <option value="RATING_LINEAR">Linear Scale (1-10)</option>
                          <option value="RATING_STAR">Star Rating</option>
                          <option value="NPS_SCALE">NPS (0-10)</option>
                        </Select>
                        <label className="flex items-center gap-2 text-sm text-[var(--text)]">
                          <input type="checkbox" checked={q.required} onChange={(e) => updateQuestion(q.id, { required: e.target.checked })} />
                          Required
                        </label>
                      </div>
                      <Button variant="danger" size="sm" onClick={() => removeQuestion(q.id)} className="flex items-center gap-1">
                        <HiTrash className="h-4 w-4" /> Delete
                      </Button>
                    </div>
                    {(q.type === 'MULTIPLE_CHOICE_SINGLE' || q.type === 'MULTIPLE_CHOICE_MULTI') && (
                      <div className="pl-4 border-l-2 border-[var(--border)] space-y-2">
                        {q.options?.map((opt, oIdx) => (
                          <div key={oIdx} className="flex gap-2">
                            <Input
                              value={opt}
                              className="flex-grow"
                              onChange={(e) => {
                                const newOpts = [...q.options]
                                newOpts[oIdx] = e.target.value
                                updateQuestion(q.id, { options: newOpts })
                              }}
                              placeholder={`Option ${oIdx + 1}`}
                            />
                            <Button variant="danger" size="sm" onClick={() => updateQuestion(q.id, { options: q.options.filter((_, i) => i !== oIdx) })}>
                              <HiTrash className="h-3 w-3" />
                            </Button>
                          </div>
                        ))}
                        <Button size="sm" variant="secondary" onClick={() => updateQuestion(q.id, { options: [...(q.options || []), ''] })}>
                          + Add Option
                        </Button>
                      </div>
                    )}
                  </div>
                </div>
              </Card>
            ))}
            <Button variant="secondary" className="w-full border-dashed border-2" onClick={handleAddQuestion}>
              <HiPlus className="mr-2 h-5 w-5" /> Add Question
            </Button>
          </div>
        </div>
      )}

      {/* Step 4: Channels */}
      {step === 4 && (
        <Card className="max-w-2xl mx-auto">
          <h2 className="text-xl font-semibold text-[var(--text)] mb-2">Distribution Channels</h2>
          <p className="text-sm text-[var(--text-muted)] mb-6">Choose how respondents will access your survey.</p>

          <div className="space-y-4">
            {/* Web — always available */}
            <div className="flex items-center justify-between rounded-lg border border-[var(--border)] p-4">
              <div className="flex items-center gap-3">
                <div className="rounded-lg p-2 bg-blue-500/10">
                  <HiOutlineGlobeAlt className="h-5 w-5 text-blue-600" />
                </div>
                <div>
                  <p className="font-medium text-[var(--text)]">Web Link</p>
                  <p className="text-xs text-[var(--text-muted)]">Shareable URL, embed in website or email</p>
                </div>
              </div>
              <ToggleSwitch checked={true} disabled onChange={() => {}} />
            </div>

            {/* SMS */}
            <div className={`flex items-center justify-between rounded-lg border p-4 ${!isChannelAllowed('SMS') ? 'border-[var(--border)] opacity-60' : 'border-[var(--border)]'}`}>
              <div className="flex items-center gap-3">
                <div className="rounded-lg p-2 bg-green-500/10">
                  <HiPhone className="h-5 w-5 text-green-600" />
                </div>
                <div>
                  <p className="font-medium text-[var(--text)] flex items-center gap-2">
                    SMS
                    {!isChannelAllowed('SMS') && <HiOutlineLockClosed className="h-3.5 w-3.5 text-[var(--text-muted)]" />}
                  </p>
                  <p className="text-xs text-[var(--text-muted)]">Send survey invitations via SMS</p>
                </div>
              </div>
              {isChannelAllowed('SMS') ? (
                <ToggleSwitch checked={channels.sms} onChange={(v) => {
                  updateChannels({ sms: v })
                  if (!v) { setEnableSmsDispatch(false); setSelectedListId('') }
                }} />
              ) : (
                <Button size="sm" variant="secondary" onClick={() => { setUpgradePlan('Pro'); setShowUpgrade(true) }}>
                  Upgrade
                </Button>
              )}
            </div>

            {/* WhatsApp */}
            <div className={`flex items-center justify-between rounded-lg border p-4 ${!isChannelAllowed('WHATSAPP') ? 'border-[var(--border)] opacity-60' : 'border-[var(--border)]'}`}>
              <div className="flex items-center gap-3">
                <div className="rounded-lg p-2 bg-emerald-500/10">
                  <HiOutlineChat className="h-5 w-5 text-emerald-600" />
                </div>
                <div>
                  <p className="font-medium text-[var(--text)] flex items-center gap-2">
                    WhatsApp
                    {!isChannelAllowed('WHATSAPP') && <HiOutlineLockClosed className="h-3.5 w-3.5 text-[var(--text-muted)]" />}
                  </p>
                  <p className="text-xs text-[var(--text-muted)]">Conversational survey via WhatsApp</p>
                </div>
              </div>
              {isChannelAllowed('WHATSAPP') ? (
                <ToggleSwitch checked={channels.whatsapp} onChange={(v) => updateChannels({ whatsapp: v })} />
              ) : (
                <Button size="sm" variant="secondary" onClick={() => { setUpgradePlan('Pro'); setShowUpgrade(true) }}>
                  Upgrade
                </Button>
              )}
            </div>

            {/* SMS Distribution List (shown when SMS is enabled) */}
            {channels.sms && isChannelAllowed('SMS') && (
              <div className="border border-green-500/30 bg-green-500/5 rounded-lg p-4 space-y-3">
                <p className="text-sm font-medium text-green-700 dark:text-green-400 flex items-center gap-2">
                  <HiPhone className="w-4 h-4" /> SMS Distribution
                </p>
                <ToggleSwitch
                  checked={enableSmsDispatch}
                  onChange={setEnableSmsDispatch}
                  label="Send SMS on launch"
                />
                {enableSmsDispatch && (
                  <div>
                    <Select value={selectedListId} onChange={(e) => setSelectedListId(e.target.value)}>
                      <option value="">-- Select a distribution list --</option>
                      {distributionLists.map(l => (
                        <option key={l.id} value={l.id}>
                          {l.name} ({l.contacts?.length ?? 0} contacts)
                        </option>
                      ))}
                    </Select>
                    {selectedList && (
                      <p className="text-xs text-[var(--text-muted)] mt-1">
                        SMS cost: KES {costCalculation?.smsCostPerMessage ?? '2.00'} x {selectedList.contacts?.length ?? 0} contacts
                        {costCalculation?.smsCost > 0 && <> = <strong>KES {Number(costCalculation.smsCost).toLocaleString()}</strong></>}
                      </p>
                    )}
                  </div>
                )}
              </div>
            )}
          </div>

          <div className="flex justify-between mt-6">
            <Button variant="secondary" onClick={() => setStep(3)}>Back</Button>
            <Button onClick={() => setStep(5)}>
              Next: Rewards <HiArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </div>
        </Card>
      )}

      {/* Step 5: Rewards */}
      {step === 5 && (
        <Card className="max-w-2xl mx-auto">
          <h2 className="text-xl font-semibold text-[var(--text)] mb-2">Rewards</h2>
          <p className="text-sm text-[var(--text-muted)] mb-6">Incentivize respondents to complete your survey.</p>

          {usage?.rewards ? (
            <div className="space-y-4">
              <div className="flex items-center justify-between rounded-lg border border-[var(--border)] p-4">
                <div className="flex items-center gap-3">
                  <div className="rounded-lg p-2 bg-amber-500/10">
                    <HiOutlineGift className="h-5 w-5 text-amber-600" />
                  </div>
                  <div>
                    <p className="font-medium text-[var(--text)]">Enable Rewards</p>
                    <p className="text-xs text-[var(--text-muted)]">Respondents earn a reward upon completing the survey</p>
                  </div>
                </div>
                <ToggleSwitch checked={reward.enabled} onChange={(v) => updateReward({ enabled: v })} />
              </div>

              {reward.enabled && (
                <div className="bg-amber-500/10 border border-amber-500/20 rounded-lg p-4 space-y-4">
                  <div>
                    <Label>Reward Type</Label>
                    <Select value={reward.type} onChange={(e) => updateReward({ type: e.target.value })}>
                      <option value="LOYALTY_POINTS">Loyalty Points</option>
                      <option value="AIRTIME">Airtime</option>
                    </Select>
                  </div>
                  <div>
                    <Label>Amount per respondent {reward.type === 'LOYALTY_POINTS' ? '(points)' : '(KES)'}</Label>
                    <Input
                      type="number"
                      value={reward.amount}
                      onChange={(e) => updateReward({ amount: e.target.value })}
                      placeholder={reward.type === 'LOYALTY_POINTS' ? 'e.g. 50' : 'e.g. 20'}
                    />
                  </div>
                  {reward.type === 'AIRTIME' && (
                    <p className="text-xs text-amber-700 dark:text-amber-400 flex items-center gap-1">
                      <HiInformationCircle className="w-3.5 h-3.5" />
                      Airtime rewards are deducted from your wallet balance on fulfillment.
                    </p>
                  )}
                </div>
              )}
            </div>
          ) : (
            <div className="text-center py-8 bg-[var(--surface-hover)] rounded-lg border-2 border-dashed border-[var(--border)]">
              <HiOutlineLockClosed className="h-8 w-8 text-[var(--text-muted)] mx-auto mb-3" />
              <p className="text-[var(--text)] font-medium">Rewards require the Pro plan or higher</p>
              <p className="text-sm text-[var(--text-muted)] mt-1 mb-4">Incentivize respondents with loyalty points or airtime</p>
              <Button size="sm" onClick={() => { setUpgradePlan('Pro'); setShowUpgrade(true) }}>
                Upgrade to Pro
              </Button>
            </div>
          )}

          <div className="flex justify-between mt-6">
            <Button variant="secondary" onClick={() => setStep(4)}>Back</Button>
            <Button onClick={() => setStep(6)} disabled={reward.enabled && (!reward.amount || parseFloat(reward.amount) <= 0)}>
              Next: Settings <HiArrowRight className="ml-2 h-4 w-4" />
            </Button>
          </div>
        </Card>
      )}

      {/* Step 6: Settings */}
      {step === 6 && (
        <Card className="max-w-2xl mx-auto">
          <h2 className="text-xl font-semibold text-[var(--text)] mb-6">Survey Settings</h2>
          <div className="space-y-4">
            <div>
              <Label>Survey Name</Label>
              <Input value={currentSurvey.name} onChange={(e) => updateSurvey({ name: e.target.value })} required />
            </div>
            <div>
              <Label>Introduction / Welcome Message</Label>
              <Textarea
                value={currentSurvey.introduction || ''}
                onChange={(e) => updateSurvey({ introduction: e.target.value })}
                placeholder="Welcome to our survey..."
                rows={3}
              />
            </div>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label>Access Type</Label>
                <Select value={currentSurvey.accessType} onChange={(e) => updateSurvey({ accessType: e.target.value })}>
                  <option value="PUBLIC">Public Link</option>
                  <option value="PRIVATE">Invitation Only</option>
                </Select>
              </div>
              <div>
                <Label>Start Date</Label>
                <Input type="datetime-local" value={currentSurvey.startDate || ''} onChange={(e) => updateSurvey({ startDate: e.target.value })} />
              </div>
            </div>
            <div>
              <Label>End Date</Label>
              <Input type="datetime-local" value={currentSurvey.endDate || ''} onChange={(e) => updateSurvey({ endDate: e.target.value })} />
            </div>

            {/* Paid respondent targeting */}
            <div className="bg-amber-500/10 border border-amber-500/20 rounded-lg p-4 space-y-3">
              <div className="flex items-center gap-2 text-sm font-medium text-amber-700 dark:text-amber-400">
                <HiCurrencyDollar className="w-4 h-4" />
                Paid Response Targeting (optional)
              </div>
              <p className="text-xs text-amber-700 dark:text-amber-400">Set a target or budget. You will be charged from your wallet when you launch the survey.</p>
              <div className="flex gap-2 items-center">
                <div className="flex-1">
                  <Label>Target Respondents</Label>
                  <Input
                    type="number"
                    value={currentSurvey.targetRespondents || ''}
                    onChange={(e) => { updateSurvey({ targetRespondents: e.target.value }); setBudget('') }}
                    placeholder="e.g. 500"
                  />
                </div>
                <div className="flex items-end pb-1 text-[var(--text-muted)] text-sm">OR</div>
                <div className="flex-1">
                  <Label>Budget (KES)</Label>
                  <Input
                    type="number"
                    value={budget}
                    onChange={(e) => { setBudget(e.target.value); updateSurvey({ targetRespondents: '' }) }}
                    placeholder="e.g. 5000"
                  />
                </div>
              </div>
              {isCalculatingCost && <span className="text-xs text-brand flex items-center gap-1"><Spinner size="xs" /> Calculating...</span>}
              {costCalculation && costCalculation.estimatedCost > 0 && (
                <p className="text-xs text-[var(--text-muted)]">
                  Est. activation cost: <strong>KES {Number(costCalculation.estimatedCost).toLocaleString()}</strong>
                  {' '}({costCalculation.targetRespondents} respondents @ KES {costCalculation.costPerRespondent}/ea)
                </p>
              )}
            </div>
          </div>
          <div className="flex justify-between mt-6">
            <Button variant="secondary" onClick={() => setStep(5)}>Back</Button>
            <Button onClick={() => setStep(7)} disabled={!currentSurvey.name}>Review & Launch</Button>
          </div>
        </Card>
      )}

      {/* Step 7: Review */}
      {step === 7 && (
        <Card className="max-w-2xl mx-auto">
          <div className="text-center mb-6">
            <div className="w-16 h-16 bg-[var(--success)]/15 rounded-full flex items-center justify-center mx-auto mb-4">
              <HiCheck className="w-8 h-8 text-[var(--success)]" />
            </div>
            <h2 className="text-2xl font-bold text-[var(--text)]">Ready to Launch</h2>
            <p className="text-[var(--text-muted)] text-sm">Review details then choose to save as draft or launch immediately.</p>
          </div>

          {/* Summary */}
          <div className="bg-[var(--surface-hover)] rounded-lg p-5 space-y-2 text-sm mb-4">
            <Row label="Name" value={currentSurvey.name} />
            <Row label="Type" value={<Badge>{currentSurvey.type}</Badge>} />
            <Row label="Questions" value={currentSurvey.questions.length} />
            <Row label="Access" value={currentSurvey.accessType} />
            <Row label="Channels" value={activeChannels.join(', ')} />
            {reward.enabled && (
              <Row
                label="Reward"
                value={`${reward.amount} ${reward.type === 'LOYALTY_POINTS' ? 'points' : 'KES'} per respondent`}
              />
            )}
          </div>

          {/* Cost breakdown */}
          {costCalculation && costCalculation.totalCost > 0 && (
            <div className="border border-amber-500/20 bg-amber-500/10 rounded-lg p-4 space-y-2 text-sm mb-4">
              <p className="font-semibold text-amber-700 dark:text-amber-400 flex items-center gap-2">
                <HiCurrencyDollar className="w-4 h-4" /> Launch Cost Breakdown
              </p>
              {costCalculation.estimatedCost > 0 && (
                <div className="flex justify-between text-[var(--text)]">
                  <span>Respondent activation ({costCalculation.targetRespondents} x KES {costCalculation.costPerRespondent})</span>
                  <span className="font-medium">KES {Number(costCalculation.estimatedCost).toLocaleString()}</span>
                </div>
              )}
              {costCalculation.smsCost > 0 && (
                <div className="flex justify-between text-[var(--text)]">
                  <span>SMS dispatch ({costCalculation.smsContactCount} x KES {costCalculation.smsCostPerMessage}/SMS)</span>
                  <span className="font-medium">KES {Number(costCalculation.smsCost).toLocaleString()}</span>
                </div>
              )}
              <div className="flex justify-between font-bold text-[var(--text)] border-t border-[var(--border)] pt-2">
                <span>Total</span>
                <span>KES {Number(costCalculation.totalCost).toLocaleString()}</span>
              </div>
              <div className="flex justify-between text-[var(--text-muted)]">
                <span>Wallet balance</span>
                <span className={costCalculation.isSufficientFunds ? 'text-[var(--success)]' : 'text-[var(--error)]'}>
                  KES {Number(costCalculation.currentWalletBalance).toLocaleString()}
                </span>
              </div>
              {!costCalculation.isSufficientFunds && (
                <div className="flex justify-between font-medium text-[var(--error)]">
                  <span>Required top-up</span>
                  <span>KES {Number(costCalculation.requiredTopUpAmount).toLocaleString()}</span>
                </div>
              )}
            </div>
          )}

          {saveError && (
            <Alert color="failure" icon={HiExclamationCircle} onDismiss={() => setSaveError('')} className="mb-4">
              {saveError}
            </Alert>
          )}

          <div className="flex flex-col gap-3 mt-2">
            {hasCost && !canLaunch && (
              <Alert color="warning" className="text-sm">
                <span className="font-medium">Insufficient funds.</span> Top up your wallet to launch with paid targeting or SMS dispatch.
              </Alert>
            )}

            <div className="flex justify-between">
              <Button variant="secondary" onClick={() => setStep(6)}>Back</Button>
              <div className="flex gap-3">
                <Button variant="secondary" onClick={handleSaveDraft} disabled={isSavingDraft || isLaunching}>
                  {isSavingDraft ? <><Spinner size="sm" className="mr-2" /> Saving...</> : 'Save as Draft'}
                </Button>
                {hasCost && !canLaunch ? (
                  <Button variant="accent" onClick={() => setPaymentModalOpen(true)} disabled={isLaunching}>
                    <HiCurrencyDollar className="mr-2 h-4 w-4" />
                    Top Up (KES {costCalculation && Number(costCalculation.requiredTopUpAmount).toLocaleString()})
                  </Button>
                ) : (
                  <Button onClick={handleLaunch} disabled={isLaunching || isSavingDraft}>
                    {isLaunching
                      ? <><Spinner size="sm" className="mr-2" /> Launching...</>
                      : <><HiLightningBolt className="mr-2 h-4 w-4" /> Launch Survey</>
                    }
                  </Button>
                )}
              </div>
            </div>
          </div>
        </Card>
      )}

      <PaymentModal
        show={paymentModalOpen}
        onClose={() => setPaymentModalOpen(false)}
        mode="WALLET_TOPUP"
        amount={costCalculation?.requiredTopUpAmount}
        onPaymentSuccess={handlePaymentSuccess}
      />

      <UpgradeModal
        open={showUpgrade}
        onClose={() => setShowUpgrade(false)}
        currentPlan={usage?.planName || 'Free'}
        highlightPlan={upgradePlan}
      />
    </div>
  )
}

// Small helper for the summary rows
const Row = ({ label, value }) => (
  <div className="flex justify-between border-b border-[var(--border)] pb-2 last:border-0">
    <span className="font-semibold text-[var(--text)]">{label}</span>
    <span className="text-[var(--text)]">{value}</span>
  </div>
)

export default SurveyBuilder
