import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createReservation, getOccupiedSlots } from './api'
import { ReservationForm } from './ReservationForm'

vi.mock('./api')

const CHARGERS = [{ id: 1, name: "King's Cross Hub", location: 'London' }]
const PREFILL = { date: '2099-01-01', time: '14:30', hours: 2 }

describe('ReservationForm', () => {
  beforeEach(() => vi.mocked(getOccupiedSlots).mockResolvedValue([]))
  afterEach(() => vi.resetAllMocks())

  it('sends start as UK local time and reports success', async () => {
    vi.mocked(createReservation).mockResolvedValue({
      id: 1,
      chargerId: 1,
      chargerName: "King's Cross Hub",
      startTime: '2099-01-01T14:30:00Z',
      endTime: '2099-01-01T16:30:00Z',
    })
    const onBooked = vi.fn()
    const user = userEvent.setup()
    render(<ReservationForm chargers={CHARGERS} version={0} initial={PREFILL} onBooked={onBooked} />)

    await user.click(screen.getByRole('button', { name: 'Book' }))

    expect(createReservation).toHaveBeenCalledWith({ chargerId: 1, startTime: '2099-01-01T14:30:00', hours: 2 })
    expect(onBooked).toHaveBeenCalled()
  })

  it('shows conflict message from backend', async () => {
    vi.mocked(createReservation).mockRejectedValue(new Error('This charger is already booked for the selected time.'))
    const user = userEvent.setup()
    render(<ReservationForm chargers={CHARGERS} version={0} initial={PREFILL} onBooked={vi.fn()} />)

    await user.click(screen.getByRole('button', { name: 'Book' }))

    expect((await screen.findByRole('alert')).textContent).toBe('This charger is already booked for the selected time.')
  })
})
