## Purpose

This file gives concise, actionable guidance for AI coding agents working on this Vite + React frontend (located at the repository root). Focus on making small, buildable changes that follow the repository's patterns.

## Quick facts
- Project type: Vite + React (JSX). Entry: `src/main.jsx` -> `src/App.jsx`.
- Scripts (see `package.json`): `dev` (vite), `build` (vite build), `preview` (vite preview), `lint` (eslint .).
- Vite plugin: `@vitejs/plugin-react` configured in `vite.config.js`.
- Linting: ESLint configured in `eslint.config.js`. Note rule: `no-unused-vars` ignores names matching `^[A-Z_]`.

## File & pattern highlights
- `index.html` contains the root element `<div id="root"></div>` and includes `/src/main.jsx`.
- `src/main.jsx` uses `createRoot(...).render(<StrictMode><App/></StrictMode>)` — follow this bootstrapping pattern for single-entry changes.
- `src/App.jsx` demonstrates two asset import styles:
  - Absolute public imports, e.g. `import viteLogo from '/vite.svg'` (served from project root / `public/`)
  - Relative imports, e.g.`import reactLogo from './assets/react.svg'`
  Preserve the existing usage: public assets can be referenced with leading `/` while component assets live under `src/`.

## Conventions and expectations

### Structure (src)
- `src/components`: navbar.jsx, footer.jsx, and other shared UI pieces.
- `src/pages`: page-level components for routing, e.g. home.jsx, login.jsx.
- `src/assets`: images, icons, and static files imported by components.
- `src/utils`: helper functions (if required) etc.
- `src/services`: API interaction logic, data fetching, etc. Note: use axios library for HTTP requests.
- Naming: PascalCase for React components and files; the ESLint rule that ignores `^[A-Z_]` means intentionally unused exported names that start with a capital will be allowed.

### Styling
- Use flowbite-react, which is a tailwind css component library
- Use the component library for hero, buttons, navbar, carousel, dashboard, etc. In other words, for every component / page
- Flowbite-react has already been added as a dependency

### State Management
- For server-side, use react-query (alongside axios) to manage server state, caching, and data synchronization i.e lifecycle of API calls.
- For client-side state, use zustand for simple and scalable state management.
- Both react-query and zustand have already been added as dependencies.

### API calls
- To be handled using `src/services/apiServices.js`

### Real-Time Dashboard Analytics
- Recharts is preferred. Data will be served from the backend using SSE.
- The dependency has been added.

## How to run / debug
- Start development server with: `npm run dev` (Vite, HMR enabled). Editing `src/App.jsx` demonstrates HMR.
- Build for production: `npm run build` then `npm run preview` to serve the build locally.
- Lint: `npm run lint` runs ESLint using the project config.

If running commands in CI or locally, ensure dependencies are installed (`npm install`) before running scripts.

## Integration points & external deps
- Current deps are minimal: `react`, `react-dom`, `vite`, `@vitejs/plugin-react`, `eslint`, `flowbite-react`, and their dependencies.
- To add new dependencies, update `package.json` and run `npm install`.
- Environment variables: Vite supports `.env` files. Use `import.meta.env.VITE_*` for runtime config.

## PR guidance for AI-generated changes
- Keep changes small and focused. Each PR should:
  1. Change at most a few files (new component + import, or fix a bug).
 2. Be buildable: run `npm run dev` or `npm run build` locally in your head — avoid references to missing packages or files.
 3. Prefer to update `README.md` or top-level comments if introducing new developer-visible behavior.

## Where to look for examples
- Bootstrapping & entry: `src/main.jsx`.
- Component + asset usage: `src/App.jsx`.
- Build config: `vite.config.js`.
- Lint rules and exceptions: `eslint.config.js`.

## When uncertain
- If you need project conventions not present here, open `package.json`, `vite.config.js`, and `eslint.config.js` first — they contain the canonical scripts, plugins, and lint rules.

---
If any section is unclear or you want more examples (routing, state management patterns, or adding tests), tell me which area to expand and I'll update this file.
