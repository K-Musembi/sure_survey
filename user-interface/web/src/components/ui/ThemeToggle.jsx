import { HiSun, HiMoon } from 'react-icons/hi'
import useThemeStore from '../../stores/themeStore'

export default function ThemeToggle({ className = '' }) {
  const { theme, toggle } = useThemeStore()

  return (
    <button
      onClick={toggle}
      className={`rounded-lg p-2 text-[var(--text-muted)] hover:bg-[var(--surface-hover)] hover:text-[var(--text)] transition-colors ${className}`}
      aria-label="Toggle theme"
    >
      {theme === 'dark' ? <HiSun className="h-5 w-5" /> : <HiMoon className="h-5 w-5" />}
    </button>
  )
}
