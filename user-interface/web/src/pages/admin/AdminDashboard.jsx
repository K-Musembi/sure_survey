import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, Table, Badge, Tabs, Modal, ModalHeader, ModalBody, ModalContext, ModalFooter, modalTheme,
  TabItem, TableHead, TableBody, TableHeadCell, TableCell, Label, TextInput, Select, Alert, Textarea, 
  TableRow} from 'flowbite-react'
import { adminAPI, billingAPI } from '../../services/apiServices'
import useAuthStore from '../../stores/authStore'
import { HiLogout, HiRefresh, HiCash, HiOfficeBuilding, HiExclamationCircle, HiTemplate } from 'react-icons/hi'

const AdminDashboard = () => {
  const navigate = useNavigate()
  const { logout, isAdmin } = useAuthStore()
  
  const [tenants, setTenants] = useState([])
  const [settings, setSettings] = useState([])
  const [plans, setPlans] = useState([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState(null)
  
  const [restockModalOpen, setRestockModalOpen] = useState(false)
  const [restockData, setRestockData] = useState({ type: 'AIRTIME', amount: '' })
  const [restockMessage, setRestockMessage] = useState(null)

  // Settings Edit State
  const [editSettingModalOpen, setEditSettingModalOpen] = useState(false)
  const [currentSetting, setCurrentSetting] = useState(null)
  const [settingValue, setSettingValue] = useState('')
  const [settingUpdateError, setSettingUpdateError] = useState('')

  // Plan Edit/Create State
  const [planModalOpen, setPlanModalOpen] = useState(false)
  const [editingPlan, setEditingPlan] = useState(null)
  const [planForm, setPlanForm] = useState({
    name: '',
    price: '',
    interval: 'MONTHLY',
    features: '' // JSON string
  })
  const [planMessage, setPlanMessage] = useState('')

  useEffect(() => {
    if (!isAdmin()) {
      navigate('/admin')
    }
  }, [isAdmin, navigate])

  const fetchData = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const [tenantsRes, settingsRes, plansRes] = await Promise.all([
        adminAPI.getAllTenants(),
        adminAPI.getSettings(),
        billingAPI.getAllPlans()
      ])
      setTenants(tenantsRes.data)
      setSettings(settingsRes.data)
      setPlans(plansRes.data)
    } catch (error) {
      console.error('Failed to fetch admin data', error)
      setError('Failed to load dashboard data. Please check your connection.')
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    fetchData()
  }, [])

  const handleLogout = async () => {
    await logout()
    navigate('/admin')
  }

  const handleRestock = async (e) => {
    e.preventDefault()
    setRestockMessage(null)
    try {
      await adminAPI.restockSystemWallet(restockData.type, restockData.amount)
      setRestockMessage({ type: 'success', text: 'System wallet restocked successfully!' })
      setTimeout(() => {
        setRestockModalOpen(false)
        setRestockMessage(null)
        setRestockData({ type: 'AIRTIME', amount: '' })
      }, 2000)
    } catch (error) {
      setRestockMessage({ type: 'failure', text: 'Restock failed: ' + (error.response?.data?.message || error.message) })
    }
  }

  const openEditSetting = (setting) => {
    setCurrentSetting(setting)
    setSettingValue(setting.value)
    setSettingUpdateError('')
    setEditSettingModalOpen(true)
  }

  const handleUpdateSetting = async () => {
    setSettingUpdateError('')
    try {
      await adminAPI.updateSettings([{ key: currentSetting.key, value: settingValue }])
      setEditSettingModalOpen(false)
      fetchData() // Refresh list
    } catch (error) {
      console.error('Failed to update setting', error)
      setSettingUpdateError('Update failed: ' + (error.response?.data?.message || error.message))
    }
  }

  // Plan Handlers
  const openCreatePlan = () => {
    setEditingPlan(null)
    setPlanForm({ name: '', price: '', interval: 'MONTHLY', features: `{
  "maxSurveys": 10,
  "maxResponses": 100
}` })
    setPlanMessage('')
    setPlanModalOpen(true)
  }

  const openEditPlan = (plan) => {
    setEditingPlan(plan)
    setPlanForm({
      name: plan.name,
      price: plan.price,
      interval: plan.billingInterval,
      features: plan.features || '{}'
    })
    setPlanMessage('')
    setPlanModalOpen(true)
  }

  const handlePlanSubmit = async (e) => {
    e.preventDefault()
    setPlanMessage('')
    
    try {
      let parsedFeatures = {}
      try {
        parsedFeatures = JSON.parse(planForm.features)
      } catch (err) {
        throw new Error('Invalid JSON format in Features field')
      }

      const payload = {
        name: planForm.name,
        price: parseFloat(planForm.price),
        interval: planForm.interval,
        features: parsedFeatures
      }

      if (editingPlan) {
        // Update
        await adminAPI.updatePlan({ 
          planId: editingPlan.id, 
          price: payload.price, 
          features: payload.features 
        })
      } else {
        // Create
        await adminAPI.createPlan(payload)
      }
      
      setPlanModalOpen(false)
      fetchData()
    } catch (error) {
      setPlanMessage('Error: ' + (error.message || 'Operation failed'))
    }
  }

  return (
    <div className="min-h-screen bg-gray-100">
      {/* Admin Header */}
      <header className="bg-gray-900 text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
          <div className="flex items-center">
            <span className="text-xl font-bold">Sure Survey Admin</span>
            <Badge color="gray" className="ml-3">SUPER ADMIN</Badge>
          </div>
          <div className="flex items-center gap-4">
            <Button size="xs" color="light" onClick={fetchData}>
              <HiRefresh className="mr-1 h-4 w-4" /> Refresh
            </Button>
            <Button size="xs" color="failure" onClick={handleLogout}>
              <HiLogout className="mr-1 h-4 w-4" /> Logout
            </Button>
          </div>
        </div>
      </header>

      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {error && (
          <Alert color="failure" icon={HiExclamationCircle} className="mb-4" onDismiss={() => setError(null)}>
            {error}
          </Alert>
        )}
        
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-6 mb-8">
          {/* Quick Actions Card */}
          <Card className="lg:col-span-1">
            <h5 className="text-lg font-bold tracking-tight text-gray-900">
              System Operations
            </h5>
            <p className="font-normal text-gray-700">
              Manage global system resources.
            </p>
            <Button color="success" onClick={() => setRestockModalOpen(true)}>
              <HiCash className="mr-2 h-5 w-5" />
              Restock Wallet
            </Button>
          </Card>

          {/* Stats Cards (Mock data or derived from tenants) */}
          <Card className="lg:col-span-1">
            <div className="flex flex-col items-center justify-center">
              <dt className="mb-2 text-3xl font-extrabold">{tenants.length}</dt>
              <dd className="text-gray-500 dark:text-gray-400">Total Tenants</dd>
            </div>
          </Card>
           <Card className="lg:col-span-1">
            <div className="flex flex-col items-center justify-center">
              <dt className="mb-2 text-3xl font-extrabold">
                {tenants.reduce((acc, t) => acc + (t.userCount || 0), 0)}
              </dt>
              <dd className="text-gray-500 dark:text-gray-400">Total Users</dd>
            </div>
          </Card>
        </div>

        <Tabs aria-label="Admin tabs" variant="underline">
          <TabItem active icon={HiOfficeBuilding} title="Tenants">
            <Card>
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-xl font-bold text-gray-900">Registered Tenants</h3>
              </div>
              <div className="overflow-x-auto">
                <Table hoverable>
                  <TableHead>
                    <TableHeadCell>Tenant Name</TableHeadCell>
                    <TableHeadCell>Slug</TableHeadCell>
                    <TableHeadCell>Plan</TableHeadCell>
                    <TableHeadCell>Users</TableHeadCell>
                    <TableHeadCell>Status</TableHeadCell>
                  </TableHead>
                  <TableBody className="divide-y">
                    {tenants.map((tenant) => (
                      <TableRow key={tenant.id} className="bg-white">
                        <TableCell className="whitespace-nowrap font-medium text-gray-900">
                          {tenant.name}
                        </TableCell>
                        <TableCell>{tenant.slug}</TableCell>
                        <TableCell>
                          <Badge color="info">{tenant.plan || 'Free'}</Badge>
                        </TableCell>
                        <TableCell>{tenant.userCount || 0}</TableCell>
                        <TableCell>
                          <Badge color={tenant.status === 'ACTIVE' ? 'success' : 'warning'}>
                            {tenant.status || 'ACTIVE'}
                          </Badge>
                        </TableCell>
                      </TableRow>
                    ))}
                    {tenants.length === 0 && !isLoading && (
                       <TableRow>
                         <TableCell colSpan={6} className="text-center py-4">No tenants found</TableCell>
                       </TableRow>
                    )}
                  </TableBody>
                </Table>
              </div>
            </Card>
          </TabItem>
          
          <TabItem icon={HiTemplate} title="Subscription Plans">
            <Card>
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-xl font-bold text-gray-900">Manage Plans</h3>
                <Button size="sm" onClick={openCreatePlan}>
                  Create Plan
                </Button>
              </div>
              <div className="overflow-x-auto">
                <Table>
                  <TableHead>
                    <TableHeadCell>Name</TableHeadCell>
                    <TableHeadCell>Price</TableHeadCell>
                    <TableHeadCell>Interval</TableHeadCell>
                    <TableHeadCell>Features</TableHeadCell>
                    <TableHeadCell>Action</TableHeadCell>
                  </TableHead>
                  <TableBody className="divide-y">
                    {plans.map((plan) => (
                      <TableRow key={plan.id} className="bg-white">
                        <TableCell className="font-bold">{plan.name}</TableCell>
                        <TableCell>${plan.price}</TableCell>
                        <TableCell>{plan.billingInterval}</TableCell>
                        <TableCell className="max-w-xs truncate">{plan.features}</TableCell>
                        <TableCell>
                          <Button size="xs" color="gray" onClick={() => openEditPlan(plan)}>Edit</Button>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </Card>
          </TabItem>

          <TabItem title="System Settings">
            <Card>
              <h3 className="text-xl font-bold text-gray-900 mb-4">Global Configuration</h3>
              <div className="overflow-x-auto">
                <Table>
                  <TableHead>
                    <TableHeadCell>Key</TableHeadCell>
                    <TableHeadCell>Value</TableHeadCell>
                    <TableHeadCell>Description</TableHeadCell>
                    <TableHeadCell>Action</TableHeadCell>
                  </TableHead>
                  <TableBody className="divide-y">
                    {settings.map((setting) => (
                      <TableRow key={setting.key}>
                         <TableCell className="font-mono text-xs">{setting.key}</TableCell>
                         <TableCell>{setting.value}</TableCell>
                         <TableCell>{setting.description}</TableCell>
                         <TableCell>
                           <Button size="xs" color="gray" onClick={() => openEditSetting(setting)}>Edit</Button>
                         </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              </div>
            </Card>
          </TabItem>
        </Tabs>
      </main>

      {/* Restock Modal */}
      <Modal show={restockModalOpen} onClose={() => setRestockModalOpen(false)}>
        <ModalHeader>System Wallet Restock</ModalHeader>
        <ModalBody>
          <div className="space-y-6">
            <p className="text-base leading-relaxed text-gray-500">
              Purchase bulk Airtime or Data Bundles from Safaricom for the system inventory.
            </p>
            {restockMessage && (
              <Alert color={restockMessage.type}>
                {restockMessage.text}
              </Alert>
            )}
            <div>
              <Label htmlFor="type" value="Inventory Type" />
              <Select 
                id="type" 
                required 
                value={restockData.type}
                onChange={(e) => setRestockData({...restockData, type: e.target.value})}
              >
                <option value="AIRTIME">Airtime</option>
                <option value="DATA_BUNDLE">Data Bundle</option>
              </Select>
            </div>
            <div>
              <Label htmlFor="amount" value="Amount (KES)" />
              <TextInput 
                id="amount" 
                type="number" 
                placeholder="e.g. 50000" 
                required 
                value={restockData.amount}
                onChange={(e) => setRestockData({...restockData, amount: e.target.value})}
              />
            </div>
          </div>
        </ModalBody>
        <ModalFooter>
          <Button onClick={handleRestock}>Process Purchase</Button>
          <Button color="gray" onClick={() => setRestockModalOpen(false)}>
            Cancel
          </Button>
        </ModalFooter>
      </Modal>

      {/* Edit Setting Modal */}
      <Modal show={editSettingModalOpen} onClose={() => setEditSettingModalOpen(false)}>
        <ModalHeader>Edit Setting</ModalHeader>
        <ModalBody>
           <div className="space-y-4">
             {settingUpdateError && (
               <Alert color="failure" icon={HiExclamationCircle}>
                 {settingUpdateError}
               </Alert>
             )}
             <p className="text-sm text-gray-500 font-mono">{currentSetting?.key}</p>
             <p className="text-sm text-gray-500">{currentSetting?.description}</p>
             <div>
               <Label htmlFor="sValue" value="Value" />
               <TextInput 
                 id="sValue"
                 value={settingValue}
                 onChange={(e) => setSettingValue(e.target.value)}
               />
             </div>
           </div>
        </ModalBody>
        <ModalFooter>
           <Button onClick={handleUpdateSetting}>Save Changes</Button>
           <Button color="gray" onClick={() => setEditSettingModalOpen(false)}>Cancel</Button>
        </ModalFooter>
      </Modal>

      {/* Plan Modal */}
      <Modal show={planModalOpen} onClose={() => setPlanModalOpen(false)}>
        <ModalHeader>{editingPlan ? 'Edit Plan' : 'Create Plan'}</ModalHeader>
        <ModalBody>
          <form onSubmit={handlePlanSubmit} className="space-y-4">
            {planMessage && <Alert color="failure">{planMessage}</Alert>}
            
            <div>
              <Label htmlFor="pName">Plan Name</Label>
              <TextInput 
                id="pName" 
                value={planForm.name} 
                onChange={e => setPlanForm({...planForm, name: e.target.value})}
                disabled={!!editingPlan} // Name usually unique/immutable logic
                required 
              />
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="pPrice">Price</Label>
                <TextInput 
                  id="pPrice" 
                  type="number" 
                  value={planForm.price}
                  onChange={e => setPlanForm({...planForm, price: e.target.value})}
                  required 
                />
              </div>
              <div>
                <Label htmlFor="pInterval">Billing Interval</Label>
                <Select 
                  id="pInterval"
                  value={planForm.interval}
                  onChange={e => setPlanForm({...planForm, interval: e.target.value})}
                  disabled={!!editingPlan}
                >
                  <option value="MONTHLY">Monthly</option>
                  <option value="YEARLY">Yearly</option>
                </Select>
              </div>
            </div>

            <div>
              <Label htmlFor="pFeatures">Features (JSON)</Label>
              <Textarea 
                id="pFeatures" 
                rows={6}
                className="font-mono text-sm"
                value={planForm.features}
                onChange={e => setPlanForm({...planForm, features: e.target.value})}
                placeholder='{"key": "value"}'
              />
              <p className="text-xs text-gray-500 mt-1">Must be valid JSON.</p>
            </div>

            <div className="flex justify-end pt-4">
              <Button type="submit">{editingPlan ? 'Update Plan' : 'Create Plan'}</Button>
            </div>
          </form>
        </ModalBody>
      </Modal>
    </div>
  )
}

export default AdminDashboard