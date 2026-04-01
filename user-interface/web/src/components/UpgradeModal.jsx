import { useState, useEffect } from 'react'
import Modal from './ui/Modal'
import Button from './ui/Button'
import Badge from './ui/Badge'
import { billingAPI } from '../services/apiServices'
import useToast from '../hooks/useToast'
import { useQueryClient } from '@tanstack/react-query'
import { QUERY_KEYS } from '../lib/queryClient'
import { HiOutlineCheckCircle, HiOutlineX, HiOutlineArrowRight } from 'react-icons/hi'

const FREE_TIER = {
  name: 'Free',
  price: 0,
  features: {
    displayFeatures: [
      'Up to 3 surveys',
      '25 responses per survey',
      'Web channel only',
      'Basic dashboard',
    ],
  },
}

/**
 * Modal that displays plan comparison and handles subscription upgrades.
 * Props:
 *  - open: boolean
 *  - onClose: () => void
 *  - currentPlan: string ("Free", "Basic", "Pro", "Enterprise")
 *  - highlightPlan: string (optional — which plan tab to emphasize)
 *  - reason: string (optional — context message like "You've reached 3/3 free surveys")
 */
export default function UpgradeModal({ open, onClose, currentPlan = 'Free', highlightPlan, reason }) {
  const toast = useToast()
  const queryClient = useQueryClient()
  const [plans, setPlans] = useState([])
  const [loading, setLoading] = useState(true)
  const [upgrading, setUpgrading] = useState(null)

  useEffect(() => {
    if (!open) return
    billingAPI.getAllPlans()
      .then((res) => setPlans(res.data))
      .catch(() => toast.error('Failed to load plans'))
      .finally(() => setLoading(false))
  }, [open])

  const fmt = (v) =>
    new Intl.NumberFormat('en-KE', { style: 'currency', currency: 'KES', maximumFractionDigits: 0 }).format(v || 0)

  const handleUpgrade = async (planId) => {
    setUpgrading(planId)
    try {
      await billingAPI.createSubscription({ planId })
      toast.success('Plan upgraded successfully!')
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.billing.usage })
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.billing.subscription })
      queryClient.invalidateQueries({ queryKey: QUERY_KEYS.surveys.my })
      onClose?.()
    } catch (err) {
      toast.error(err.response?.data?.message || 'Upgrade failed. Please try again.')
    } finally {
      setUpgrading(null)
    }
  }

  // Build the full tier list: Free (synthetic) + DB plans
  const allTiers = [FREE_TIER, ...plans.map((p) => {
    let parsed = {}
    try { parsed = JSON.parse(p.features) } catch { /* ignore */ }
    return { ...p, features: parsed }
  })]

  const tierOrder = ['Free', 'Basic', 'Pro', 'Enterprise']
  const sorted = allTiers.sort((a, b) => tierOrder.indexOf(a.name) - tierOrder.indexOf(b.name))

  const normalize = (n) => n?.charAt(0).toUpperCase() + n?.slice(1).toLowerCase()
  const isCurrent = (name) => normalize(currentPlan) === name
  const isHighlighted = (name) => highlightPlan === name
  const canUpgradeTo = (name) => {
    const currentIdx = tierOrder.indexOf(normalize(currentPlan))
    const targetIdx = tierOrder.indexOf(name)
    return targetIdx > currentIdx
  }

  return (
    <Modal open={open} onClose={onClose} title="Choose a Plan" size="xl">
      {reason && (
        <div className="mb-5 rounded-lg border border-amber-300/30 bg-amber-500/10 p-3 text-sm text-amber-700 dark:text-amber-400">
          {reason}
        </div>
      )}

      {loading ? (
        <div className="flex items-center justify-center py-12">
          <div className="h-8 w-8 animate-spin rounded-full border-2 border-brand border-t-transparent" />
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
          {sorted.map((tier) => {
            const features = tier.features?.displayFeatures || []
            const isCustom = tier.features?.isCustomPricing
            const highlighted = isHighlighted(tier.name)
            const current = isCurrent(tier.name)

            return (
              <div
                key={tier.name}
                className={`relative rounded-xl border-2 p-5 flex flex-col transition-all ${
                  highlighted
                    ? 'border-brand shadow-lg shadow-brand/10 scale-[1.02]'
                    : current
                    ? 'border-brand/50 bg-brand/5'
                    : 'border-[var(--border)]'
                }`}
              >
                {highlighted && (
                  <div className="absolute -top-3 left-1/2 -translate-x-1/2">
                    <Badge color="brand">Recommended</Badge>
                  </div>
                )}

                <div className="flex justify-between items-start mb-2">
                  <h4 className="font-bold text-[var(--text)]">{tier.name}</h4>
                  {current && <Badge color="brand">Current</Badge>}
                </div>

                <div className="mb-4">
                  {tier.name === 'Free' ? (
                    <span className="text-2xl font-extrabold text-[var(--text)]">Free</span>
                  ) : isCustom ? (
                    <span className="text-lg font-bold text-[var(--text-muted)]">Custom pricing</span>
                  ) : (
                    <>
                      <span className="text-2xl font-extrabold text-[var(--text)]">{fmt(tier.price)}</span>
                      <span className="text-sm text-[var(--text-muted)]">/mo</span>
                    </>
                  )}
                </div>

                <ul className="space-y-2 flex-1 text-sm text-[var(--text-muted)] mb-4">
                  {features.map((f, i) => (
                    <li key={i} className="flex items-start gap-2">
                      <HiOutlineCheckCircle className="h-4 w-4 text-[var(--success)] flex-shrink-0 mt-0.5" />
                      {f}
                    </li>
                  ))}
                </ul>

                {current ? (
                  <Button variant="secondary" className="w-full" disabled>
                    Current Plan
                  </Button>
                ) : isCustom ? (
                  <a href="mailto:sales@suresurvey.co">
                    <Button variant="secondary" className="w-full">Contact Sales</Button>
                  </a>
                ) : canUpgradeTo(tier.name) ? (
                  <Button
                    className="w-full"
                    variant={highlighted ? 'primary' : 'secondary'}
                    loading={upgrading === tier.id}
                    disabled={!!upgrading}
                    onClick={() => handleUpgrade(tier.id)}
                  >
                    Upgrade <HiOutlineArrowRight className="ml-1 h-4 w-4 inline" />
                  </Button>
                ) : (
                  <Button variant="secondary" className="w-full" disabled>
                    {tier.name === 'Free' ? 'Free' : 'Downgrade'}
                  </Button>
                )}
              </div>
            )
          })}
        </div>
      )}
    </Modal>
  )
}
