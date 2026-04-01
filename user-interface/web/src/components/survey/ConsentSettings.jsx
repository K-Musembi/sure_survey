export default function ConsentSettings({ requiresConsent, consentMessage, onChange }) {
  return (
    <div className="rounded-xl border border-[var(--border)] p-5 space-y-3">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-[var(--text)]">Require Consent</p>
          <p className="text-xs text-[var(--text-muted)]">Respondents must agree before participating (ODPC compliance)</p>
        </div>
        <button
          onClick={() => onChange({ requiresConsent: !requiresConsent })}
          className={`relative h-6 w-11 rounded-full transition-colors cursor-pointer ${requiresConsent ? 'bg-brand' : 'bg-[var(--border)]'}`}
        >
          <span className={`absolute top-0.5 left-0.5 h-5 w-5 rounded-full bg-white transition-transform ${requiresConsent ? 'translate-x-5' : ''}`} />
        </button>
      </div>
      {requiresConsent && (
        <textarea
          className="input-field min-h-[60px] text-sm"
          value={consentMessage || ''}
          onChange={(e) => onChange({ consentMessage: e.target.value })}
          placeholder="I agree to participate in this survey and consent to data collection for the stated purpose."
        />
      )}
    </div>
  )
}
