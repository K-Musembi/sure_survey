import NavBar from './NavBar'
import Footer from './Footer'

export default function PublicLayout({ children }) {
  return (
    <div className="flex min-h-screen flex-col" style={{ backgroundColor: 'var(--bg)' }}>
      <NavBar />
      <main className="flex-1">{children}</main>
      <Footer />
    </div>
  )
}
