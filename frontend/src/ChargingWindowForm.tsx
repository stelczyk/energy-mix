import { useState, type FormEvent } from 'react'
import { getChargingWindow, type ChargingWindow } from './api'
import type { BookingPrefill } from './ReservationForm'
import { formatUkTime, ukDateTime } from './time'

const HOURS = [1, 2, 3, 4, 5, 6]

// window start (UTC) -> UK date + time for the booking form; hours from the window itself
function toPrefill(window: ChargingWindow): BookingPrefill {
  const [date, time] = ukDateTime(Date.parse(window.start)).split('T')
  return { date, time, hours: (Date.parse(window.end) - Date.parse(window.start)) / 3_600_000 }
}

export function ChargingWindowForm({ onBook }: { onBook?: (prefill: BookingPrefill) => void }) {
  const [hours, setHours] = useState(3)
  const [result, setResult] = useState<ChargingWindow | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  async function handleSubmit(event: FormEvent) {
    event.preventDefault() // no full page reload on submit
    setLoading(true)
    setError(null)
    setResult(null)
    try {
      setResult(await getChargingWindow(hours))
    } catch (e) {
      setError((e as Error).message)
    } finally {
      setLoading(false)
    }
  }

  return (
    <section className="charging-window">
      <h2>Best time to charge</h2>
      <p className="note">Searches the forecast for tomorrow and the day after.</p>
      <form onSubmit={handleSubmit}>
        <label>
          Charging time
          <select value={hours} onChange={(e) => setHours(Number(e.target.value))}>
            {HOURS.map((h) => (
              <option key={h} value={h}>
                {h} h
              </option>
            ))}
          </select>
        </label>
        <button type="submit" disabled={loading}>
          {loading ? 'Searching…' : 'Find cleanest window'}
        </button>
      </form>
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      {result && (
        <>
          <p className="result">
            {formatUkTime(result.start)} – {formatUkTime(result.end)} (UK time)
            <br />
            Average clean energy: <strong>{result.cleanPercentage}%</strong>
          </p>
          {onBook && (
            <button type="button" onClick={() => onBook(toPrefill(result))}>
              Book this window
            </button>
          )}
        </>
      )}
    </section>
  )
}
