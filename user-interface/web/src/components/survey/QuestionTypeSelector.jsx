const types = [
  { value: 'FREE_TEXT', label: 'Free Text' },
  { value: 'MULTIPLE_CHOICE_SINGLE', label: 'Single Choice' },
  { value: 'MULTIPLE_CHOICE_MULTI', label: 'Multiple Choice' },
  { value: 'RATING_LINEAR', label: 'Linear Scale (1-10)' },
  { value: 'RATING_STAR', label: 'Star Rating' },
  { value: 'NPS_SCALE', label: 'NPS (0-10)' },
]

export default function QuestionTypeSelector({ value, onChange }) {
  return (
    <select
      className="input-field w-auto pr-8 text-sm"
      value={value}
      onChange={(e) => onChange(e.target.value)}
    >
      {types.map((t) => (
        <option key={t.value} value={t.value}>{t.label}</option>
      ))}
    </select>
  )
}
