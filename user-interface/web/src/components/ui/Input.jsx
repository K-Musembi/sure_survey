import { forwardRef, useState } from 'react'

const Input = forwardRef(function Input(
  { type = 'text', error, validate, onBlur, className = '', ...props },
  ref
) {
  const [localError, setLocalError] = useState('')
  const displayError = error || localError

  const handleBlur = (e) => {
    if (validate) {
      const msg = validate(e.target.value)
      setLocalError(msg || '')
    }
    onBlur?.(e)
  }

  return (
    <div className="w-full">
      <input
        ref={ref}
        type={type}
        onBlur={handleBlur}
        className={`input-field ${displayError ? 'input-error' : ''} ${className}`}
        {...props}
      />
      {displayError && (
        <p className="mt-1 text-xs" style={{ color: 'var(--error)' }}>{displayError}</p>
      )}
    </div>
  )
})

export default Input
