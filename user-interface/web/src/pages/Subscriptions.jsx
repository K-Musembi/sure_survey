import { useState, useEffect } from 'react'
import { Card, Button, Badge, Table, Tabs, Alert, Spinner } from 'flowbite-react'
import { billingAPI } from '../services/apiServices'
import PaymentModal from '../components/PaymentModal'
import { HiCheckCircle, HiCreditCard, HiCurrencyDollar, HiClock, HiExclamationCircle } from 'react-icons/hi'

const Subscriptions = () => {
  const [walletBalance, setWalletBalance] = useState(0)
  const [transactions, setTransactions] = useState([])
  const [subscription, setSubscription] = useState(null)
  const [invoices, setInvoices] = useState([])
  const [plans, setPlans] = useState([])
  const [showTopUpModal, setShowTopUpModal] = useState(false)
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)

  const fetchData = async () => {
    setIsLoading(true)
    setError(null)
    try {
      // Fetch data in parallel for speed
      const [balanceRes, transRes, subRes, invRes, plansRes] = await Promise.allSettled([
        billingAPI.getWalletBalance(),
        billingAPI.getWalletTransactions(),
        billingAPI.getSubscription(),
        billingAPI.getInvoices(),
        billingAPI.getAllPlans()
      ])
      
      if (balanceRes.status === 'fulfilled') setWalletBalance(balanceRes.value.data)
      if (transRes.status === 'fulfilled') setTransactions(transRes.value.data)
      if (subRes.status === 'fulfilled') setSubscription(subRes.value.data)
      if (invRes.status === 'fulfilled') setInvoices(invRes.value.data)
      if (plansRes.status === 'fulfilled') setPlans(plansRes.value.data)

    } catch (error) {
      console.error('Error fetching billing data', error)
      setError('Could not load billing information. Please try again later.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  const [isUpgrading, setIsUpgrading] = useState(false)
  const [upgradeError, setUpgradeError] = useState('')

  const handleUpgrade = async (planId) => {
    setIsUpgrading(true)
    setUpgradeError('')
    try {
      await billingAPI.createSubscription({ planId })
      await fetchData() // Refresh data
    } catch (error) {
      console.error('Upgrade failed', error)
      setUpgradeError(error.response?.data?.message || 'Failed to update plan')
    } finally {
      setIsUpgrading(false)
    }
  }

  if (isLoading) {
    return (
      <div className="flex justify-center items-center h-64">
        <Spinner size="xl" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
           <h1 className="text-3xl font-bold text-gray-900">Billing & Subscriptions</h1>
           <p className="text-gray-600 mt-1">Manage your plan and wallet</p>
        </div>
      </div>

      {upgradeError && (
        <Alert color="failure" onDismiss={() => setUpgradeError('')}>
          {upgradeError}
        </Alert>
      )}

      {error && (
        <Alert color="failure" icon={HiExclamationCircle}>
          <span className="font-medium">Error!</span> {error}
        </Alert>
      )}

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Wallet Card */}
        <Card className="bg-gradient-to-br from-purple-50 to-white border border-purple-100">
          <div className="flex justify-between items-start">
            <div>
              <h5 className="text-xl font-bold tracking-tight text-gray-900">
                Wallet Balance
              </h5>
              <p className="text-sm text-gray-500">Available for rewards & surveys</p>
            </div>
            <div className="p-2 bg-purple-100 rounded-full">
              <HiCurrencyDollar className="w-8 h-8 text-purple-600" />
            </div>
          </div>
          
          <div className="mt-4 mb-4">
            <span className="text-4xl font-extrabold text-gray-900">
              {new Intl.NumberFormat('en-KE', { style: 'currency', currency: 'KES' }).format(walletBalance || 0)}
            </span>
          </div>
          
          <Button onClick={() => setShowTopUpModal(true)} color="purple">
            Top Up Wallet
          </Button>
        </Card>

        {/* Current Plan Card */}
        <Card>
          <div className="flex justify-between items-start">
             <div>
              <h5 className="text-xl font-bold tracking-tight text-gray-900">
                Current Plan
              </h5>
              <p className="text-sm text-gray-500">Your subscription status</p>
            </div>
             <div className="p-2 bg-green-100 rounded-full">
              <HiCheckCircle className="w-8 h-8 text-green-600" />
            </div>
          </div>
          
          <div className="mt-4 space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-gray-600">Plan Name:</span>
              <span className="font-bold text-lg">{subscription?.plan?.name || 'Free Tier'}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-gray-600">Status:</span>
              <Badge color="success">{subscription?.status || 'Active'}</Badge>
            </div>
             <div className="flex justify-between items-center">
              <span className="text-gray-600">Renews on:</span>
              <span className="font-medium">
                {subscription?.currentPeriodEnd ? new Date(subscription.currentPeriodEnd).toLocaleDateString() : 'N/A'}
              </span>
            </div>
          </div>
        </Card>
      </div>

      {/* Available Plans */}
      <Card>
        <h3 className="text-xl font-bold text-gray-900 mb-4">Available Plans</h3>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {plans.length > 0 ? plans.map((plan) => {
            const isCurrent = subscription?.plan?.name === plan.name
            let featuresList = []
            try {
               const parsed = JSON.parse(plan.features)
               // Simple heuristic to display features
               featuresList = Object.entries(parsed).map(([k, v]) => `${k}: ${v}`)
            } catch (e) {
               featuresList = ['Standard Features']
            }

            return (
              <div key={plan.id} className={`border rounded-lg p-4 flex flex-col ${isCurrent ? 'border-primary-500 bg-primary-50' : 'border-gray-200'}`}>
                <h4 className="font-bold text-lg">{plan.name}</h4>
                <div className="text-2xl font-bold mt-2">${plan.price}<span className="text-sm font-normal text-gray-500">/{plan.billingInterval}</span></div>
                <ul className="mt-4 space-y-2 flex-grow text-sm text-gray-600">
                  {featuresList.map((f, i) => (
                    <li key={i} className="flex items-center">
                      <HiCheckCircle className="w-4 h-4 text-green-500 mr-2" />
                      {f}
                    </li>
                  ))}
                </ul>
                <Button 
                  className="mt-4" 
                  color={isCurrent ? 'success' : 'dark'}
                  disabled={isCurrent || isUpgrading}
                  onClick={() => handleUpgrade(plan.id)}
                >
                  {isCurrent ? 'Current Plan' : (isUpgrading ? 'Updating...' : 'Switch Plan')}
                </Button>
              </div>
            )
          }) : (
            <div className="col-span-full text-center py-4 text-gray-500">No plans available.</div>
          )}
        </div>
      </Card>

      {/* Tabs for History */}
      <Tabs aria-label="Billing tabs" variant="underline">
        <Tabs.Item active icon={HiClock} title="Wallet Transactions">
          <Card>
             <div className="overflow-x-auto">
                <Table>
                  <Table.Head>
                    <Table.HeadCell>Date</Table.HeadCell>
                    <Table.HeadCell>Description</Table.HeadCell>
                    <Table.HeadCell>Type</Table.HeadCell>
                    <Table.HeadCell>Amount</Table.HeadCell>
                    <Table.HeadCell>Reference</Table.HeadCell>
                  </Table.Head>
                  <Table.Body className="divide-y">
                    {transactions?.map((tx) => (
                      <Table.Row key={tx.id} className="bg-white">
                        <Table.Cell>{new Date(tx.createdAt).toLocaleDateString()}</Table.Cell>
                        <Table.Cell>{tx.description}</Table.Cell>
                        <Table.Cell>
                          <Badge color={tx.type === 'CREDIT' ? 'success' : 'failure'}>
                            {tx.type}
                          </Badge>
                        </Table.Cell>
                        <Table.Cell className={tx.type === 'CREDIT' ? 'text-green-600 font-bold' : 'text-red-600 font-bold'}>
                          {tx.type === 'CREDIT' ? '+' : '-'}{tx.amount}
                        </Table.Cell>
                        <Table.Cell className="font-mono text-xs">{tx.referenceId}</Table.Cell>
                      </Table.Row>
                    ))}
                    {(!transactions || transactions.length === 0) && (
                       <Table.Row>
                         <Table.Cell colSpan={5} className="text-center py-4">No transactions found</Table.Cell>
                       </Table.Row>
                    )}
                  </Table.Body>
                </Table>
             </div>
          </Card>
        </Tabs.Item>

        <Tabs.Item icon={HiCreditCard} title="Invoices">
           <Card>
             <div className="overflow-x-auto">
                <Table>
                  <Table.Head>
                    <Table.HeadCell>Date</Table.HeadCell>
                    <Table.HeadCell>Amount</Table.HeadCell>
                    <Table.HeadCell>Status</Table.HeadCell>
                    <Table.HeadCell>Download</Table.HeadCell>
                  </Table.Head>
                  <Table.Body className="divide-y">
                    {invoices?.map((inv) => (
                      <Table.Row key={inv.id} className="bg-white">
                        <Table.Cell>{new Date(inv.createdAt).toLocaleDateString()}</Table.Cell>
                        <Table.Cell>{inv.amount}</Table.Cell>
                        <Table.Cell>
                           <Badge color={inv.status === 'PAID' ? 'success' : 'warning'}>{inv.status}</Badge>
                        </Table.Cell>
                        <Table.Cell>
                          {inv.invoicePdfUrl && (
                            <a href={inv.invoicePdfUrl} target="_blank" rel="noreferrer" className="text-primary-600 hover:underline">
                              PDF
                            </a>
                          )}
                        </Table.Cell>
                      </Table.Row>
                    ))}
                     {(!invoices || invoices.length === 0) && (
                       <Table.Row>
                         <Table.Cell colSpan={4} className="text-center py-4">No invoices found</Table.Cell>
                       </Table.Row>
                    )}
                  </Table.Body>
                </Table>
             </div>
           </Card>
        </Tabs.Item>
      </Tabs>

      {/* Top Up Modal */}
      <PaymentModal 
        show={showTopUpModal} 
        onClose={() => setShowTopUpModal(false)}
        mode="WALLET_TOPUP"
        onPaymentSuccess={() => {
           fetchData()
           setShowTopUpModal(false)
        }}
      />
    </div>
  )
}

export default Subscriptions
