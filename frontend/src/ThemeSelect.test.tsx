import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { ThemeSelect } from './ThemeSelect'

describe('ThemeSelect', () => {
  it('applies and remembers chosen theme, system removes it', async () => {
    const user = userEvent.setup()
    render(<ThemeSelect />)

    await user.selectOptions(screen.getByLabelText('Theme'), 'dark')
    expect(document.documentElement.dataset.theme).toBe('dark')
    expect(localStorage.getItem('theme')).toBe('dark')

    await user.selectOptions(screen.getByLabelText('Theme'), 'system')
    expect(document.documentElement.dataset.theme).toBeUndefined()
    expect(localStorage.getItem('theme')).toBeNull()
  })
})
