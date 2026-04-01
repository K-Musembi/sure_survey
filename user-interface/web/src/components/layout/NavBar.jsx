import { useState } from 'react'
import { Link } from 'react-router-dom'
import { HiOutlineMenuAlt3, HiOutlineX } from 'react-icons/hi'
import ThemeToggle from '../ui/ThemeToggle'
import useAuthStore from '../../stores/authStore'

const links = [
  { to: '/#features', label: 'Features' },
  { to: '/#pricing', label: 'Pricing' },
]

export default function NavBar() {
  const [open, setOpen] = useState(false)
  const isAuthenticated = useAuthStore((s) => s.isAuthenticated)

  return (
    <nav className="sticky top-0 z-50 border-b border-[var(--border)] bg-[var(--surface)]/80 backdrop-blur-md">
      <div className="mx-auto flex max-w-6xl items-center justify-between px-4 py-3 sm:px-6 lg:px-8">
        {/* Logo */}
        <Link to="/" className="flex items-center gap-2">
          <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-white font-bold text-sm">a</div>
          <span className="text-lg font-bold text-[var(--text)]">asq</span>
        </Link>

        {/* Desktop nav */}
        <div className="hidden md:flex items-center gap-6">
          {links.map(({ to, label }) => (
            <a key={to} href={to} className="text-sm font-medium text-[var(--text-muted)] hover:text-[var(--text)] transition-colors">
              {label}
            </a>
          ))}
        </div>

        {/* Actions */}
        <div className="hidden md:flex items-center gap-3">
          <ThemeToggle />
          {isAuthenticated ? (
            <Link to="/dashboard" className="btn-brand text-sm px-5 py-2">Dashboard</Link>
          ) : (
            <>
              <Link to="/login" className="btn-ghost text-sm px-4 py-2">Sign in</Link>
              <Link to="/signup" className="btn-brand text-sm px-5 py-2">Get Started</Link>
            </>
          )}
        </div>

        {/* Mobile toggle */}
        <div className="flex items-center gap-2 md:hidden">
          <ThemeToggle />
          <button onClick={() => setOpen(!open)} className="rounded-lg p-2 text-[var(--text-muted)] hover:text-[var(--text)]">
            {open ? <HiOutlineX className="h-5 w-5" /> : <HiOutlineMenuAlt3 className="h-5 w-5" />}
          </button>
        </div>
      </div>

      {/* Mobile menu */}
      {open && (
        <div className="border-t border-[var(--border)] bg-[var(--surface)] px-4 py-4 md:hidden">
          <div className="space-y-3">
            {links.map(({ to, label }) => (
              <a
                key={to}
                href={to}
                onClick={() => setOpen(false)}
                className="block text-sm font-medium text-[var(--text-muted)] hover:text-[var(--text)]"
              >
                {label}
              </a>
            ))}
            <div className="pt-3 border-t border-[var(--border)] space-y-2">
              {isAuthenticated ? (
                <Link to="/dashboard" onClick={() => setOpen(false)} className="btn-brand w-full text-sm py-2">Dashboard</Link>
              ) : (
                <>
                  <Link to="/login" onClick={() => setOpen(false)} className="btn-secondary w-full text-sm py-2">Sign in</Link>
                  <Link to="/signup" onClick={() => setOpen(false)} className="btn-brand w-full text-sm py-2">Get Started</Link>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </nav>
  )
}
