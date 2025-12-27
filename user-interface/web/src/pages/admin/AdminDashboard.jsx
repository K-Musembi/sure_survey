import { useState, useEffect } from 'react'
import { useNavigate } from 'react-router-dom'
import { Button, Card, Table, Badge, Tabs, Modal, Label, TextInput, Select, Alert } from 'flowbite-react'
import { adminAPI } from '../../services/apiServices'
import useAuthStore from '../../stores/authStore'
import { HiLogout, HiRefresh, HiCash, HiOfficeBuilding, HiExclamationCircle } from 'react-icons/hi'

const AdminDashboard = () => {
  const navigate = useNavigate()
  const { logout, isAdmin } = useAuthStore()
  
  const [tenants, setTenants] = useState([])
  const [settings, setSettings] = useState([])
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

  // Verify admin access
  useEffect(() => {
    if (!isAdmin()) {
      navigate('/admin')
    }
  }, [isAdmin, navigate])

  const fetchData = async () => {
    setIsLoading(true)
    setError(null)
    try {
      const [tenantsRes, settingsRes] = await Promise.all([
        adminAPI.getAllTenants(),
        adminAPI.getSettings()
      ])
      setTenants(tenantsRes.data)
      setSettings(settingsRes.data)
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
      // API expects a list of settings to update
      await adminAPI.updateSettings([{ key: currentSetting.key, value: settingValue }])
      setEditSettingModalOpen(false)
      fetchData() // Refresh list
    } catch (error) {
      console.error('Failed to update setting', error)
      setSettingUpdateError('Update failed: ' + (error.response?.data?.message || error.message))
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
            <Button gradientDuoTone="greenToBlue" onClick={() => setRestockModalOpen(true)}>
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

        <Tabs.Group aria-label="Admin tabs" style="underline">
          <Tabs.Item active icon={HiOfficeBuilding} title="Tenants">
            <Card>
              <div className="flex items-center justify-between mb-4">
                <h3 className="text-xl font-bold text-gray-900">Registered Tenants</h3>
              </div>
              <div className="overflow-x-auto">
                <Table hoverable>
                  <Table.Head>
                    <Table.HeadCell>Tenant Name</Table.HeadCell>
                    <Table.HeadCell>Slug</Table.HeadCell>
                    <Table.HeadCell>Plan</Table.HeadCell>
                    <Table.HeadCell>Users</Table.HeadCell>
                    <Table.HeadCell>Status</Table.HeadCell>
                    <Table.HeadCell>Actions</Table.HeadCell>
                  </Table.Head>
                  <Table.Body className="divide-y">
                    {tenants.map((tenant) => (
                      <Table.Row key={tenant.id} className="bg-white dark:border-gray-700 dark:bg-gray-800">
                        <Table.Cell className="whitespace-nowrap font-medium text-gray-900 dark:text-white">
                          {tenant.name}
                        </Table.Cell>
                        <Table.Cell>{tenant.slug}</Table.Cell>
                        <Table.Cell>
                          <Badge color="info">{tenant.plan || 'Free'}</Badge>
                        </Table.Cell>
                        <Table.Cell>{tenant.userCount || 0}</Table.Cell>
                        <Table.Cell>
                          <Badge color={tenant.status === 'ACTIVE' ? 'success' : 'warning'}>
                            {tenant.status || 'ACTIVE'}
                          </Badge>
                        </Table.Cell>
                        <Table.Cell>
                          <a href="#" className="font-medium text-cyan-600 hover:underline dark:text-cyan-500">
                            Edit
                          </a>
                        </Table.Cell>
                      </Table.Row>
                    ))}
                    {tenants.length === 0 && !isLoading && (
                       <Table.Row>
                         <Table.Cell colSpan={6} className="text-center py-4">No tenants found</Table.Cell>
                       </Table.Row>
                    )}
                  </Table.Body>
                </Table>
              </div>
            </Card>
          </Tabs.Item>
          
          <Tabs.Item title="System Settings">
            <Card>
              <h3 className="text-xl font-bold text-gray-900 mb-4">Global Configuration</h3>
              <div className="overflow-x-auto">
                <Table>
                  <Table.Head>
                    <Table.HeadCell>Key</Table.HeadCell>
                    <Table.HeadCell>Value</Table.HeadCell>
                    <Table.HeadCell>Description</Table.HeadCell>
                    <Table.HeadCell>Action</Table.HeadCell>
                  </Table.Head>
                  <Table.Body className="divide-y">
                    {settings.map((setting) => (
                      <Table.Row key={setting.key}>
                         <Table.Cell className="font-mono text-xs">{setting.key}</Table.Cell>
                         <Table.Cell>{setting.value}</Table.Cell>
                         <Table.Cell>{setting.description}</Table.Cell>
                         <Table.Cell>
                           <Button size="xs" color="gray" onClick={() => openEditSetting(setting)}>Edit</Button>
                         </Table.Cell>
                      </Table.Row>
                    ))}
                  </Table.Body>
                </Table>
              </div>
            </Card>
          </Tabs.Item>
        </Tabs.Group>
      </main>

      {/* Restock Modal */}
      <Modal show={restockModalOpen} onClose={() => setRestockModalOpen(false)}>
        <Modal.Header>System Wallet Restock</Modal.Header>
        <Modal.Body>
          <div className="space-y-6">
            <p className="text-base leading-relaxed text-gray-500 dark:text-gray-400">
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
        </Modal.Body>
        <Modal.Footer>
          <Button onClick={handleRestock}>Process Purchase</Button>
          <Button color="gray" onClick={() => setRestockModalOpen(false)}>
            Cancel
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Edit Setting Modal */}
      <Modal show={editSettingModalOpen} onClose={() => setEditSettingModalOpen(false)}>
        <Modal.Header>Edit Setting: {currentSetting?.key}</Modal.Header>
        <Modal.Body>
           <div className="space-y-4">
             {settingUpdateError && (
               <Alert color="failure" icon={HiExclamationCircle}>
                 {settingUpdateError}
               </Alert>
             )}
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
        </Modal.Body>
        <Modal.Footer>
           <Button onClick={handleUpdateSetting}>Save Changes</Button>
           <Button color="gray" onClick={() => setEditSettingModalOpen(false)}>Cancel</Button>
        </Modal.Footer>
      </Modal>
    </div>
  )
}

export default AdminDashboard
