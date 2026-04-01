import { useLocation } from 'react-router-dom'
import ThemeToggle from '../ui/ThemeToggle'
import useAuthStore from '../../stores/authStore'

const breadcrumbMap = {
  '/dashboard': 'Surveys',
  '/dashboard/analytics': 'Analytics',
  '/dashboard/intelligence': 'Intelligence',
  '/dashboard/referrals': 'Referrals',
  '/dashboard/billing': 'Billing',
  '/dashboard/profile': 'Profile',
  '/dashboard/settings': 'Settings',
  '/dashboard/new': 'New Survey',
}

export default function TopBar() {
  const location = useLocation()
  const user = useAuthStore((s) => s.user)

  const title = breadcrumbMap[location.pathname] || 'Dashboard'

  return (
    <header className="sticky top-0 z-30 flex items-center justify-between border-b border-[var(--border)] bg-[var(--surface)]/80 backdrop-blur-md px-6 py-3 lg:px-8">
      <div className="flex items-center gap-4">
        {/* Spacer for mobile menu button */}
        <div className="w-8 lg:hidden" />
        <h1 className="text-lg font-semibold text-[var(--text)]">{title}</h1>
      </div>

      <div className="flex items-center gap-3">
        <ThemeToggle />
        <div className="flex items-center gap-3 rounded-full border border-[var(--border)] py-1.5 pl-3 pr-1.5">
          <span className="text-sm font-medium text-[var(--text)] hidden sm:block">
            {user?.name || 'User'}
          </span>
          <div className="flex h-7 w-7 items-center justify-center rounded-full bg-brand text-xs font-bold text-white">
            {user?.name?.charAt(0)?.toUpperCase() || 'U'}
          </div>
        </div>
      </div>
    </header>
  )
}
