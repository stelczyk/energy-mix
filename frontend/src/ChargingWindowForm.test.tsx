import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { getChargingWindow } from './api'
import { ChargingWindowForm } from './ChargingWindowForm'

vi.mock('./api') // replace real HTTP calls with mocks

describe('ChargingWindowForm', () => {
  afterEach(() => vi.resetAllMocks())

  it('requests selected hours and shows window in UK time', async () => {
    // summer date: UK = UTC+1, so 11:00Z must be shown as 12:00
    vi.mocked(getChargingWindow).mockResolvedValue({
      start: '2026-07-02T11:00:00Z',
      end: '2026-07-02T13:00:00Z',
      cleanPercentage: 81.5,
    })
    const user = userEvent.setup()
    render(<ChargingWindowForm />)

    await user.selectOptions(screen.getByLabelText('Charging time'), '2')
    await user.click(screen.getByRole('button', { name: 'Find cleanest window' }))

    expect(getChargingWindow).toHaveBeenCalledWith(2)
    await screen.findByText(/12:00 – .*14:00 \(UK time\)/)
    screen.getByText('81.5%')
  })

  it('shows error message when request fails', async () => {
    vi.mocked(getChargingWindow).mockRejectedValue(new Error('Not enough forecast data'))
    const user = userEvent.setup()
    render(<ChargingWindowForm />)

    await user.click(screen.getByRole('button', { name: 'Find cleanest window' }))

    expect((await screen.findByRole('alert')).textContent).toBe('Not enough forecast data')
  })

  it('passes UK date, time and hours of the window to booking', async () => {
    // summer: 11:00Z is 12:00 in the UK
    vi.mocked(getChargingWindow).mockResolvedValue({
      start: '2026-07-02T11:00:00Z',
      end: '2026-07-02T13:00:00Z',
      cleanPercentage: 81.5,
    })
    const onBook = vi.fn()
    const user = userEvent.setup()
    render(<ChargingWindowForm onBook={onBook} />)

    await user.click(screen.getByRole('button', { name: 'Find cleanest window' }))
    await user.click(await screen.findByRole('button', { name: 'Book this window' }))

    expect(onBook).toHaveBeenCalledWith({ date: '2026-07-02', time: '12:00', hours: 2 })
  })
})
