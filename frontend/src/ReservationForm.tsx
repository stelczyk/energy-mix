import { useState, type FormEvent } from 'react'
import { createReservation, type Charger } from './api'
import { HALF_HOURS, todayInUk } from './time'

const HOURS = [1, 2, 3, 4, 5, 6]

export function ReservationForm({ chargers, onBooked }: { chargers: Charger[]; onBooked: () => void }) {
  const [chargerId, setChargerId] = useState(chargers[0].id)
  const [date, setDate] = useState(todayInUk())
  const [time, setTime] = useState('12:00')
  const [hours, setHours] = useState(1)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault()
    setSaving(true)
    setError(null)
    try {
      await createReservation({ chargerId, startTime: `${date}T${time}:00`, hours })
      onBooked()
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <form className="reservation-form" onSubmit={handleSubmit}>
      <label>
        Charger
        <select value={chargerId} onChange={(e) => setChargerId(Number(e.target.value))}>
          {chargers.map((charger) => (
            <option key={charger.id} value={charger.id}>
              {charger.name} – {charger.location}
            </option>
          ))}
        </select>
      </label>
      <label>
        Date
        <input type="date" required min={todayInUk()} value={date} onChange={(e) => setDate(e.target.value)} />
      </label>
      <label>
        Start (UK time)
        <select value={time} onChange={(e) => setTime(e.target.value)}>
          {HALF_HOURS.map((t) => (
            <option key={t} value={t}>
              {t}
            </option>
          ))}
        </select>
      </label>
      <label>
        Duration
        <select value={hours} onChange={(e) => setHours(Number(e.target.value))}>
          {HOURS.map((h) => (
            <option key={h} value={h}>
              {h} h
            </option>
          ))}
        </select>
      </label>
      <button type="submit" disabled={saving}>
        {saving ? 'Booking…' : 'Book'}
      </button>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
    </form>
  )
}
