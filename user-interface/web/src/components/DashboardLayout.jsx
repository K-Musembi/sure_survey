import { useState } from 'react'
import { Link, useLocation } from 'react-router-dom'
import { Sidebar, SidebarItem, SidebarItemGroup, SidebarItems } from 'flowbite-react'
import { HiChartPie, HiViewBoards, HiUser, HiCog, HiCreditCard, HiOutlineLogout, HiLightningBolt, HiUserGroup } from 'react-icons/hi'
import NavBar from './NavBar'
import useAuthStore from '../stores/authStore'
import { useLogout } from '../hooks/useApi'

const DashboardLayout = ({ children }) => {
  const location = useLocation()
  const { logout } = useAuthStore()
  const logoutMutation = useLogout()

  const handleLogout = async () => {
    try {
      await logoutMutation.mutateAsync()
      // The useLogout hook already handles navigation to '/'
    } catch (error) {
      console.error('Logout failed:', error)
      // Force logout even if API call fails
      logout()
    }
  }

  return (
    <div className="flex flex-col min-h-screen bg-gray-50">
      <NavBar isDashboard={true} />
      
      <div className="flex flex-grow">
        <Sidebar aria-label="Dashboard sidebar" className="w-64 flex-shrink-0 bg-gray-100 text-gray-900">
          <SidebarItems>
            <SidebarItemGroup>
              <SidebarItem 
                as={Link} 
                to="/dashboard" 
                icon={HiChartPie} 
                active={location.pathname === '/dashboard'}
              >
                Surveys
              </SidebarItem>
              <SidebarItem 
                as={Link} 
                to="/dashboard/analytics" 
                icon={HiViewBoards} 
                active={location.pathname === '/dashboard/analytics'}
              >
                Analytics
              </SidebarItem>
              <SidebarItem 
                as={Link} 
                to="/subscriptions" 
                icon={HiCreditCard} 
                active={location.pathname === '/subscriptions'}
              >
                Billing
              </SidebarItem>
              <SidebarItem 
                as={Link} 
                to="/profile" 
                icon={HiUser} 
                active={location.pathname === '/profile'}
              >
                Profile
              </SidebarItem>
              <SidebarItem 
                as={Link} 
                to="/settings" 
                icon={HiCog} 
                active={location.pathname === '/settings'}
              >
                Settings
              </SidebarItem>
              <SidebarItem 
                onClick={handleLogout} 
                icon={HiOutlineLogout} 
                className="cursor-pointer"
              >
                Sign Out
              </SidebarItem>
            </SidebarItemGroup>
          </SidebarItems>
        </Sidebar>
        
        <main className="flex-grow p-8 overflow-x-hidden">
          {children}
        </main>
      </div>
    </div>
  )
}

export default DashboardLayout
