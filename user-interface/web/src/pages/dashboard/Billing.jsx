import { useState, useEffect, useRef } from 'react'
import { useSearchParams } from 'react-router-dom'
import { billingAPI, paymentAPI } from '../../services/apiServices'
import Button from '../../components/ui/Button'
import Badge from '../../components/ui/Badge'
import { SkeletonCard } from '../../components/ui/Skeleton'
import EmptyState from '../../components/ui/EmptyState'
import PaymentModal from '../../components/PaymentModal'
import useToast from '../../hooks/useToast'
import { useUsage } from '../../hooks/useApi'
import { HiOutlineCurrencyDollar, HiOutlineCheckCircle, HiOutlineCreditCard, HiOutlineClock } from 'react-icons/hi'

export default function Billing() {
  const toast = useToast()
  const [searchParams, setSearchParams] = useSearchParams()
  const [balance, setBalance] = useState(0)
  const [transactions, setTransactions] = useState([])
  const [subscription, setSubscription] = useState(null)
  const [invoices, setInvoices] = useState([])
  const [plans, setPlans] = useState([])
  const [loading, setLoading] = useState(true)
  const [showTopUp, setShowTopUp] = useState(false)
  const [upgrading, setUpgrading] = useState(false)
  const [activeTab, setActiveTab] = useState('transactions')
  const verifiedRef = useRef(false)
  const { data: usage } = useUsage()

  const fetchData = async () => {
    setLoading(true)
    const [b, t, s, i, p] = await Promise.allSettled([
      billingAPI.getWalletBalance(), billingAPI.getWalletTransactions(),
      billingAPI.getSubscription(), billingAPI.getInvoices(), billingAPI.getAllPlans(),
    ])
    if (b.status === 'fulfilled') setBalance(b.value.data)
    if (t.status === 'fulfilled') setTransactions(t.value.data)
    if (s.status === 'fulfilled') setSubscription(s.value.data)
    if (i.status === 'fulfilled') setInvoices(i.value.data)
    if (p.status === 'fulfilled') setPlans(p.value.data)
    setLoading(false)
  }

  useEffect(() => { fetchData() }, [])

  // Handle Paystack payment callback redirect
  useEffect(() => {
    const ref = searchParams.get('payment_ref') || searchParams.get('reference') || searchParams.get('trxref')
    if (!ref || verifiedRef.current) return
    verifiedRef.current = true

    paymentAPI.verifyPayment(ref)
      .then((res) => {
        const status = res.data?.data?.status || res.data?.status
        if (status === 'success') {
          toast.success('Payment successful! Your wallet has been topped up.')
        } else {
          toast.error('Payment was not completed. Status: ' + (status || 'unknown'))
        }
      })
      .catch(() => toast.error('Could not verify payment status.'))
      .finally(() => {
        setSearchParams({}, { replace: true }) // clean URL
        fetchData() // refresh balances
      })
  }, [searchParams])

  const handleUpgrade = async (planId) => {
    setUpgrading(true)
    try { await billingAPI.createSubscription({ planId }); await fetchData(); toast.success('Plan updated!') }
    catch (err) { toast.error(err.response?.data?.message || 'Upgrade failed.') }
    finally { setUpgrading(false) }
  }

  if (loading) return <div className="space-y-4">{Array.from({ length: 3 }).map((_, i) => <SkeletonCard key={i} />)}</div>

  const fmt = (v) => new Intl.NumberFormat('en-KE', { style: 'currency', currency: 'KES', maximumFractionDigits: 0 }).format(v || 0)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-[var(--text)]">Billing</h1>
        <p className="text-sm text-[var(--text-muted)] mt-0.5">Manage your plan and wallet</p>
      </div>

      {/* Top cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div className="card bg-gradient-to-br from-brand/5 to-transparent">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm font-medium text-[var(--text-muted)]">Wallet Balance</p>
              <p className="mt-1 text-3xl font-bold text-[var(--text)]">{fmt(balance)}</p>
              <p className="text-xs text-[var(--text-muted)] mt-1">For rewards & SMS distribution</p>
            </div>
            <div className="rounded-lg p-2.5 bg-brand/10"><HiOutlineCurrencyDollar className="h-5 w-5 text-brand" /></div>
          </div>
          <Button className="mt-4" onClick={() => setShowTopUp(true)}>Top Up Wallet</Button>
        </div>

        <div className="card">
          <div className="flex items-start justify-between">
            <div>
              <p className="text-sm font-medium text-[var(--text-muted)]">Current Plan</p>
              <p className="mt-1 text-xl font-bold text-[var(--text)]">{usage?.planName || subscription?.plan?.name || 'Free'}</p>
            </div>
            <div className="rounded-lg p-2.5 bg-[var(--success)]/10"><HiOutlineCheckCircle className="h-5 w-5 text-[var(--success)]" /></div>
          </div>
          <div className="mt-3 space-y-1.5 text-sm">
            <div className="flex justify-between"><span className="text-[var(--text-muted)]">Status</span><Badge color={subscription?.status === 'ACTIVE' || !subscription ? 'success' : 'accent'}>{subscription?.status || 'ACTIVE'}</Badge></div>
            {subscription?.currentPeriodEnd && (
              <div className="flex justify-between"><span className="text-[var(--text-muted)]">Renews</span><span className="text-[var(--text)]">{new Date(subscription.currentPeriodEnd).toLocaleDateString()}</span></div>
            )}
            {usage && usage.maxSurveys > 0 && (
              <div className="flex justify-between items-center">
                <span className="text-[var(--text-muted)]">Surveys</span>
                <span className="text-[var(--text)] font-medium">{usage.currentSurveys} / {usage.maxSurveys}</span>
              </div>
            )}
            {usage && usage.maxSurveys === -1 && (
              <div className="flex justify-between items-center">
                <span className="text-[var(--text-muted)]">Surveys</span>
                <span className="text-[var(--text)] font-medium">Unlimited</span>
              </div>
            )}
            {usage && (
              <div className="flex justify-between items-center">
                <span className="text-[var(--text-muted)]">Channels</span>
                <span className="text-[var(--text)] font-medium">{usage.allowedChannels?.join(', ')}</span>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Plans */}
      <div className="card">
        <h3 className="text-lg font-semibold text-[var(--text)] mb-4">Available Plans</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {plans.map((plan) => {
            const currentPlanName = usage?.planName || subscription?.plan?.name || 'Free'
            const tierOrder = ['Free', 'Basic', 'Pro', 'Enterprise']
            const isCurrent = currentPlanName.toLowerCase() === plan.name.toLowerCase()
            const canUpgrade = tierOrder.indexOf(plan.name) > tierOrder.indexOf(currentPlanName.charAt(0).toUpperCase() + currentPlanName.slice(1).toLowerCase())
            let parsed = {}; try { parsed = JSON.parse(plan.features) } catch { /* ignore */ }
            const isCustom = parsed.isCustomPricing
            const features = parsed.displayFeatures || []
            return (
              <div key={plan.id} className={`rounded-xl border-2 p-5 flex flex-col ${isCurrent ? 'border-brand bg-brand/5' : 'border-[var(--border)]'}`}>
                <div className="flex justify-between items-start">
                  <h4 className="font-bold text-[var(--text)]">{plan.name}</h4>
                  {isCurrent && <Badge color="brand">Current</Badge>}
                </div>
                <div className="mt-2 mb-4">
                  {isCustom ? <span className="text-lg font-bold text-[var(--text-muted)]">Custom pricing</span> : (
                    <><span className="text-2xl font-extrabold text-[var(--text)]">{fmt(plan.price)}</span><span className="text-sm text-[var(--text-muted)]">/{plan.billingInterval === 'YEARLY' ? 'yr' : 'mo'}</span></>
                  )}
                </div>
                <ul className="space-y-2 flex-1 text-sm text-[var(--text-muted)] mb-4">
                  {features.map((f, i) => (
                    <li key={i} className="flex items-start gap-2"><HiOutlineCheckCircle className="h-4 w-4 text-[var(--success)] flex-shrink-0 mt-0.5" />{f}</li>
                  ))}
                </ul>
                {isCustom ? (
                  <a href="mailto:sales@suresurvey.co"><Button variant="secondary" className="w-full">Contact Sales</Button></a>
                ) : isCurrent ? (
                  <Button className="w-full" variant="secondary" disabled>Current</Button>
                ) : canUpgrade ? (
                  <Button className="w-full" variant="primary" disabled={upgrading} onClick={() => handleUpgrade(plan.id)}>
                    {upgrading ? 'Updating...' : 'Upgrade'}
                  </Button>
                ) : (
                  <Button className="w-full" variant="secondary" disabled>Included</Button>
                )}
              </div>
            )
          })}
        </div>
      </div>

      {/* History tabs */}
      <div>
        <div className="flex gap-1 border-b border-[var(--border)] mb-4">
          {[{ key: 'transactions', label: 'Transactions', icon: HiOutlineClock }, { key: 'invoices', label: 'Invoices', icon: HiOutlineCreditCard }].map((tab) => (
            <button key={tab.key} onClick={() => setActiveTab(tab.key)}
              className={`flex items-center gap-2 px-4 py-3 text-sm font-medium border-b-2 transition-colors ${activeTab === tab.key ? 'border-brand text-brand' : 'border-transparent text-[var(--text-muted)] hover:text-[var(--text)]'}`}>
              <tab.icon className="h-4 w-4" />{tab.label}
            </button>
          ))}
        </div>

        {activeTab === 'transactions' && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead><tr className="border-b border-[var(--border)]">
                <th className="py-3 px-4 font-medium text-[var(--text-muted)]">Date</th>
                <th className="py-3 px-4 font-medium text-[var(--text-muted)]">Description</th>
                <th className="py-3 px-4 font-medium text-[var(--text-muted)]">Type</th>
                <th className="py-3 px-4 font-medium text-[var(--text-muted)]">Amount</th>
              </tr></thead>
              <tbody>
                {transactions?.length ? transactions.map((tx) => (
                  <tr key={tx.id} className="border-b border-[var(--border)]">
                    <td className="py-3 px-4 text-[var(--text)]">{new Date(tx.createdAt).toLocaleDateString()}</td>
                    <td className="py-3 px-4 text-[var(--text)]">{tx.description}</td>
                    <td className="py-3 px-4"><Badge color={tx.type === 'CREDIT' ? 'success' : 'error'}>{tx.type}</Badge></td>
                    <td className={`py-3 px-4 font-medium ${tx.type === 'CREDIT' ? 'text-[var(--success)]' : 'text-[var(--error)]'}`}>{tx.type === 'CREDIT' ? '+' : '-'}{fmt(tx.amount)}</td>
                  </tr>
                )) : <tr><td colSpan={4} className="py-8 text-center text-[var(--text-muted)]">No transactions yet</td></tr>}
              </tbody>
            </table>
          </div>
        )}

        {activeTab === 'invoices' && (
          <div className="overflow-x-auto">
            <table className="w-full text-sm text-left">
              <thead><tr className="border-b border-[var(--border)]">
                <th className="py-3 px-4 font-medium text-[var(--text-muted)]">Date</th>
                <th className="py-3 px-4 font-medium text-[var(--text-muted)]">Amount</th>
                <th className="py-3 px-4 font-medium text-[var(--text-muted)]">Status</th>
              </tr></thead>
              <tbody>
                {invoices?.length ? invoices.map((inv) => (
                  <tr key={inv.id} className="border-b border-[var(--border)]">
                    <td className="py-3 px-4 text-[var(--text)]">{new Date(inv.createdAt).toLocaleDateString()}</td>
                    <td className="py-3 px-4 text-[var(--text)]">{fmt(inv.amount)}</td>
                    <td className="py-3 px-4"><Badge color={inv.status === 'PAID' ? 'success' : 'accent'}>{inv.status}</Badge></td>
                  </tr>
                )) : <tr><td colSpan={3} className="py-8 text-center text-[var(--text-muted)]">No invoices yet</td></tr>}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <PaymentModal show={showTopUp} onClose={() => setShowTopUp(false)} mode="WALLET_TOPUP" onPaymentSuccess={() => { fetchData(); setShowTopUp(false) }} />
    </div>
  )
}
