import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { Navbar, Button, Avatar, Dropdown } from 'flowbite-react'
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
      <Navbar.Brand as={Link} to="/">
        <span className="self-center whitespace-nowrap text-2xl font-bold text-primary-600">
          SureSurvey
        </span>
      </Navbar.Brand>

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
                  rounded
                  className="w-8 h-8"
                >
                  <div className="font-medium text-sm">
                    {user?.name?.charAt(0)?.toUpperCase() || 'U'}
                  </div>
                </Avatar>
              }
            >
              <Dropdown.Header>
                <span className="block text-sm font-medium text-gray-900">
                  {user?.name || 'User'}
                </span>
                <span className="block truncate text-sm text-gray-500">
                  {user?.email || ''}
                </span>
              </Dropdown.Header>
              
              <Dropdown.Item as={Link} to="/dashboard">
                Dashboard
              </Dropdown.Item>
              
              <Dropdown.Item as={Link} to="/profile">
                Profile Settings
              </Dropdown.Item>
              
              <Dropdown.Divider />
              
              <Dropdown.Item 
                onClick={handleLogout}
                className="text-red-600 hover:bg-red-50"
              >
                Sign out
              </Dropdown.Item>
            </Dropdown>
          </>
        ) : (
          <>
            {/* Login Button */}
            <Button
              as={Link}
              to="/login"
              color="gray"
              size="sm"
              className="text-gray-700 hover:text-gray-900"
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
        <Navbar.Toggle 
          onClick={() => setIsMenuOpen(!isMenuOpen)}
          className="md:hidden"
        />
      </div>

      {/* Navigation Links */}
      <Navbar.Collapse className={isMenuOpen ? 'block' : 'hidden'}>
        <Navbar.Link
          as={Link}
          to="/"
          className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent"
        >
          Home
        </Navbar.Link>
        
        <Navbar.Link
          as={Link}
          to="/features"
          className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent"
        >
          Features
        </Navbar.Link>
        
        <Navbar.Link
          as={Link}
          to="/pricing"
          className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent"
        >
          Pricing
        </Navbar.Link>
        
        <Navbar.Link
          as={Link}
          to="/contact"
          className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent"
        >
          Contact
        </Navbar.Link>

        {/* Mobile-only authenticated links */}
        {isAuthenticated && (
          <>
            <Navbar.Link
              as={Link}
              to="/dashboard"
              className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent md:hidden"
            >
              Dashboard
            </Navbar.Link>
            
            <Navbar.Link
              onClick={handleLogout}
              className="text-red-600 hover:text-red-700 md:hover:bg-transparent md:hidden cursor-pointer"
            >
              Sign Out
            </Navbar.Link>
          </>
        )}

        {/* Mobile-only auth links */}
        {!isAuthenticated && (
          <>
            <Navbar.Link
              as={Link}
              to="/login"
              className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent md:hidden"
            >
              Sign In
            </Navbar.Link>
            
            <Navbar.Link
              as={Link}
              to="/signup"
              className="text-primary-600 hover:text-primary-700 md:hover:bg-transparent md:hidden font-medium"
            >
              Get Started
            </Navbar.Link>
          </>
        )}
      </Navbar.Collapse>
    </Navbar>
  )
}

export default NavBar