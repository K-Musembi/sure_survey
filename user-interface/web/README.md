# SureSurvey - Survey & Rewards Platform

A modern survey platform built with React + Vite that allows users to create surveys with built-in reward systems and real-time analytics.

## Features

- **Survey Builder**: Step-by-step survey creation with templates
- **Multiple Survey Types**: NPS, CES, CSAT support  
- **Real-time Analytics**: Live dashboards with SSE integration
- **Reward System**: Integrated payments via Paystack
- **Responsive Design**: Mobile-first with Tailwind CSS + Flowbite
- **Authentication**: JWT-based auth with httpOnly cookies

## Tech Stack

- **Frontend**: React 19, Vite, TypeScript
- **UI**: Tailwind CSS, Flowbite React
- **State**: Zustand, React Query
- **Charts**: Recharts
- **HTTP**: Axios
- **Payments**: Paystack

## Quick Start

### Prerequisites
- Node.js 18+
- Backend API running on port 8080

### Installation

```bash
# Install dependencies
npm install

# Copy environment variables
cp .env.example .env

# Update .env with your Paystack public key
# VITE_PAYSTACK_PUBLIC_KEY=pk_test_your_actual_key

# Start development server
npm run dev
```

### Available Scripts

```bash
npm run dev      # Start development server
npm run build    # Build for production  
npm run preview  # Preview production build
npm run lint     # Run ESLint
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API URL | `http://localhost:8080` |
| `VITE_API_VERSION` | API version | `v1` |
| `VITE_PAYSTACK_PUBLIC_KEY` | Paystack public key | Required for payments |
| `VITE_APP_ENV` | Environment | `development` |

## Project Structure

```
src/
├── components/          # Reusable UI components
│   ├── NavBar.jsx      # Navigation component
│   ├── Hero.jsx        # Landing page hero
│   ├── Footer.jsx      # Footer component
│   ├── AnalyticsDashboard.jsx  # Charts and analytics
│   ├── PaymentModal.jsx        # Paystack integration
│   ├── ProtectedRoute.jsx      # Auth guard
│   └── PublicRoute.jsx         # Public route guard
├── pages/              # Page-level components
│   ├── Home.jsx        # Landing page
│   ├── Login.jsx       # Login form
│   ├── Signup.jsx      # Registration form
│   ├── Dashboard.jsx   # Main dashboard
│   ├── Profile.jsx     # User profile
│   ├── SurveyBuilder.jsx       # Survey creation wizard
│   └── SurveySession.jsx       # Public survey taking
├── hooks/              # React hooks
│   └── useApi.js       # API integration hooks
├── services/           # API services
│   └── apiServices.js  # Axios client and endpoints
├── stores/             # Zustand stores
│   ├── authStore.js    # Authentication state
│   └── surveyStore.js  # Survey builder state
└── lib/                # Utilities
    └── queryClient.js  # React Query configuration
```

## Usage

### Creating a Survey

1. **Sign up/Login**: Create account or sign in
2. **Dashboard**: Click "Create Survey" 
3. **Survey Builder**: 
   - Choose survey type (NPS/CES/CSAT)
   - Select template or create custom
   - Add/edit questions
   - Configure settings
   - Set up rewards (optional)
4. **Activate**: Pay for activation if required
5. **Share**: Copy survey URL and distribute

### Survey URLs

- Long form: `/survey/{id}`
- Short form: `/s/{shortCode}` (better UX for sharing)

### Taking a Survey

1. Click survey link
2. Optionally register for rewards
3. Answer questions one-by-one
4. Submit and receive confirmation
5. Rewards processed automatically

### Analytics

- **Real-time**: Live response tracking via SSE
- **Historical**: Historical data and trends
- **Charts**: Response timeline, satisfaction distribution, NPS breakdown
- **Metrics**: Response count, completion rate, average time

## Development

### State Management

- **Authentication**: `useAuthStore()` - user state, login/logout
- **Survey Builder**: `useSurveyStore()` - form state, templates
- **API**: React Query for server state and caching

### API Integration

All API calls use the `apiServices.js` client with:
- Automatic JWT handling via httpOnly cookies
- Request/response interceptors
- Error handling and retries
- Environment-based configuration

### Styling

- **Theme**: Lime green primary (`#84cc16`)
- **Components**: Flowbite React component library
- **Utilities**: Tailwind CSS for custom styling
- **Responsive**: Mobile-first design approach

## Deployment

```bash
# Build for production
npm run build

# Test production build locally
npm run preview
```

The build outputs to `dist/` and can be deployed to any static hosting service.

## Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)  
5. Open Pull Request

## Support

For support, email support@suresurvey.com or create an issue.

## License

This project is licensed under the MIT License.