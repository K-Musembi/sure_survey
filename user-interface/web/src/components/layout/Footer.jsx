import { Link } from 'react-router-dom'

const columns = [
  {
    title: 'Product',
    links: [
      { label: 'Features', href: '/#features' },
      { label: 'Pricing', href: '/#pricing' },
      { label: 'Templates', href: '/#features' },
    ],
  },
  {
    title: 'Company',
    links: [
      { label: 'About', href: '#' },
      { label: 'Privacy Policy', href: 'https://asq.co.ke/privacy', external: true },
      { label: 'Terms of Service', href: '#' },
    ],
  },
  {
    title: 'Support',
    links: [
      { label: 'Help Center', href: '#' },
      { label: 'Contact Us', href: '#' },
      { label: 'Status', href: '#' },
    ],
  },
]

export default function Footer() {
  return (
    <footer className="border-t border-[var(--border)]" style={{ backgroundColor: 'var(--surface)' }}>
      <div className="mx-auto max-w-6xl px-4 py-12 sm:px-6 lg:px-8">
        <div className="grid grid-cols-2 gap-8 md:grid-cols-4">
          {/* Brand */}
          <div className="col-span-2 md:col-span-1">
            <div className="flex items-center gap-2 mb-4">
              <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-brand text-white font-bold text-sm">a</div>
              <span className="text-lg font-bold text-[var(--text)]">asq</span>
            </div>
            <p className="text-sm text-[var(--text-muted)] max-w-xs">
              AI-powered surveys with rewards, referrals, and decision intelligence.
            </p>
          </div>

          {columns.map((col) => (
            <div key={col.title}>
              <h4 className="text-sm font-semibold text-[var(--text)] mb-3">{col.title}</h4>
              <ul className="space-y-2">
                {col.links.map((link) => (
                  <li key={link.label}>
                    {link.external ? (
                      <a href={link.href} target="_blank" rel="noopener noreferrer" className="text-sm text-[var(--text-muted)] hover:text-[var(--text)] transition-colors">
                        {link.label}
                      </a>
                    ) : (
                      <a href={link.href} className="text-sm text-[var(--text-muted)] hover:text-[var(--text)] transition-colors">
                        {link.label}
                      </a>
                    )}
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="mt-10 border-t border-[var(--border)] pt-6 text-center text-sm text-[var(--text-muted)]">
          &copy; {new Date().getFullYear()} asq. All rights reserved.
        </div>
      </div>
    </footer>
  )
}
