import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { billingAPI } from '../../services/apiServices'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import {
  HiOutlineSparkles, HiOutlineCurrencyDollar, HiOutlineChartBar,
  HiOutlineUserGroup, HiOutlineClipboardCheck, HiOutlineLightBulb,
  HiOutlineCheckCircle, HiOutlineDeviceMobile, HiOutlineShieldCheck,
} from 'react-icons/hi'

const features = [
  { icon: HiOutlineSparkles, title: 'AI Survey Builder', desc: 'Generate professional surveys in seconds with AI or use industry templates.' },
  { icon: HiOutlineCurrencyDollar, title: 'Built-in Rewards', desc: 'Incentivize respondents with airtime, loyalty points, or cash rewards.' },
  { icon: HiOutlineChartBar, title: 'Real-time Analytics', desc: 'Live dashboards, response streams, and exportable reports.' },
  { icon: HiOutlineUserGroup, title: 'Referral Engine', desc: 'Grow your reach with ODPC-compliant referral campaigns.' },
  { icon: HiOutlineLightBulb, title: 'Decision Intelligence', desc: 'AI-generated insight reports with actionable recommendations.' },
  { icon: HiOutlineDeviceMobile, title: 'Multi-channel', desc: 'Collect responses via Web, SMS, WhatsApp, and USSD.' },
]

const FALLBACK_PLANS = [
  { id: 'basic', name: 'Basic', price: 0, billingInterval: 'MONTHLY', features: JSON.stringify({ displayFeatures: ['5 surveys', '25 responses/survey', 'Web channel', 'Basic analytics'], isCustomPricing: false }) },
  { id: 'pro', name: 'Pro', price: 5999, billingInterval: 'MONTHLY', features: JSON.stringify({ displayFeatures: ['10 surveys', 'Web + SMS + WhatsApp', 'AI analysis', 'Referral engine', 'Rewards'], isCustomPricing: false }) },
  { id: 'enterprise', name: 'Enterprise', price: 19999, billingInterval: 'MONTHLY', features: JSON.stringify({ displayFeatures: ['Unlimited surveys', 'All channels', 'Business intelligence', 'Performance surveys', 'Webhooks + API', 'Dedicated support'], isCustomPricing: true }) },
]

export default function Home() {
  const [plans, setPlans] = useState(FALLBACK_PLANS)

  useEffect(() => {
    billingAPI.getAllPlans().then(r => { if (r.data?.length) setPlans(r.data) }).catch(() => {})
  }, [])

  const fmt = (v) => new Intl.NumberFormat('en-KE', { style: 'currency', currency: 'KES', maximumFractionDigits: 0 }).format(v || 0)

  return (
    <div>
      {/* Hero */}
      <section className="relative overflow-hidden py-20 sm:py-28">
        <div className="absolute inset-0 -z-10">
          <div className="absolute top-1/4 left-1/2 -translate-x-1/2 h-[600px] w-[600px] rounded-full bg-brand/5 blur-3xl" />
        </div>
        <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8 text-center">
          <Badge color="brand" className="mb-6">Now with AI-Powered Decision Intelligence</Badge>
          <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-[var(--text)] leading-tight">
            Surveys that drive<br />
            <span className="bg-gradient-to-r from-brand to-teal-400 bg-clip-text text-transparent">real decisions</span>
          </h1>
          <p className="mx-auto mt-6 max-w-2xl text-lg text-[var(--text-muted)]">
            AI-powered survey platform with built-in rewards, referral campaigns, and decision intelligence. Collect insights that matter.
          </p>

          <div className="mt-8 flex flex-wrap items-center justify-center gap-3">
            <Link to="/signup" className="btn-brand px-8 py-3 text-base">Start for free</Link>
            <Link to="/login" className="btn-secondary px-6 py-3 text-base">Sign in</Link>
          </div>

          <div className="mt-10 flex flex-wrap items-center justify-center gap-4">
            {['Real-time Analytics', 'Built-in Rewards', 'AI Insights', 'ODPC Compliant', 'Multi-channel'].map((pill) => (
              <span key={pill} className="rounded-full border border-[var(--border)] px-4 py-1.5 text-xs font-medium text-[var(--text-muted)]">
                {pill}
              </span>
            ))}
          </div>

          {/* Dashboard preview */}
          <div className="mx-auto mt-16 max-w-4xl">
            <div className="rounded-2xl border border-[var(--border)] bg-[var(--surface)] p-6 shadow-2xl">
              <div className="grid grid-cols-3 gap-4 mb-6">
                {[
                  { label: 'Total Responses', value: '12,847', delta: '+18%' },
                  { label: 'Completion Rate', value: '87.3%', delta: '+5%' },
                  { label: 'NPS Score', value: '72', delta: '+12' },
                ].map(({ label, value, delta }) => (
                  <div key={label} className="rounded-xl p-4" style={{ backgroundColor: 'var(--surface-hover)' }}>
                    <p className="text-xs font-medium text-[var(--text-muted)]">{label}</p>
                    <p className="mt-1 text-xl font-bold text-[var(--text)]">{value}</p>
                    <p className="text-xs text-[var(--success)]">{delta}</p>
                  </div>
                ))}
              </div>
              <div className="h-32 rounded-xl bg-gradient-to-r from-brand/10 to-brand/5 flex items-center justify-center">
                <p className="text-sm text-[var(--text-muted)]">Live analytics dashboard</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="py-20 sm:py-24">
        <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-[var(--text)]">Everything you need</h2>
            <p className="mt-3 text-lg text-[var(--text-muted)]">From creation to insights, all in one platform.</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {features.map((feat) => (
              <div key={feat.title} className="card-hover">
                <div className="mb-4 flex h-10 w-10 items-center justify-center rounded-lg bg-brand/10">
                  <feat.icon className="h-5 w-5 text-brand" />
                </div>
                <h3 className="font-semibold text-[var(--text)]">{feat.title}</h3>
                <p className="mt-2 text-sm text-[var(--text-muted)]">{feat.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Compliance */}
      <section className="py-16" style={{ backgroundColor: 'var(--surface)' }}>
        <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8">
          <div className="flex flex-col md:flex-row items-center gap-8">
            <div className="flex h-16 w-16 flex-shrink-0 items-center justify-center rounded-2xl bg-brand/10">
              <HiOutlineShieldCheck className="h-8 w-8 text-brand" />
            </div>
            <div>
              <h3 className="text-xl font-bold text-[var(--text)]">ODPC Compliant</h3>
              <p className="mt-2 text-[var(--text-muted)]">
                Built for Kenya's Data Protection Act 2019. Consent logging, data subject access and erasure rights, purpose limitation, and privacy notices built-in.
              </p>
            </div>
          </div>
        </div>
      </section>

      {/* Pricing */}
      <section id="pricing" className="py-20 sm:py-24">
        <div className="mx-auto max-w-6xl px-4 sm:px-6 lg:px-8">
          <div className="text-center mb-12">
            <h2 className="text-3xl font-bold text-[var(--text)]">Simple, transparent pricing</h2>
            <p className="mt-3 text-lg text-[var(--text-muted)]">Start free, upgrade as you grow.</p>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {plans.map((plan) => {
              let parsed = {}; try { parsed = JSON.parse(plan.features) } catch { /* ignore */ }
              const isCustom = parsed.isCustomPricing
              const features = parsed.displayFeatures || []
              const isPro = plan.name === 'Pro'
              return (
                <div key={plan.id} className={`card relative flex flex-col ${isPro ? 'border-2 border-brand' : ''}`}>
                  {isPro && (
                    <div className="absolute -top-3 left-1/2 -translate-x-1/2">
                      <Badge color="brand">Most Popular</Badge>
                    </div>
                  )}
                  <h3 className="text-lg font-bold text-[var(--text)]">{plan.name}</h3>
                  <div className="mt-2 mb-4">
                    {isCustom ? (
                      <span className="text-lg font-bold text-[var(--text-muted)]">Custom pricing</span>
                    ) : (
                      <><span className="text-3xl font-extrabold text-[var(--text)]">{fmt(plan.price)}</span><span className="text-sm text-[var(--text-muted)]">/mo</span></>
                    )}
                  </div>
                  <ul className="space-y-2.5 flex-1 mb-6">
                    {features.map((f, j) => (
                      <li key={j} className="flex items-start gap-2 text-sm text-[var(--text-muted)]">
                        <HiOutlineCheckCircle className="h-4 w-4 text-[var(--success)] flex-shrink-0 mt-0.5" />
                        {f}
                      </li>
                    ))}
                  </ul>
                  {isCustom ? (
                    <a href="mailto:sales@asq.co.ke"><Button variant="secondary" className="w-full">Contact Sales</Button></a>
                  ) : (
                    <Link to="/signup"><Button variant={isPro ? 'primary' : 'secondary'} className="w-full">Get Started</Button></Link>
                  )}
                </div>
              )
            })}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-20 bg-brand">
        <div className="mx-auto max-w-4xl px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl font-bold text-white">Ready to get started?</h2>
          <p className="mt-3 text-lg text-white/80">Create your free account and launch your first survey in minutes.</p>
          <div className="mt-8 flex flex-wrap justify-center gap-3">
            <Link to="/signup" className="inline-flex items-center rounded-full bg-white px-8 py-3 font-medium text-brand shadow-sm hover:shadow-md hover:scale-[1.02] transition-all">
              Start for free
            </Link>
            <Link to="/login" className="inline-flex items-center rounded-full border border-white/30 px-6 py-3 font-medium text-white hover:bg-white/10 transition-all">
              Sign in
            </Link>
          </div>
        </div>
      </section>
    </div>
  )
}
