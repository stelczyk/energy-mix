import { useEffect, useState } from 'react'
import { getDailyMix, type DailyMix } from './api'
import { EnergyMixChart } from './EnergyMixChart'

export default function App() {
  const [days, setDays] = useState<DailyMix[]>([])
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    getDailyMix().then(setDays).catch((e: Error) => setError(e.message))
  }, [])

  return (
    <main>
      <h1>UK Energy Mix</h1>
      {error && <p role="alert">{error}</p>}
      <section className="days">
        {days.map((day) => (
          <EnergyMixChart key={day.date} day={day} />
        ))}
      </section>
    </main>
  )
}
