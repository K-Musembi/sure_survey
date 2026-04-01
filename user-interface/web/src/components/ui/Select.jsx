import { forwardRef } from 'react'

const Select = forwardRef(function Select({ error, className = '', children, ...props }, ref) {
  return (
    <div className="w-full">
      <select
        ref={ref}
        className={`input-field pr-10 appearance-none bg-[length:16px] bg-[right_12px_center] bg-no-repeat ${error ? 'input-error' : ''} ${className}`}
        style={{
          backgroundImage: `url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' fill='none' viewBox='0 0 20 20'%3E%3Cpath stroke='%2394A3B8' stroke-linecap='round' stroke-linejoin='round' stroke-width='1.5' d='m6 8 4 4 4-4'/%3E%3C/svg%3E")`,
        }}
        {...props}
      >
        {children}
      </select>
      {error && (
        <p className="mt-1 text-xs" style={{ color: 'var(--error)' }}>{error}</p>
      )}
    </div>
  )
})

export default Select
