import { useState, useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { surveyAPI, competitionAPI, integrationAPI } from '../../services/apiServices'
import Stepper from '../../components/ui/Stepper'
import Button from '../../components/ui/Button'
import Card from '../../components/ui/Card'
import Label from '../../components/ui/Label'
import Input from '../../components/ui/Input'
import Select from '../../components/ui/Select'
import Badge from '../../components/ui/Badge'
import useToast from '../../hooks/useToast'
import {
  HiOutlinePlus, HiOutlineTrash, HiOutlineChevronRight,
  HiOutlineChevronLeft, HiOutlineCheck, HiOutlineLightningBolt,
} from 'react-icons/hi'

const STEPS = ['Scoring', 'Organization', 'Subjects']
const SCORING_STRATEGIES = [
  { value: 'DIRECT_VALUE', label: 'Direct Value — use the answer as the score' },
  { value: 'OPTION_MAP', label: 'Option Map — map each option to a score' },
]
const ORG_UNIT_TYPES = ['REGION', 'BRANCH', 'TEAM']
const SUBJECT_TYPES = ['INDIVIDUAL', 'BUSINESS_UNIT']
const ROLES = ['MEMBER', 'LEAD']

export default function CompetitionSetup() {
  const { surveyId } = useParams()
  const navigate = useNavigate()
  const toast = useToast()

  const [step, setStep] = useState(0)
  const [survey, setSurvey] = useState(null)
  const [loading, setLoading] = useState(true)

  // Step 1: Scoring
  const [targetScore, setTargetScore] = useState(100)
  const [defaultWeight, setDefaultWeight] = useState(1.0)
  const [rules, setRules] = useState([])
  const [savingScoring, setSavingScoring] = useState(false)

  // Step 2: Org hierarchy
  const [orgUnits, setOrgUnits] = useState([])
  const [newUnit, setNewUnit] = useState({ name: '', type: 'REGION', parentId: '' })
  const [savingUnit, setSavingUnit] = useState(false)

  // Step 3: Subjects
  const [subjects, setSubjects] = useState([])
  const [newSubject, setNewSubject] = useState({ displayName: '', referenceCode: '', type: 'INDIVIDUAL', role: 'MEMBER', orgUnitId: '' })
  const [savingSubject, setSavingSubject] = useState(false)
  const [integrations, setIntegrations] = useState([])

  // Final
  const [isLaunching, setIsLaunching] = useState(false)

  useEffect(() => {
    const load = async () => {
      try {
        const [surveyRes, intRes] = await Promise.all([
          surveyAPI.getSurvey(surveyId),
          integrationAPI.getIntegrations(),
        ])
        const s = surveyRes.data
        setSurvey(s)
        setIntegrations(intRes.data || [])

        // Pre-populate scoring rules from survey questions
        const questions = s.questions || []
        setRules(questions.map(q => ({
          questionId: q.id,
          questionText: q.questionText,
          questionType: q.questionType,
          options: q.options ? (typeof q.options === 'string' ? JSON.parse(q.options) : q.options) : [],
          weight: 1.0,
          scoringStrategy: 'DIRECT_VALUE',
          optionScoreMap: {},
        })))

        // Load existing org units
        try {
          const nodesRes = await competitionAPI.getNodes()
          setOrgUnits(nodesRes.data || [])
        } catch { /* no nodes yet */ }
      } catch {
        toast.error('Failed to load survey')
      } finally {
        setLoading(false)
      }
    }
    load()
  }, [surveyId])

  // Step 1: Save scoring schema
  const handleSaveScoring = async () => {
    setSavingScoring(true)
    try {
      await competitionAPI.createScoringSchema({
        surveyId: parseInt(surveyId),
        defaultQuestionWeight: defaultWeight,
        targetScore,
        rules: rules.map(r => ({
          questionId: r.questionId,
          weight: r.weight,
          scoringStrategy: r.scoringStrategy,
          optionScoreMap: r.scoringStrategy === 'OPTION_MAP' ? r.optionScoreMap : null,
        })),
      })
      toast.success('Scoring schema saved!')
      setStep(1)
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save scoring schema')
    } finally {
      setSavingScoring(false)
    }
  }

  // Step 2: Add org unit
  const handleAddUnit = async () => {
    if (!newUnit.name.trim()) return
    setSavingUnit(true)
    try {
      const res = await competitionAPI.createNode({
        name: newUnit.name,
        type: newUnit.type,
        parentId: newUnit.parentId || null,
      })
      setOrgUnits(prev => [...prev, res.data])
      setNewUnit({ name: '', type: 'REGION', parentId: '' })
      toast.success('Organization unit added!')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to create org unit')
    } finally {
      setSavingUnit(false)
    }
  }

  // Step 3: Add subject
  const handleAddSubject = async () => {
    if (!newSubject.displayName.trim() || !newSubject.referenceCode.trim()) return
    setSavingSubject(true)
    try {
      const res = await competitionAPI.createSubject({
        displayName: newSubject.displayName,
        referenceCode: newSubject.referenceCode,
        type: newSubject.type,
        role: newSubject.role,
        orgUnitId: newSubject.orgUnitId || orgUnits[0]?.id || null,
      })
      setSubjects(prev => [...prev, res.data])
      setNewSubject({ displayName: '', referenceCode: '', type: 'INDIVIDUAL', role: 'MEMBER', orgUnitId: '' })
      toast.success('Subject added!')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add subject')
    } finally {
      setSavingSubject(false)
    }
  }

  // Launch
  const handleLaunch = async () => {
    setIsLaunching(true)
    try {
      await surveyAPI.activateSurvey(surveyId)
      toast.success('Competition launched!')
      navigate('/dashboard/competitions')
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to launch competition')
    } finally {
      setIsLaunching(false)
    }
  }

  const updateRule = (idx, field, value) => {
    setRules(prev => prev.map((r, i) => i === idx ? { ...r, [field]: value } : r))
  }

  const updateOptionScore = (ruleIdx, optionKey, score) => {
    setRules(prev => prev.map((r, i) => i === ruleIdx
      ? { ...r, optionScoreMap: { ...r.optionScoreMap, [optionKey]: parseFloat(score) || 0 } }
      : r
    ))
  }

  if (loading) {
    return (
      <div className="flex min-h-[50vh] items-center justify-center">
        <div className="h-8 w-8 animate-spin rounded-full border-4 border-brand/30 border-t-brand" />
      </div>
    )
  }

  if (!survey) {
    return <p className="text-[var(--text-muted)] text-center py-16">Survey not found.</p>
  }

  return (
    <div className="max-w-4xl mx-auto space-y-8">
      <div>
        <h1 className="text-2xl font-bold text-[var(--text)]">Competition Setup</h1>
        <p className="text-sm text-[var(--text-muted)] mt-0.5">{survey.name}</p>
      </div>

      <Stepper steps={STEPS} current={step} />

      {/* Step 0: Scoring Schema */}
      {step === 0 && (
        <div className="space-y-6">
          <Card>
            <h3 className="text-lg font-semibold text-[var(--text)] mb-4">Scoring Configuration</h3>

            <div className="grid grid-cols-2 gap-4 mb-6">
              <div>
                <Label>Target Score</Label>
                <Input type="number" value={targetScore} onChange={e => setTargetScore(parseFloat(e.target.value) || 0)} />
              </div>
              <div>
                <Label>Default Question Weight</Label>
                <Input type="number" step="0.1" value={defaultWeight} onChange={e => setDefaultWeight(parseFloat(e.target.value) || 1)} />
              </div>
            </div>

            {rules.length === 0 ? (
              <p className="text-sm text-[var(--text-muted)]">This survey has no questions yet. Add questions in the Survey Builder first.</p>
            ) : (
              <div className="space-y-4">
                <h4 className="text-sm font-medium text-[var(--text-muted)]">Question Scoring Rules</h4>
                {rules.map((rule, idx) => (
                  <div key={rule.questionId} className="border border-[var(--border)] rounded-lg p-4 space-y-3">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-sm font-medium text-[var(--text)]">
                          Q{idx + 1}: {rule.questionText}
                        </p>
                        <Badge color="gray" className="mt-1">{rule.questionType}</Badge>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <Label>Weight</Label>
                        <Input
                          type="number"
                          step="0.1"
                          value={rule.weight}
                          onChange={e => updateRule(idx, 'weight', parseFloat(e.target.value) || 0)}
                        />
                      </div>
                      <div>
                        <Label>Scoring Strategy</Label>
                        <Select
                          value={rule.scoringStrategy}
                          onChange={e => updateRule(idx, 'scoringStrategy', e.target.value)}
                        >
                          {SCORING_STRATEGIES.map(s => (
                            <option key={s.value} value={s.value}>{s.label}</option>
                          ))}
                        </Select>
                      </div>
                    </div>

                    {rule.scoringStrategy === 'OPTION_MAP' && rule.options.length > 0 && (
                      <div className="bg-[var(--surface-hover)] p-3 rounded-lg space-y-2">
                        <p className="text-xs font-medium text-[var(--text-muted)]">Map each option to a score value:</p>
                        {rule.options.map((opt) => (
                          <div key={opt} className="flex items-center gap-3">
                            <span className="text-sm text-[var(--text)] flex-1 truncate">{opt}</span>
                            <Input
                              type="number"
                              step="0.1"
                              className="w-24"
                              placeholder="Score"
                              value={rule.optionScoreMap[opt] ?? ''}
                              onChange={e => updateOptionScore(idx, opt, e.target.value)}
                            />
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            )}
          </Card>

          <div className="flex justify-end">
            <Button onClick={handleSaveScoring} loading={savingScoring}>
              Save Scoring <HiOutlineChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}

      {/* Step 1: Organization Structure */}
      {step === 1 && (
        <div className="space-y-6">
          <Card>
            <h3 className="text-lg font-semibold text-[var(--text)] mb-2">Organization Structure</h3>
            <p className="text-sm text-[var(--text-muted)] mb-4">
              Define your organizational hierarchy (regions, branches, teams). This is optional for solo competitions.
            </p>

            {/* Existing units */}
            {orgUnits.length > 0 && (
              <div className="space-y-2 mb-4">
                {orgUnits.map(u => (
                  <div key={u.id} className="flex items-center gap-3 p-3 rounded-lg bg-[var(--surface-hover)]">
                    <Badge color="blue">{u.type}</Badge>
                    <span className="text-sm font-medium text-[var(--text)]">{u.name}</span>
                    {u.parentId && (
                      <span className="text-xs text-[var(--text-muted)]">
                        under {orgUnits.find(p => p.id === u.parentId)?.name || 'Unknown'}
                      </span>
                    )}
                  </div>
                ))}
              </div>
            )}

            {/* Add form */}
            <div className="border border-[var(--border)] rounded-lg p-4 space-y-3">
              <p className="text-sm font-medium text-[var(--text)]">Add Organization Unit</p>
              <div className="grid grid-cols-1 sm:grid-cols-3 gap-3">
                <div>
                  <Label>Name</Label>
                  <Input
                    value={newUnit.name}
                    onChange={e => setNewUnit({ ...newUnit, name: e.target.value })}
                    placeholder="e.g. Nairobi Region"
                  />
                </div>
                <div>
                  <Label>Type</Label>
                  <Select value={newUnit.type} onChange={e => setNewUnit({ ...newUnit, type: e.target.value })}>
                    {ORG_UNIT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                  </Select>
                </div>
                <div>
                  <Label>Parent (optional)</Label>
                  <Select value={newUnit.parentId} onChange={e => setNewUnit({ ...newUnit, parentId: e.target.value })}>
                    <option value="">None (top-level)</option>
                    {orgUnits.map(u => <option key={u.id} value={u.id}>{u.name}</option>)}
                  </Select>
                </div>
              </div>
              <Button size="sm" variant="secondary" onClick={handleAddUnit} loading={savingUnit}>
                <HiOutlinePlus className="h-4 w-4" /> Add Unit
              </Button>
            </div>
          </Card>

          <div className="flex justify-between">
            <Button variant="secondary" onClick={() => setStep(0)}>
              <HiOutlineChevronLeft className="h-4 w-4" /> Back
            </Button>
            <Button onClick={() => setStep(2)}>
              {orgUnits.length === 0 ? 'Skip' : 'Next'}: Subjects <HiOutlineChevronRight className="h-4 w-4" />
            </Button>
          </div>
        </div>
      )}

      {/* Step 2: Performance Subjects */}
      {step === 2 && (
        <div className="space-y-6">
          <Card>
            <h3 className="text-lg font-semibold text-[var(--text)] mb-2">Performance Subjects</h3>
            <p className="text-sm text-[var(--text-muted)] mb-4">
              Add the individuals or business units being evaluated. Use reference codes to link M-Pesa BillRefNumber transactions for automatic attribution.
            </p>

            {/* Integration hint */}
            {integrations.length > 0 && (
              <div className="bg-[var(--surface-hover)] border border-[var(--border)] rounded-lg p-3 mb-4">
                <div className="flex items-start gap-2">
                  <HiOutlineLightningBolt className="h-5 w-5 text-brand flex-shrink-0 mt-0.5" />
                  <div>
                    <p className="text-sm font-medium text-[var(--text)]">Auto-attribution enabled</p>
                    <p className="text-xs text-[var(--text-muted)]">
                      When a customer pays via M-Pesa and enters a subject's reference code as the BillRefNumber, their survey response will be automatically attributed to that subject.
                    </p>
                  </div>
                </div>
              </div>
            )}

            {/* Existing subjects */}
            {subjects.length > 0 && (
              <div className="space-y-2 mb-4">
                {subjects.map(s => (
                  <div key={s.id} className="flex items-center gap-3 p-3 rounded-lg bg-[var(--surface-hover)]">
                    <Badge color={s.type === 'INDIVIDUAL' ? 'blue' : 'accent'}>{s.type}</Badge>
                    <span className="text-sm font-medium text-[var(--text)]">{s.displayName}</span>
                    <span className="text-xs font-mono text-[var(--text-muted)]">ref: {s.referenceCode}</span>
                    <Badge color="gray">{s.role}</Badge>
                  </div>
                ))}
              </div>
            )}

            {/* Add form */}
            {orgUnits.length === 0 ? (
              <div className="border border-dashed border-[var(--border)] rounded-lg p-4 text-center">
                <p className="text-sm text-[var(--text-muted)] mb-2">
                  Create at least one organization unit before adding subjects.
                </p>
                <Button size="sm" variant="secondary" onClick={() => setStep(1)}>
                  <HiOutlineChevronLeft className="h-4 w-4" /> Go to Organization
                </Button>
              </div>
            ) : (
              <div className="border border-[var(--border)] rounded-lg p-4 space-y-3">
                <p className="text-sm font-medium text-[var(--text)]">Add Subject</p>
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
                  <div>
                    <Label>Display Name</Label>
                    <Input
                      value={newSubject.displayName}
                      onChange={e => setNewSubject({ ...newSubject, displayName: e.target.value })}
                      placeholder="e.g. John Kamau"
                    />
                  </div>
                  <div>
                    <Label>Reference Code</Label>
                    <Input
                      value={newSubject.referenceCode}
                      onChange={e => setNewSubject({ ...newSubject, referenceCode: e.target.value })}
                      placeholder="e.g. EMP001 or BillRefNumber"
                    />
                  </div>
                  <div>
                    <Label>Type</Label>
                    <Select value={newSubject.type} onChange={e => setNewSubject({ ...newSubject, type: e.target.value })}>
                      {SUBJECT_TYPES.map(t => <option key={t} value={t}>{t}</option>)}
                    </Select>
                  </div>
                  <div>
                    <Label>Role</Label>
                    <Select value={newSubject.role} onChange={e => setNewSubject({ ...newSubject, role: e.target.value })}>
                      {ROLES.map(r => <option key={r} value={r}>{r}</option>)}
                    </Select>
                  </div>
                  <div className="sm:col-span-2">
                    <Label>Assigned Unit</Label>
                    <Select value={newSubject.orgUnitId} onChange={e => setNewSubject({ ...newSubject, orgUnitId: e.target.value })}>
                      <option value="">Select unit...</option>
                      {orgUnits.map(u => <option key={u.id} value={u.id}>{u.name} ({u.type})</option>)}
                    </Select>
                  </div>
                </div>
                <Button size="sm" variant="secondary" onClick={handleAddSubject} loading={savingSubject}>
                  <HiOutlinePlus className="h-4 w-4" /> Add Subject
                </Button>
              </div>
            )}
          </Card>

          <div className="flex justify-between">
            <Button variant="secondary" onClick={() => setStep(1)}>
              <HiOutlineChevronLeft className="h-4 w-4" /> Back
            </Button>
            <Button onClick={handleLaunch} loading={isLaunching}>
              <HiOutlineCheck className="h-4 w-4" /> Launch Competition
            </Button>
          </div>
        </div>
      )}
    </div>
  )
}
