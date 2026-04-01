import { Link } from 'react-router-dom'
import { Button } from 'flowbite-react'
import { HiChartBar, HiCurrencyDollar, HiLightningBolt, HiSparkles, HiShieldCheck } from 'react-icons/hi'

const Hero = () => {
  return (
    <section className="relative bg-gradient-to-br from-primary-50 via-white to-purple-50 py-20 lg:py-32 overflow-hidden">
      {/* Decorative blobs */}
      <div className="absolute top-0 right-0 w-96 h-96 bg-primary-200 rounded-full opacity-20 blur-3xl -translate-y-1/2 translate-x-1/3" />
      <div className="absolute bottom-0 left-0 w-64 h-64 bg-purple-200 rounded-full opacity-20 blur-3xl translate-y-1/2 -translate-x-1/3" />

      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="lg:grid lg:grid-cols-12 lg:gap-12 lg:items-center">

          {/* Content */}
          <div className="lg:col-span-6">
            <div className="text-center lg:text-left">

              {/* Badge */}
              <div className="inline-flex items-center px-4 py-1.5 rounded-full bg-purple-100 text-purple-800 text-sm font-medium mb-6 border border-purple-200">
                <HiSparkles className="w-4 h-4 mr-2 text-purple-600" />
                Now with AI-Powered Decision Intelligence
              </div>

              {/* Headline */}
              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-extrabold text-gray-900 leading-tight tracking-tight">
                Surveys that drive{' '}
                <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary-500 to-purple-600">
                  real decisions
                </span>
              </h1>

              {/* Subheadline */}
              <p className="mt-6 text-lg text-gray-600 leading-relaxed max-w-xl mx-auto lg:mx-0">
                Collect quality responses across Web, SMS, USSD, and Mobile. Reward participants instantly.
                Turn data into boardroom-ready intelligence — built for African enterprises.
              </p>

              {/* Feature pills */}
              <div className="mt-8 flex flex-wrap gap-3 justify-center lg:justify-start">
                {[
                  { label: 'Real-time Analytics', icon: 'chart' },
                  { label: 'Built-in Rewards', icon: 'currency' },
                  { label: 'AI Insights', icon: 'sparkles' },
                  { label: 'ODPC Compliant', icon: 'shield' },
                  { label: 'Multi-channel', icon: 'lightning' },
                ].map(({ label, icon }) => (
                  <div key={label} className="flex items-center gap-1.5 bg-white border border-gray-200 rounded-full px-3 py-1 text-sm text-gray-700 shadow-sm">
                    {icon === 'chart' && <HiChartBar className="w-4 h-4 text-primary-500" />}
                    {icon === 'currency' && <HiCurrencyDollar className="w-4 h-4 text-primary-500" />}
                    {icon === 'sparkles' && <HiSparkles className="w-4 h-4 text-primary-500" />}
                    {icon === 'shield' && <HiShieldCheck className="w-4 h-4 text-primary-500" />}
                    {icon === 'lightning' && <HiLightningBolt className="w-4 h-4 text-primary-500" />}
                    {label}
                  </div>
                ))}
              </div>

              {/* CTA Buttons */}
              <div className="mt-10 flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
                <Button
                  as={Link}
                  to="/signup"
                  size="xl"
                  color="purple"
                  className="font-semibold px-8 shadow-lg shadow-purple-200"
                >
                  Start for free
                </Button>
                <Button
                  as={Link}
                  to="/login"
                  size="xl"
                  color="light"
                  className="font-medium border border-gray-300"
                >
                  Sign in
                </Button>
              </div>

              {/* Trust indicators */}
              <div className="mt-10 text-center lg:text-left">
                <p className="text-xs text-gray-400 mb-3 uppercase tracking-wide">Trusted by organisations across</p>
                <div className="flex flex-wrap gap-4 justify-center lg:justify-start">
                  {['SACCOs', 'Insurance', 'NGOs', 'Retail', 'Healthcare', 'Financial Services'].map(sector => (
                    <span key={sector} className="text-sm font-semibold text-gray-400">{sector}</span>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* Visual */}
          <div className="mt-16 lg:mt-0 lg:col-span-6">
            <div className="relative max-w-md mx-auto lg:max-w-none">

              {/* Main dashboard card */}
              <div className="bg-white rounded-2xl shadow-2xl p-6 border border-gray-100">
                <div className="flex items-center justify-between mb-4">
                  <div>
                    <p className="text-xs text-gray-500 uppercase tracking-wide">Employee Satisfaction Survey</p>
                    <p className="font-bold text-gray-900 text-lg mt-0.5">Response Overview</p>
                  </div>
                  <span className="bg-green-100 text-green-700 text-xs font-semibold px-2.5 py-1 rounded-full">Live</span>
                </div>

                <div className="grid grid-cols-3 gap-3 mb-5">
                  {[
                    { label: 'Responses', value: '1,247', delta: '+18%', color: 'text-primary-600' },
                    { label: 'Completion', value: '87%', delta: '+5%', color: 'text-green-600' },
                    { label: 'NPS Score', value: '72', delta: '+12', color: 'text-purple-600' },
                  ].map(({ label, value, delta, color }) => (
                    <div key={label} className="bg-gray-50 rounded-xl p-3 text-center">
                      <p className="text-xs text-gray-500 mb-1">{label}</p>
                      <p className={`text-xl font-extrabold ${color}`}>{value}</p>
                      <p className="text-xs text-green-600 font-medium">{delta}</p>
                    </div>
                  ))}
                </div>

                {/* Progress bar */}
                <div>
                  <div className="flex justify-between text-xs text-gray-500 mb-1">
                    <span>Progress to target (1,500)</span>
                    <span>83%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div className="bg-gradient-to-r from-primary-400 to-primary-600 h-2 rounded-full" style={{ width: '83%' }} />
                  </div>
                </div>

                {/* AI insight teaser */}
                <div className="mt-4 bg-purple-50 rounded-xl p-3 border border-purple-100">
                  <div className="flex items-start gap-2">
                    <HiSparkles className="w-4 h-4 text-purple-600 mt-0.5 shrink-0" />
                    <div>
                      <p className="text-xs font-semibold text-purple-800">AI Insight</p>
                      <p className="text-xs text-purple-700 mt-0.5 leading-relaxed">
                        63% of detractors cite workload balance. Consider scheduling a team review in Q2.
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              {/* Floating rewards card */}
              <div className="absolute -bottom-4 -left-6 bg-white rounded-xl shadow-lg p-3 border border-gray-100 flex items-center gap-3">
                <div className="w-9 h-9 bg-green-100 rounded-full flex items-center justify-center">
                  <HiCurrencyDollar className="w-5 h-5 text-green-600" />
                </div>
                <div>
                  <p className="text-xs text-gray-500">Rewards paid out</p>
                  <p className="font-bold text-gray-900 text-sm">KES 124,500</p>
                </div>
              </div>

              {/* Floating channel badge */}
              <div className="absolute -top-3 -right-4 bg-white rounded-xl shadow-lg px-3 py-2 border border-gray-100">
                <div className="flex items-center gap-1.5">
                  <HiLightningBolt className="w-3.5 h-3.5 text-primary-500" />
                  <p className="text-xs font-semibold text-gray-700">Web · SMS · USSD · Mobile</p>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>
    </section>
  )
}

export default Hero
