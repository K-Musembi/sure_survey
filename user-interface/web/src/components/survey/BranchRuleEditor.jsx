import { useState } from 'react'
import Button from '../ui/Button'
import Badge from '../ui/Badge'
import { HiOutlinePlus, HiOutlineTrash, HiOutlineSparkles } from 'react-icons/hi'

const conditionTypes = [
  { value: 'ANSWER_EQUALS', label: 'Answer Equals' },
  { value: 'SCORE_LT', label: 'Score Less Than' },
  { value: 'SCORE_GT', label: 'Score Greater Than' },
  { value: 'ALWAYS', label: 'Always' },
]

export default function BranchRuleEditor({ rules = [], questions = [], onCreate, onDelete, onSuggest, loading }) {
  const [form, setForm] = useState({ conditionType: 'ANSWER_EQUALS', sourceQuestionId: '', conditionValue: '', targetQuestionId: '' })

  const handleAdd = () => {
    if (!form.sourceQuestionId || !form.targetQuestionId) return
    onCreate(form)
    setForm({ conditionType: 'ANSWER_EQUALS', sourceQuestionId: '', conditionValue: '', targetQuestionId: '' })
  }

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <h3 className="text-lg font-semibold text-[var(--text)]">Branch Rules</h3>
        {onSuggest && (
          <Button variant="secondary" size="sm" onClick={onSuggest} loading={loading}>
            <HiOutlineSparkles className="h-4 w-4" /> AI Suggest
          </Button>
        )}
      </div>

      {rules.length > 0 ? (
        <div className="space-y-2">
          {rules.map((rule) => (
            <div key={rule.id} className="flex items-center justify-between rounded-lg border border-[var(--border)] p-3">
              <div className="flex items-center gap-2 text-sm">
                <Badge color="blue">{rule.conditionType}</Badge>
                <span className="text-[var(--text-muted)]">Q{rule.sourceQuestionId}</span>
                {rule.conditionValue && <span className="text-[var(--text)]">= "{rule.conditionValue}"</span>}
                <span className="text-[var(--text-muted)]">&rarr; Q{rule.targetQuestionId}</span>
              </div>
              <button onClick={() => onDelete(rule.id)} className="text-[var(--text-muted)] hover:text-[var(--error)] transition-colors">
                <HiOutlineTrash className="h-4 w-4" />
              </button>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-sm text-[var(--text-muted)]">No branch rules. Add rules to create conditional question flow.</p>
      )}

      {/* Add form */}
      <div className="rounded-xl border border-[var(--border)] p-4 space-y-3" style={{ backgroundColor: 'var(--surface-hover)' }}>
        <p className="text-sm font-medium text-[var(--text)]">Add Rule</p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
          <select className="input-field text-sm" value={form.conditionType} onChange={(e) => setForm({ ...form, conditionType: e.target.value })}>
            {conditionTypes.map((c) => <option key={c.value} value={c.value}>{c.label}</option>)}
          </select>
          <select className="input-field text-sm" value={form.sourceQuestionId} onChange={(e) => setForm({ ...form, sourceQuestionId: e.target.value })}>
            <option value="">Source question...</option>
            {questions.map((q, i) => <option key={q.id} value={q.id}>Q{i + 1}: {q.text || q.questionText}</option>)}
          </select>
          {form.conditionType !== 'ALWAYS' && (
            <input className="input-field text-sm" placeholder="Condition value" value={form.conditionValue} onChange={(e) => setForm({ ...form, conditionValue: e.target.value })} />
          )}
          <select className="input-field text-sm" value={form.targetQuestionId} onChange={(e) => setForm({ ...form, targetQuestionId: e.target.value })}>
            <option value="">Target question...</option>
            {questions.map((q, i) => <option key={q.id} value={q.id}>Q{i + 1}: {q.text || q.questionText}</option>)}
          </select>
        </div>
        <Button size="sm" onClick={handleAdd} disabled={!form.sourceQuestionId || !form.targetQuestionId}>
          <HiOutlinePlus className="h-4 w-4" /> Add Rule
        </Button>
      </div>
    </div>
  )
}
