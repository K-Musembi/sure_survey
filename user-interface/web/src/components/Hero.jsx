import { Link } from 'react-router-dom'
import { Button } from 'flowbite-react'
import { HiChartBar, HiCurrencyDollar, HiLightningBolt } from "react-icons/hi";

const Hero = () => {
  return (
    <section className="relative bg-gradient-to-br from-primary-50 via-white to-primary-50 py-20 lg:py-32">
      {/* Background Pattern */}
      <div className="absolute inset-0 opacity-50" style={{
        backgroundImage: `url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%2384cc16' fill-opacity='0.05'%3E%3Ccircle cx='30' cy='30' r='1'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E")`
      }}></div>
      
      <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="lg:grid lg:grid-cols-12 lg:gap-8 lg:items-center">
          {/* Content */}
          <div className="lg:col-span-7">
            <div className="text-center lg:text-left">
              {/* Badge */}
              <div className="inline-flex items-center px-4 py-2 rounded-full bg-primary-100 text-primary-800 text-sm font-medium mb-8">
                <HiLightningBolt className="w-4 h-4 mr-2" />
                Now with Real-time Analytics
              </div>

              {/* Headline */}
              <h1 className="text-4xl sm:text-5xl lg:text-6xl font-bold text-gray-900 leading-tight">
                Create Surveys,{' '}
                <span className="text-primary-600">Reward</span>{' '}
                Participants
              </h1>

              {/* Subheadline */}
              <p className="mt-6 text-xl text-gray-600 leading-relaxed max-w-2xl mx-auto lg:mx-0">
                Build engaging surveys with built-in reward systems. Get quality responses 
                with real-time analytics and flexible payment options for participants.
              </p>

              {/* Feature highlights */}
              <div className="mt-8 flex flex-wrap gap-6 justify-center lg:justify-start">
                <div className="flex items-center text-gray-700">
                  <HiChartBar className="w-5 h-5 text-primary-500 mr-2" />
                  <span className="text-sm font-medium">Real-time Analytics</span>
                </div>
                <div className="flex items-center text-gray-700">
                  <HiCurrencyDollar className="w-5 h-5 text-primary-500 mr-2" />
                  <span className="text-sm font-medium">Reward System</span>
                </div>
                <div className="flex items-center text-gray-700">
                  <HiLightningBolt className="w-5 h-5 text-primary-500 mr-2" />
                  <span className="text-sm font-medium">Instant Deployment</span>
                </div>
              </div>

              {/* CTA Buttons */}
              <div className="mt-10 flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
                <Button
                  as={Link}
                  to="/signup"
                  size="xl"
                  className="bg-primary-500 hover:bg-primary-600 text-white px-8 py-4 text-lg font-semibold"
                >
                  Start Creating Surveys
                </Button>
                
                <Button
                  as={Link}
                  to="/demo"
                  color="gray"
                  size="xl"
                  className="text-gray-700 hover:text-primary-600 md:hover:bg-transparent md:hidden"
                >
                  Watch Demo
                </Button>
              </div>

              {/* Trust indicators */}
              <div className="mt-12 text-center lg:text-left">
                <p className="text-sm text-gray-500 mb-4">Trusted by teams at</p>
                <div className="flex flex-wrap gap-8 justify-center lg:justify-start opacity-60">
                  <div className="text-gray-400 font-semibold">Company A</div>
                  <div className="text-gray-400 font-semibold">Company B</div>
                  <div className="text-gray-400 font-semibold">Company C</div>
                  <div className="text-gray-400 font-semibold">Company D</div>
                </div>
              </div>
            </div>
          </div>

          {/* Visual/Dashboard Preview */}
          <div className="mt-16 lg:mt-0 lg:col-span-5">
            <div className="relative">
              {/* Floating Cards */}
              <div className="relative bg-white rounded-2xl shadow-2xl p-6 transform rotate-2">
                <div className="bg-primary-50 rounded-lg p-4 mb-4">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-medium text-gray-700">Survey Responses</span>
                    <span className="text-primary-600 font-bold">+24%</span>
                  </div>
                  <div className="text-2xl font-bold text-gray-900">1,247</div>
                </div>
                
                <div className="space-y-2">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-600">Completion Rate</span>
                    <span className="font-medium text-gray-900">87%</span>
                  </div>
                  <div className="w-full bg-gray-200 rounded-full h-2">
                    <div className="bg-primary-500 h-2 rounded-full" style={{ width: '87%' }}></div>
                  </div>
                </div>
              </div>

              {/* Secondary floating card */}
              <div className="absolute -top-4 -right-4 bg-white rounded-xl shadow-lg p-4 transform -rotate-3">
                <div className="flex items-center space-x-2">
                  <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                    <HiCurrencyDollar className="w-4 h-4 text-primary-600" />
                  </div>
                  <div>
                    <div className="text-xs text-gray-500">Rewards Paid</div>
                    <div className="font-bold text-gray-900">$2,847</div>
                  </div>
                </div>
              </div>

              {/* Background blur effect */}
              <div className="absolute inset-0 bg-gradient-to-tr from-primary-500/20 to-transparent rounded-2xl -z-10 transform rotate-1 scale-105"></div>
            </div>
          </div>
        </div>
      </div>
    </section>
  )
}

export default Hero