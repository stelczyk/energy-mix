import { useState, type FormEvent } from 'react'
import { getChargingWindow, type ChargingWindow } from './api'

const HOURS = [1, 2, 3, 4, 5, 6]

// data is about the UK grid - show UK time regardless of the browser's time zone
function formatUkTime(iso: string) {
  return new Date(iso).toLocaleString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'Europe/London',
  })
}

export function ChargingWindowForm() {
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
        <p className="result">
          {formatUkTime(result.start)} – {formatUkTime(result.end)} (UK time)
          <br />
          Average clean energy: <strong>{result.cleanPercentage}%</strong>
        </p>
      )}
    </section>
  )
}
