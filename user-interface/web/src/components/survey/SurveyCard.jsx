import { Link } from 'react-router-dom'
import Badge from '../ui/Badge'
import Button from '../ui/Button'
import { HiOutlineEye, HiOutlinePlay, HiOutlinePencil, HiOutlineShare } from 'react-icons/hi'

const statusColors = {
  DRAFT: 'gray',
  ACTIVE: 'success',
  PAUSED: 'accent',
  CLOSED: 'error',
  COMPLETED: 'blue',
  EXPIRED: 'error',
}

export default function SurveyCard({ survey, onActivate, onCopyLink }) {
  return (
    <div className="card-hover">
      <div className="flex items-start justify-between mb-3">
        <div className="flex-1 min-w-0">
          <h3 className="text-base font-semibold text-[var(--text)] truncate" title={survey.name}>
            {survey.name}
          </h3>
          <div className="mt-1.5 flex flex-wrap items-center gap-1.5">
            <Badge color="blue">{survey.type}</Badge>
            <Badge color={statusColors[survey.status] || 'gray'}>{survey.status}</Badge>
          </div>
        </div>
        <button
          onClick={() => onCopyLink?.(survey)}
          className="ml-2 rounded-lg p-1.5 text-[var(--text-muted)] hover:bg-[var(--surface-hover)] hover:text-[var(--text)] transition-colors"
          title="Copy survey link"
        >
          <HiOutlineShare className="h-4 w-4" />
        </button>
      </div>

      <div className="space-y-2 text-sm">
        <div className="flex justify-between">
          <span className="text-[var(--text-muted)]">Responses</span>
          <span className="font-medium text-[var(--text)]">{survey.responseCount || 0}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-[var(--text-muted)]">Created</span>
          <span className="font-medium text-[var(--text)]">{new Date(survey.createdAt).toLocaleDateString()}</span>
        </div>
        {survey.rewardAmount > 0 && (
          <div className="flex justify-between">
            <span className="text-[var(--text-muted)]">Reward</span>
            <span className="font-medium text-[var(--success)]">KES {survey.rewardAmount}</span>
          </div>
        )}
      </div>

      <div className="mt-4 flex gap-2 border-t border-[var(--border)] pt-4">
        <Button variant="secondary" size="sm" className="flex-1" as={Link} to={`/dashboard/survey/${survey.id}`}>
          <HiOutlineEye className="h-4 w-4" /> Manage
        </Button>
        {survey.status === 'DRAFT' && (
          <>
            <Button variant="secondary" size="sm" as={Link} to={`/dashboard/new?edit=${survey.id}`}>
              <HiOutlinePencil className="h-4 w-4" />
            </Button>
            <Button size="sm" onClick={() => onActivate?.(survey)}>
              <HiOutlinePlay className="h-4 w-4" /> Activate
            </Button>
          </>
        )}
      </div>
    </div>
  )
}
