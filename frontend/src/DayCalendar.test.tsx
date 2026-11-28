import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { getOccupiedSlots } from './api'
import { DayCalendar } from './DayCalendar'

vi.mock('./api')

describe('DayCalendar', () => {
  afterEach(() => vi.resetAllMocks())

  it('marks own and booked slots and lets user pick a free one', async () => {
    // winter date: UK time = UTC
    vi.mocked(getOccupiedSlots).mockResolvedValue([
      { startTime: '2099-01-01T10:00:00Z', endTime: '2099-01-01T11:00:00Z', mine: true },
      { startTime: '2099-01-01T12:00:00Z', endTime: '2099-01-01T12:30:00Z', mine: false },
    ])
    const onSelect = vi.fn()
    const user = userEvent.setup()
    render(<DayCalendar chargerId={1} date="2099-01-01" version={0} selected="" onSelect={onSelect} />)

    expect((await screen.findByRole('button', { name: '10:30 mine' })).hasAttribute('disabled')).toBe(true)
    expect(screen.getByRole('button', { name: '12:00 booked' }).hasAttribute('disabled')).toBe(true)
    screen.getByRole('button', { name: '11:00 free' }) // reservation end is exclusive

    await user.click(screen.getByRole('button', { name: '14:00 free' }))
    expect(onSelect).toHaveBeenCalledWith('14:00')
  })

  it('shows past slots as past, even when they were booked', async () => {
    vi.mocked(getOccupiedSlots).mockResolvedValue([
      { startTime: '2000-01-01T10:00:00Z', endTime: '2000-01-01T11:00:00Z', mine: true },
    ])
    render(<DayCalendar chargerId={1} date="2000-01-01" version={0} selected="" onSelect={vi.fn()} />)

    expect((await screen.findByRole('button', { name: '10:00 past' })).hasAttribute('disabled')).toBe(true)
    expect(screen.getByRole('button', { name: '14:00 past' }).hasAttribute('disabled')).toBe(true)
  })
})
