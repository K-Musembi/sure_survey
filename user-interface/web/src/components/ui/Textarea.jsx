import { forwardRef } from 'react'

const Textarea = forwardRef(function Textarea({ error, className = '', ...props }, ref) {
  return (
    <div className="w-full">
      <textarea
        ref={ref}
        className={`input-field min-h-[80px] resize-y ${error ? 'input-error' : ''} ${className}`}
        {...props}
      />
      {error && (
        <p className="mt-1 text-xs" style={{ color: 'var(--error)' }}>{error}</p>
      )}
    </div>
  )
})

export default Textarea
