import Badge from '../ui/Badge'
import Button from '../ui/Button'
import { HiOutlineCheck, HiOutlineCurrencyDollar, HiOutlineLightningBolt, HiOutlinePhone } from 'react-icons/hi'

export default function SurveyReviewPanel({
  survey, costCalculation,
  enableSmsDispatch, setEnableSmsDispatch, isSmsAllowed,
  distributionLists, selectedListId, setSelectedListId,
  isLaunching, isSavingDraft,
  onSaveDraft, onLaunch, onTopUp, onBack,
  error, onDismissError,
}) {
  const hasCost = costCalculation?.totalCost > 0
  const canLaunch = !hasCost || costCalculation?.isSufficientFunds
  const selectedList = distributionLists.find(l => String(l.id) === String(selectedListId))

  return (
    <div className="card max-w-2xl mx-auto space-y-6">
      {/* Header */}
      <div className="text-center">
        <div className="mx-auto mb-4 flex h-14 w-14 items-center justify-center rounded-full bg-[var(--success)]/10">
          <HiOutlineCheck className="h-7 w-7 text-[var(--success)]" />
        </div>
        <h2 className="text-2xl font-bold text-[var(--text)]">Ready to Launch</h2>
        <p className="text-sm text-[var(--text-muted)]">Review details, then save as draft or launch.</p>
      </div>

      {/* Summary */}
      <div className="rounded-xl p-5 space-y-2.5 text-sm" style={{ backgroundColor: 'var(--surface-hover)' }}>
        <SummaryRow label="Name" value={survey.name} />
        <SummaryRow label="Type" value={<Badge color="blue">{survey.type}</Badge>} />
        <SummaryRow label="Questions" value={survey.questions?.length || 0} />
        <SummaryRow label="Access" value={survey.accessType} />
      </div>

      {/* Cost */}
      {hasCost && (
        <div className="rounded-xl border border-[var(--border)] p-5 space-y-2 text-sm">
          <p className="font-semibold text-[var(--text)] flex items-center gap-2">
            <HiOutlineCurrencyDollar className="h-4 w-4 text-brand" /> Cost Breakdown
          </p>
          {costCalculation.estimatedCost > 0 && (
            <div className="flex justify-between text-[var(--text-muted)]">
              <span>Respondent activation ({costCalculation.targetRespondents} x KES {costCalculation.costPerRespondent})</span>
              <span className="font-medium text-[var(--text)]">KES {Number(costCalculation.estimatedCost).toLocaleString()}</span>
            </div>
          )}
          {costCalculation.smsCost > 0 && (
            <div className="flex justify-between text-[var(--text-muted)]">
              <span>SMS dispatch ({costCalculation.smsContactCount} x KES {costCalculation.smsCostPerMessage})</span>
              <span className="font-medium text-[var(--text)]">KES {Number(costCalculation.smsCost).toLocaleString()}</span>
            </div>
          )}
          <div className="flex justify-between font-bold text-[var(--text)] border-t border-[var(--border)] pt-2">
            <span>Total</span>
            <span>KES {Number(costCalculation.totalCost).toLocaleString()}</span>
          </div>
          <div className="flex justify-between text-[var(--text-muted)]">
            <span>Wallet</span>
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

      {/* SMS */}
      <div className="rounded-xl border border-[var(--border)] p-5 space-y-3">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-2 text-sm font-medium text-[var(--text)]">
            <HiOutlinePhone className="h-4 w-4" /> Send via SMS on launch
          </div>
          <button
            onClick={() => isSmsAllowed && setEnableSmsDispatch(!enableSmsDispatch)}
            className={`relative h-6 w-11 rounded-full transition-colors ${enableSmsDispatch && isSmsAllowed ? 'bg-brand' : 'bg-[var(--border)]'} ${!isSmsAllowed ? 'opacity-50 cursor-not-allowed' : 'cursor-pointer'}`}
          >
            <span className={`absolute top-0.5 left-0.5 h-5 w-5 rounded-full bg-white transition-transform ${enableSmsDispatch && isSmsAllowed ? 'translate-x-5' : ''}`} />
          </button>
        </div>
        {!isSmsAllowed && (
          <p className="text-xs text-[var(--accent)]">Upgrade your plan to enable SMS distribution.</p>
        )}
        {enableSmsDispatch && isSmsAllowed && (
          <div>
            <select className="input-field text-sm" value={selectedListId} onChange={(e) => setSelectedListId(e.target.value)}>
              <option value="">Select a distribution list</option>
              {distributionLists.map(l => (
                <option key={l.id} value={l.id}>{l.name} ({l.contacts?.length ?? 0} contacts)</option>
              ))}
            </select>
            {selectedList && costCalculation?.smsCost > 0 && (
              <p className="text-xs text-[var(--text-muted)] mt-1">
                SMS cost: KES {costCalculation.smsCostPerMessage} x {selectedList.contacts?.length ?? 0} = <strong>KES {Number(costCalculation.smsCost).toLocaleString()}</strong>
              </p>
            )}
          </div>
        )}
      </div>

      {error && (
        <div className="rounded-lg border border-[var(--error)]/30 bg-[var(--error)]/10 px-4 py-3 text-sm text-[var(--error)] flex justify-between items-start">
          <span>{error}</span>
          <button onClick={onDismissError} className="ml-2 font-bold">&times;</button>
        </div>
      )}

      {hasCost && !canLaunch && (
        <div className="rounded-lg border border-[var(--accent)]/30 bg-[var(--accent)]/10 px-4 py-3 text-sm text-[var(--accent)]">
          <strong>Insufficient funds.</strong> Top up your wallet to launch.
        </div>
      )}

      <div className="flex justify-between pt-2">
        <Button variant="secondary" onClick={onBack}>Back</Button>
        <div className="flex gap-3">
          <Button variant="secondary" onClick={onSaveDraft} loading={isSavingDraft} disabled={isLaunching}>
            Save as Draft
          </Button>
          {hasCost && !canLaunch ? (
            <Button variant="accent" onClick={onTopUp}>
              <HiOutlineCurrencyDollar className="h-4 w-4" />
              Top Up (KES {Number(costCalculation.requiredTopUpAmount).toLocaleString()})
            </Button>
          ) : (
            <Button onClick={onLaunch} loading={isLaunching} disabled={isSavingDraft}>
              <HiOutlineLightningBolt className="h-4 w-4" /> Launch Survey
            </Button>
          )}
        </div>
      </div>
    </div>
  )
}

function SummaryRow({ label, value }) {
  return (
    <div className="flex justify-between border-b border-[var(--border)] pb-2 last:border-0">
      <span className="font-medium text-[var(--text-muted)]">{label}</span>
      <span className="text-[var(--text)]">{value}</span>
    </div>
  )
}
