import { useEffect, useState } from 'react'
import { getDailyMix, type DailyMix } from './api'
import { ChargingWindowForm } from './ChargingWindowForm'
import { EnergyMixChart } from './EnergyMixChart'
import type { BookingPrefill } from './ReservationForm'
import { ReservationsSection } from './ReservationsSection'

export default function App() {
  const [days, setDays] = useState<DailyMix[]>([])
  const [error, setError] = useState<string | null>(null)
  const [prefill, setPrefill] = useState<BookingPrefill | null>(null)

  useEffect(() => {
    getDailyMix().then(setDays).catch((e: Error) => setError(e.message))
  }, [])

  function handleBookWindow(next: BookingPrefill) {
    setPrefill(next)
    document.getElementById('book')?.scrollIntoView({ behavior: 'smooth' })
  }

  return (
    <main>
      <h1>UK Energy Mix</h1>
      {error && <p role="alert" className="error">{error}</p>}
      <section className="days">
        {days.map((day) => (
          <EnergyMixChart key={day.date} day={day} />
        ))}
      </section>
      <ChargingWindowForm onBook={handleBookWindow} />
      <ReservationsSection prefill={prefill} />
    </main>
  )
}
