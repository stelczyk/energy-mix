import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, describe, expect, it, vi } from 'vitest'
import { cancelReservation, getChargers, getMyReservations, getOccupiedSlots } from './api'
import { ReservationsSection } from './ReservationsSection'

vi.mock('./api')

const RESERVATION = {
  id: 7,
  chargerId: 1,
  chargerName: "King's Cross Hub",
  startTime: '2099-01-01T10:00:00Z',
  endTime: '2099-01-01T11:00:00Z',
}

describe('ReservationsSection', () => {
  afterEach(() => vi.restoreAllMocks())

  it('cancels reservation after confirmation and reloads list', async () => {
    vi.spyOn(window, 'confirm').mockReturnValue(true) // jsdom has no real dialog
    vi.mocked(getChargers).mockResolvedValue([{ id: 1, name: "King's Cross Hub", location: 'London' }])
    vi.mocked(getOccupiedSlots).mockResolvedValue([])
    vi.mocked(getMyReservations).mockResolvedValueOnce([RESERVATION]).mockResolvedValueOnce([])
    vi.mocked(cancelReservation).mockResolvedValue(undefined)
    const user = userEvent.setup()
    render(<ReservationsSection prefill={null} />)

    await user.click(await screen.findByRole('button', { name: 'Cancel' }))

    expect(cancelReservation).toHaveBeenCalledWith(7)
    await screen.findByText('No upcoming reservations.')
  })
})
