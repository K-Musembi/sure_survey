import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import {
  HiOutlineClipboardList, HiOutlineChartBar, HiOutlineLightBulb,
  HiOutlineUserGroup, HiOutlineCreditCard, HiOutlineUser,
  HiOutlineCog, HiOutlineLogout, HiOutlineMenuAlt2, HiOutlineX,
  HiOutlineLockClosed, HiOutlineFire, HiOutlineLightningBolt,
} from 'react-icons/hi'
import useAuthStore from '../../stores/authStore'
import { useLogout, useUsage } from '../../hooks/useApi'
import UpgradeModal from '../UpgradeModal'

const navItems = [
  { to: '/dashboard', label: 'Surveys', icon: HiOutlineClipboardList },
  { to: '/dashboard/analytics', label: 'Analytics', icon: HiOutlineChartBar },
  { to: '/dashboard/intelligence', label: 'Intelligence', icon: HiOutlineLightBulb, requiredFeature: 'aiAnalysis', minPlan: 'Enterprise' },
  { to: '/dashboard/competitions', label: 'Competitions', icon: HiOutlineFire, requiredFeature: 'performanceSurvey', minPlan: 'Pro' },
  { to: '/dashboard/referrals', label: 'Referrals', icon: HiOutlineUserGroup, requiredFeature: 'referralEngine', minPlan: 'Pro' },
  { to: '/dashboard/integrations', label: 'Integrations', icon: HiOutlineLightningBolt, requiredFeature: 'webhooks', minPlan: 'Pro' },
  { to: '/dashboard/billing', label: 'Billing', icon: HiOutlineCreditCard },
  { to: '/dashboard/profile', label: 'Profile', icon: HiOutlineUser },
  { to: '/dashboard/settings', label: 'Settings', icon: HiOutlineCog },
]

export default function Sidebar() {
  const location = useLocation()
  const [collapsed, setCollapsed] = useState(false)
  const [mobileOpen, setMobileOpen] = useState(false)
  const [showUpgrade, setShowUpgrade] = useState(false)
  const [upgradePlan, setUpgradePlan] = useState(null)
  const { logout } = useAuthStore()
  const logoutMutation = useLogout()
  const { data: usage, isLoading: usageLoading } = useUsage()

  const handleLogout = async () => {
    try {
      await logoutMutation.mutateAsync()
    } catch {
      logout()
    }
  }

  const isActive = (path) => {
    if (path === '/dashboard') return location.pathname === '/dashboard'
    return location.pathname.startsWith(path)
  }

  const isFeatureLocked = (item) => {
    if (!item.requiredFeature) return false
    if (!usage) return usageLoading // treat as locked while loading to prevent premature navigation
    return !usage[item.requiredFeature]
  }

  const handleNavClick = (e, item) => {
    if (isFeatureLocked(item)) {
      e.preventDefault()
      if (!usageLoading) {
        setUpgradePlan(item.minPlan || 'Pro')
        setShowUpgrade(true)
      }
      setMobileOpen(false)
    } else {
      setMobileOpen(false)
    }
  }

  const nav = (
    <div className="flex h-full flex-col">
      {/* Logo */}
      <div className="flex items-center gap-3 px-4 py-5 border-b border-[var(--border)]">
        <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-white font-bold text-sm">a</div>
        {!collapsed && <span className="text-lg font-bold text-[var(--text)]">asq</span>}
      </div>

      {/* Nav items */}
      <nav className="flex-1 space-y-1 px-3 py-4">
        {navItems.map((item) => {
          const locked = isFeatureLocked(item)
          return (
            <Link
              key={item.to}
              to={item.to}
              onClick={(e) => handleNavClick(e, item)}
              className={`flex items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium transition-colors
                ${locked
                  ? 'text-[var(--text-muted)] opacity-60 hover:opacity-80'
                  : isActive(item.to)
                  ? 'bg-brand/10 text-brand'
                  : 'text-[var(--text-muted)] hover:bg-[var(--surface-hover)] hover:text-[var(--text)]'
                }`}
            >
              <span className="relative flex-shrink-0">
                <item.icon className="h-5 w-5" />
                {collapsed && locked && !usageLoading && (
                  <HiOutlineLockClosed className="absolute -top-1 -right-1 h-3 w-3 text-[var(--text-muted)]" />
                )}
              </span>
              {!collapsed && (
                <span className="flex-1 flex items-center justify-between">
                  {item.label}
                  {locked && !usageLoading && <HiOutlineLockClosed className="h-3.5 w-3.5 text-[var(--text-muted)]" />}
                </span>
              )}
            </Link>
          )
        })}
      </nav>

      {/* Sign out */}
      <div className="border-t border-[var(--border)] px-3 py-4">
        <button
          onClick={handleLogout}
          className="flex w-full items-center gap-3 rounded-lg px-3 py-2.5 text-sm font-medium text-[var(--text-muted)] hover:bg-[var(--surface-hover)] hover:text-[var(--error)] transition-colors"
        >
          <HiOutlineLogout className="h-5 w-5 flex-shrink-0" />
          {!collapsed && <span>Sign Out</span>}
        </button>
      </div>
    </div>
  )

  return (
    <>
      {/* Mobile toggle button */}
      <button
        onClick={() => setMobileOpen(true)}
        className="fixed left-4 top-4 z-40 rounded-lg p-2 bg-[var(--surface)] border border-[var(--border)] text-[var(--text-muted)] hover:text-[var(--text)] lg:hidden"
      >
        <HiOutlineMenuAlt2 className="h-5 w-5" />
      </button>

      {/* Mobile overlay */}
      {mobileOpen && (
        <div className="fixed inset-0 z-40 lg:hidden">
          <div className="absolute inset-0 bg-black/50" onClick={() => setMobileOpen(false)} />
          <div className="absolute left-0 top-0 h-full w-64 bg-[var(--surface)] border-r border-[var(--border)] shadow-xl">
            <button
              onClick={() => setMobileOpen(false)}
              className="absolute right-3 top-5 rounded-lg p-1 text-[var(--text-muted)] hover:text-[var(--text)]"
            >
              <HiOutlineX className="h-5 w-5" />
            </button>
            {nav}
          </div>
        </div>
      )}

      {/* Desktop sidebar */}
      <aside
        className={`hidden lg:flex flex-col flex-shrink-0 h-screen sticky top-0 bg-[var(--surface)] border-r border-[var(--border)] transition-all duration-200
          ${collapsed ? 'w-[68px]' : 'w-64'}`}
      >
        {nav}
        <button
          onClick={() => setCollapsed(!collapsed)}
          className="absolute -right-3 top-20 hidden lg:flex h-6 w-6 items-center justify-center rounded-full border border-[var(--border)] bg-[var(--surface)] text-[var(--text-muted)] hover:text-[var(--text)] shadow-sm"
        >
          <svg className={`h-3 w-3 transition-transform ${collapsed ? 'rotate-180' : ''}`} fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
          </svg>
        </button>
      </aside>

      {/* Upgrade modal */}
      <UpgradeModal
        open={showUpgrade}
        onClose={() => setShowUpgrade(false)}
        currentPlan={usage?.planName || 'Free'}
        highlightPlan={upgradePlan}
        reason={`This feature requires the ${upgradePlan} plan or higher.`}
      />
    </>
  )
}
