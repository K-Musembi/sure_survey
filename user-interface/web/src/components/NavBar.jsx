import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Navbar, NavbarBrand, NavbarToggle, NavbarCollapse, 
  NavbarLink, Button, Avatar, Dropdown, DropdownHeader, DropdownItem, DropdownDivider } from 'flowbite-react'
import useAuthStore from '../stores/authStore'
import { useLogout } from '../hooks/useApi'

const NavBar = () => {
  const navigate = useNavigate()
  const { user, isAuthenticated } = useAuthStore()
  const logoutMutation = useLogout()
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  const handleLogout = async () => {
    try {
      await logoutMutation.mutateAsync()
      navigate('/')
    } catch (error) {
      console.error('Logout failed:', error)
      // Force logout even if API call fails
      navigate('/')
    }
  }

  return (
    <Navbar fluid rounded className="bg-white shadow-sm border-b border-gray-200 sticky top-0 z-50">
      <NavbarBrand as={Link} to="/" className="flex items-center">
        <span className="self-center whitespace-nowrap text-2xl font-bold text-primary-600">
          Sure Survey
        </span>
      </NavbarBrand>

      <div className="flex md:order-2 gap-3">
        {isAuthenticated ? (
          <>
            {/* Dashboard Button */}
            <Button
              as={Link}
              to="/dashboard"
              color="gray"
              size="sm"
              className="hidden md:flex"
            >
              Dashboard
            </Button>

            {/* User Dropdown */}
            <Dropdown
              arrowIcon={false}
              inline
              label={
                <Avatar
                  alt="User settings"
                  img={user?.avatar || undefined}
                  placeholderInitials={user?.name?.charAt(0)?.toUpperCase() || 'U'}
                  rounded
                  className="w-8 h-8"
                />
              }
            >
              <DropdownHeader>
                <span className="block text-sm font-medium text-gray-900">
                  {user?.name || 'User'}
                </span>
                <span className="block truncate text-sm text-gray-500">
                  {user?.email || ''}
                </span>
              </DropdownHeader>
              
              <DropdownItem as={Link} to="/dashboard">
                Dashboard
              </DropdownItem>
              
              <DropdownItem as={Link} to="/profile">
                Profile Settings
              </DropdownItem>
              
              <DropdownDivider />
              
              <DropdownItem 
                onClick={handleLogout}
                className="text-red-600 hover:bg-red-50"
              >
                Sign out
              </DropdownItem>
            </Dropdown>
          </>
        ) : (
          <>
            {/* Login Button */}
            <Button
              as={Link}
              to="/login"
              size="sm"
              className="bg-gray-100 hover:bg-gray-200 text-gray-800"
            >
              Sign In
            </Button>

            {/* Sign Up Button */}
            <Button
              as={Link}
              to="/signup"
              className="bg-primary-500 hover:bg-primary-600 text-white"
              size="sm"
            >
              Get Started
            </Button>
          </>
        )}

        {/* Mobile menu toggle */}
        <NavbarToggle 
          onClick={() => setIsMenuOpen(!isMenuOpen)}
          className="md:hidden"
        />
      </div>

      {/* Navigation Links */}
      <NavbarCollapse className={isMenuOpen ? 'block' : 'hidden'}>
        <NavbarLink
          as={Link}
          to="/"
          className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent"
        >
          Home
        </NavbarLink>
        
        <NavbarLink
          as={Link}
          to="/features"
          className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent"
        >
          Features
        </NavbarLink>
        
        <NavbarLink
          as={Link}
          to="/pricing"
          className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent"
        >
          Pricing
        </NavbarLink>
        
        <NavbarLink
          as={Link}
          to="/contact"
          className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent"
        >
          Contact
        </NavbarLink>

        {/* Mobile-only authenticated links */}
        {isAuthenticated && (
          <>
            <NavbarLink
              as={Link}
              to="/dashboard"
              className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent md:hidden"
            >
              Dashboard
            </NavbarLink>
            
            <NavbarLink
              onClick={handleLogout}
              className="text-red-600 hover:text-red-700 md:hover:bg-transparent md:hidden cursor-pointer"
            >
              Sign Out
            </NavbarLink>
          </>
        )}

        {/* Mobile-only auth links */}
        {!isAuthenticated && (
          <>
            <NavbarLink
              as={Link}
              to="/login"
              className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent md:hidden"
            >
              Sign In
            </NavbarLink>
            
            <NavbarLink
              as={Link}
              to="/signup"
              className="text-primary-600 hover:text-primary-700 md:hover:bg-transparent md:hidden font-medium"
            >
              Sign Up
            </NavbarLink>
          </>
        )}
      </NavbarCollapse>
    </Navbar>
  )
}

export default NavBar