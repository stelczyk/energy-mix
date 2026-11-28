import { useEffect, useState } from 'react'
import { getOccupiedSlots, type OccupiedSlot } from './api'
import { HALF_HOURS, ukDateTime } from './time'

const HALF_HOUR_MS = 30 * 60 * 1000

// which half-hours of the given UK day are taken - "14:30" -> 'mine' | 'booked'
// ponytail: grid always has 48 half-hours, so on the 2 DST-change nights one hour shows imperfectly
function takenHalfHours(slots: OccupiedSlot[], date: string) {
  const taken = new Map<string, 'mine' | 'booked'>()
  for (const slot of slots) {
    for (let ms = Date.parse(slot.startTime); ms < Date.parse(slot.endTime); ms += HALF_HOUR_MS) {
      const [day, time] = ukDateTime(ms).split('T')
      if (day === date) taken.set(time, slot.mine ? 'mine' : 'booked')
    }
  }
  return taken
}

interface Props {
  chargerId: number
  date: string
  version: number
  selected: string
  onSelect: (time: string) => void
}

export function DayCalendar({ chargerId, date, version, selected, onSelect }: Props) {
  const [slots, setSlots] = useState<OccupiedSlot[]>([])
  const [error, setError] = useState<string | null>(null)
  // read the clock with the data, not on every render - past slots refresh together with bookings
  const [now, setNow] = useState(() => ukDateTime(Date.now()))

  useEffect(() => {
    getOccupiedSlots(chargerId, date)
      .then((data) => {
        setSlots(data)
        setNow(ukDateTime(Date.now()))
      })
      .catch((e: Error) => setError(e.message))
  }, [chargerId, date, version])

  const taken = takenHalfHours(slots, date)

  return (
    <div className="availability">
      {error && (
        <p role="alert" className="error">
          {error}
        </p>
      )}
      <div className="day-calendar">
        {HALF_HOURS.map((time) => {
          // past wins - matches "My reservations", which lists only upcoming bookings
          const state = `${date}T${time}` < now ? 'past' : (taken.get(time) ?? 'free')
          return (
            <button
              key={time}
              type="button"
              disabled={state !== 'free'}
              className={`slot slot-${state}${time === selected ? ' slot-selected' : ''}`}
              aria-label={`${time} ${state}`}
              onClick={() => onSelect(time)}
            >
              {time}
            </button>
          )
        })}
      </div>
    </div>
  )
}
