import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import { QueryClientProvider } from '@tanstack/react-query'
import { ThemeProvider as FlowbiteThemeProvider } from 'flowbite-react'
import { queryClient } from './lib/queryClient'
import { ThemeProvider } from './theme/themeContext'
import flowbiteTheme from './theme/flowbiteTheme'
import './index.css'
import App from './App.jsx'

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <ThemeProvider>
        <FlowbiteThemeProvider value={{ theme: flowbiteTheme }}>
          <BrowserRouter>
            <App />
          </BrowserRouter>
        </FlowbiteThemeProvider>
      </ThemeProvider>
    </QueryClientProvider>
  </StrictMode>,
)
