import { useId, useState, type FormEvent } from 'react'
import { createReservation, type Charger } from './api'
import { DayCalendar } from './DayCalendar'
import { HALF_HOURS, addDays, todayInUk } from './time'

const HOURS = [1, 2, 3, 4, 5, 6]

export interface BookingPrefill {
  date: string
  time: string
  hours: number
}

interface Props {
  chargers: Charger[]
  version: number
  initial?: BookingPrefill
  onBooked: () => void
}

export function ReservationForm({ chargers, version, initial, onBooked }: Props) {
  const [chargerId, setChargerId] = useState(chargers[0].id)
  const [date, setDate] = useState(initial?.date ?? todayInUk())
  const [time, setTime] = useState(initial?.time ?? '12:00')
  const [hours, setHours] = useState(initial?.hours ?? 1)
  const [error, setError] = useState<string | null>(null)
  const [saving, setSaving] = useState(false)
  const dateId = useId()

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
      <div className="form-row">
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
      </div>
      <div className="form-row">
        <div className="date-picker">
          <label htmlFor={dateId}>Date</label>
          <button
            type="button"
            aria-label="Previous day"
            disabled={date <= todayInUk()}
            onClick={() => setDate(addDays(date, -1))}
          >
            ‹
          </button>
          <input
            id={dateId}
            type="date"
            required
            min={todayInUk()}
            value={date}
            onChange={(e) => setDate(e.target.value)}
          />
          <button type="button" aria-label="Next day" onClick={() => setDate(addDays(date, 1))}>
            ›
          </button>
        </div>
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
      </div>
      <DayCalendar chargerId={chargerId} date={date} version={version} selected={time} onSelect={setTime} />
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
    </form>
  )
}
