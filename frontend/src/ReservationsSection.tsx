import { useEffect, useState } from 'react'
import { cancelReservation, getChargers, getMyReservations, type Charger, type Reservation } from './api'
import { ReservationForm } from './ReservationForm'
import { formatUkTime } from './time'

export function ReservationsSection() {
  const [chargers, setChargers] = useState<Charger[]>([])
  const [reservations, setReservations] = useState<Reservation[]>([])
  const [error, setError] = useState<string | null>(null)
  const [version, setVersion] = useState(0)

  const loadReservations = () =>
    getMyReservations()
      .then(setReservations)
      .catch((e: Error) => setError(e.message))

  // reload own list and the calendar after booking or cancelling
  const refresh = () => {
    loadReservations()
    setVersion((v) => v + 1)
  }

  useEffect(() => {
    getChargers()
      .then(setChargers)
      .catch((e: Error) => setError(e.message))
    loadReservations()
  }, [])

  async function handleCancel(reservation: Reservation) {
    if (!window.confirm(`Cancel booking at ${reservation.chargerName}?`)) return
    try {
      await cancelReservation(reservation.id)
      refresh()
    } catch (e) {
      setError((e as Error).message)
    }
  }

  return (
    <section className="reservations">
      <h2>Book a charger</h2>
      {chargers.length > 0 && <ReservationForm chargers={chargers} version={version} onBooked={refresh} />}
      <h3>My reservations (UK time)</h3>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      {reservations.length === 0 ? (
        <p className="note">No upcoming reservations.</p>
      ) : (
        <ul className="reservation-list">
          {reservations.map((reservation) => (
            <li key={reservation.id}>
              <span>
                <strong>{reservation.chargerName}</strong>: {formatUkTime(reservation.startTime)} –{' '}
                {formatUkTime(reservation.endTime)}
              </span>
              <button type="button" onClick={() => handleCancel(reservation)}>
                Cancel
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}
