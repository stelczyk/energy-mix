import { useState } from 'react'

type Theme = 'system' | 'light' | 'dark'
const KEY = 'theme'

// sets data-theme on <html>; CSS picks colours through color-scheme + light-dark()
function applyTheme(theme: Theme) {
  if (theme === 'system') {
    localStorage.removeItem(KEY)
    delete document.documentElement.dataset.theme
  } else {
    localStorage.setItem(KEY, theme)
    document.documentElement.dataset.theme = theme
  }
}

export function ThemeSelect() {
  const [theme, setTheme] = useState<Theme>(() => (localStorage.getItem(KEY) as Theme | null) ?? 'system')

  function handleChange(next: Theme) {
    setTheme(next)
    applyTheme(next)
  }

  return (
    <label className="theme-select">
      Theme
      <select value={theme} onChange={(e) => handleChange(e.target.value as Theme)}>
        <option value="system">System</option>
        <option value="light">Light</option>
        <option value="dark">Dark</option>
      </select>
    </label>
  )
}
