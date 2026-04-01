import { HiOutlineTrash, HiOutlinePlus } from 'react-icons/hi'

export default function OptionsEditor({ options, onChange }) {
  const update = (idx, value) => {
    const next = [...options]
    next[idx] = value
    onChange(next)
  }

  const remove = (idx) => onChange(options.filter((_, i) => i !== idx))
  const add = () => onChange([...options, ''])

  return (
    <div className="space-y-2 border-l-2 border-brand/20 pl-4">
      {options.map((opt, i) => (
        <div key={i} className="flex items-center gap-2">
          <input
            className="input-field flex-1 text-sm"
            value={opt}
            onChange={(e) => update(i, e.target.value)}
            placeholder={`Option ${i + 1}`}
          />
          <button onClick={() => remove(i)} className="rounded-lg p-1 text-[var(--text-muted)] hover:text-[var(--error)] transition-colors">
            <HiOutlineTrash className="h-4 w-4" />
          </button>
        </div>
      ))}
      <button onClick={add} className="flex items-center gap-1.5 text-sm font-medium text-brand hover:text-brand-light transition-colors">
        <HiOutlinePlus className="h-4 w-4" /> Add Option
      </button>
    </div>
  )
}
