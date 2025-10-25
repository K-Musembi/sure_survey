import NavBar from '../components/NavBar'
import Hero from '../components/Hero'
import Footer from '../components/Footer'

const Home = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <NavBar />

      <main className="flex-grow">
        <Hero />

        <section className="py-16 bg-white">
          <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8">
            <h2 className="text-3xl font-semibold text-gray-900 text-center">Why choose Sure Survey?</h2>
            <p className="mt-4 text-gray-600 text-center max-w-2xl mx-auto">
              Fast survey creation, rewarding respondents and real-time analytics to help you make decisions faster.
            </p>

            <div className="mt-12 grid grid-cols-1 md:grid-cols-3 gap-8">
              <div className="card">
                <h3 className="text-lg font-semibold mb-2">Easy to use builder</h3>
                <p className="text-sm text-gray-600">Drag, drop and configure survey questions quickly.</p>
              </div>
              <div className="card">
                <h3 className="text-lg font-semibold mb-2">Reward participants</h3>
                <p className="text-sm text-gray-600">Attach rewards to surveys and pay seamlessly via Paystack.</p>
              </div>
              <div className="card">
                <h3 className="text-lg font-semibold mb-2">Real-time insights</h3>
                <p className="text-sm text-gray-600">Live dashboards powered by SSE for immediate feedback.</p>
              </div>
            </div>
          </div>
        </section>
      </main>

      <Footer />
    </div>
  )
}

export default Home
