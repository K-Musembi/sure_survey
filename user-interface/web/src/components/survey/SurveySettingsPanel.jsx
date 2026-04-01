import FormField from '../forms/FormField'
import Button from '../ui/Button'
import { HiOutlineCurrencyDollar } from 'react-icons/hi'

export default function SurveySettingsPanel({
  survey, onUpdate, budget, setBudget,
  costCalculation, isCalculatingCost,
  onBack, onNext,
}) {
  return (
    <div className="card max-w-2xl mx-auto space-y-6">
      <h2 className="text-xl font-semibold text-[var(--text)]">Survey Settings</h2>

      <FormField label="Survey Name" required>
        <input className="input-field" value={survey.name} onChange={(e) => onUpdate({ name: e.target.value })} required />
      </FormField>

      <FormField label="Introduction / Welcome Message">
        <textarea
          className="input-field min-h-[80px]"
          value={survey.introduction || ''}
          onChange={(e) => onUpdate({ introduction: e.target.value })}
          placeholder="Welcome to our survey..."
        />
      </FormField>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
        <FormField label="Access Type">
          <select className="input-field" value={survey.accessType} onChange={(e) => onUpdate({ accessType: e.target.value })}>
            <option value="PUBLIC">Public Link</option>
            <option value="PRIVATE">Invitation Only</option>
          </select>
        </FormField>
        <FormField label="Start Date">
          <input className="input-field" type="datetime-local" value={survey.startDate || ''} onChange={(e) => onUpdate({ startDate: e.target.value })} />
        </FormField>
      </div>

      <FormField label="End Date">
        <input className="input-field" type="datetime-local" value={survey.endDate || ''} onChange={(e) => onUpdate({ endDate: e.target.value })} />
      </FormField>

      {/* Paid targeting */}
      <div className="rounded-xl border border-[var(--border)] p-5 space-y-4" style={{ backgroundColor: 'var(--surface-hover)' }}>
        <div className="flex items-center gap-2 text-sm font-medium text-[var(--text)]">
          <HiOutlineCurrencyDollar className="h-4 w-4 text-brand" />
          Paid Response Targeting (optional)
        </div>
        <p className="text-xs text-[var(--text-muted)]">Set a target or budget. Wallet will be debited on launch.</p>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <FormField label="Target Respondents">
            <input
              className="input-field"
              type="number"
              value={survey.targetRespondents || ''}
              onChange={(e) => { onUpdate({ targetRespondents: e.target.value }); setBudget('') }}
              placeholder="e.g. 500"
            />
          </FormField>
          <FormField label="Budget (KES)">
            <input
              className="input-field"
              type="number"
              value={budget}
              onChange={(e) => { setBudget(e.target.value); onUpdate({ targetRespondents: '' }) }}
              placeholder="e.g. 5000"
            />
          </FormField>
        </div>
        {isCalculatingCost && (
          <p className="text-xs text-brand flex items-center gap-1">
            <span className="h-3 w-3 animate-spin rounded-full border-2 border-brand/30 border-t-brand" /> Calculating...
          </p>
        )}
        {costCalculation?.estimatedCost > 0 && (
          <p className="text-xs text-[var(--text-muted)]">
            Est. cost: <strong className="text-[var(--text)]">KES {Number(costCalculation.estimatedCost).toLocaleString()}</strong>
            {' '}({costCalculation.targetRespondents} respondents @ KES {costCalculation.costPerRespondent}/ea)
          </p>
        )}
      </div>

      <div className="flex justify-between pt-2">
        <Button variant="secondary" onClick={onBack}>Back</Button>
        <Button onClick={onNext} disabled={!survey.name}>Review & Launch</Button>
      </div>
    </div>
  )
}
