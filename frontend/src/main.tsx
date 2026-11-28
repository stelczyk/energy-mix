import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'

// apply saved theme before the first render - no flash of the wrong theme
const savedTheme = localStorage.getItem('theme')
if (savedTheme) document.documentElement.dataset.theme = savedTheme

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
