import Input from '../ui/Input'

export default function FormField({ label, helper, error, required, children, className = '', ...inputProps }) {
  return (
    <div className={className}>
      {label && (
        <label className="mb-1.5 block text-sm font-medium text-[var(--text)]">
          {label}
          {required && <span className="ml-0.5 text-[var(--error)]">*</span>}
        </label>
      )}
      {children || <Input error={error} {...inputProps} />}
      {helper && !error && (
        <p className="mt-1 text-xs text-[var(--text-muted)]">{helper}</p>
      )}
    </div>
  )
}
