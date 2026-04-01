import { HiOutlineTrash } from 'react-icons/hi'
import OptionsEditor from './OptionsEditor'
import QuestionTypeSelector from './QuestionTypeSelector'

export default function QuestionEditor({ question, index, onChange, onRemove }) {
  const update = (field, value) => onChange(question.id, { [field]: value })

  return (
    <div className="card">
      <div className="flex gap-4">
        <div className="flex h-8 w-8 flex-shrink-0 items-center justify-center rounded-full bg-brand/10 text-sm font-bold text-brand">
          {index + 1}
        </div>
        <div className="flex-1 space-y-3">
          <input
            className="input-field text-base font-medium"
            value={question.text}
            onChange={(e) => update('text', e.target.value)}
            placeholder="Question text..."
          />

          <div className="flex flex-wrap items-center gap-3">
            <QuestionTypeSelector value={question.type} onChange={(v) => update('type', v)} />
            <label className="flex items-center gap-2 text-sm text-[var(--text-muted)]">
              <input
                type="checkbox"
                checked={question.required}
                onChange={(e) => update('required', e.target.checked)}
                className="rounded border-[var(--border)]"
              />
              Required
            </label>
            <button onClick={onRemove} className="ml-auto rounded-lg p-1.5 text-[var(--text-muted)] hover:text-[var(--error)] hover:bg-[var(--error)]/10 transition-colors">
              <HiOutlineTrash className="h-4 w-4" />
            </button>
          </div>

          {(question.type === 'MULTIPLE_CHOICE_SINGLE' || question.type === 'MULTIPLE_CHOICE_MULTI') && (
            <OptionsEditor options={question.options || []} onChange={(opts) => update('options', opts)} />
          )}
        </div>
      </div>
    </div>
  )
}
