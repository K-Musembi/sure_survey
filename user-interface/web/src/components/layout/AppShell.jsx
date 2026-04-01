import Sidebar from './Sidebar'
import TopBar from './TopBar'

export default function AppShell({ children }) {
  return (
    <div className="flex min-h-screen" style={{ backgroundColor: 'var(--bg)' }}>
      <Sidebar />
      <div className="flex flex-1 flex-col min-w-0">
        <TopBar />
        <main className="flex-1 p-4 lg:p-8 overflow-x-hidden">
          {children}
        </main>
      </div>
    </div>
  )
}
