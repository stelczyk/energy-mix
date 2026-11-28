// data and chargers are in the UK - show UK time regardless of the browser's time zone
export function formatUkTime(iso: string) {
  return new Date(iso).toLocaleString('en-GB', {
    weekday: 'short',
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
    timeZone: 'Europe/London',
  })
}

// 'en-CA' formats as YYYY-MM-DD, the format <input type="date"> expects
export const todayInUk = () => new Date().toLocaleDateString('en-CA', { timeZone: 'Europe/London' })

// "00:00", "00:30", ... "23:30" - reservations start on full or half hour
export const HALF_HOURS = Array.from(
  { length: 48 },
  (_, i) => `${String(Math.floor(i / 2)).padStart(2, '0')}:${i % 2 ? '30' : '00'}`,
)

// "2026-11-29T14:30" in UK time; 'sv-SE' happens to format as YYYY-MM-DD HH:MM:SS
export function ukDateTime(ms: number) {
  return new Date(ms).toLocaleString('sv-SE', { timeZone: 'Europe/London' }).slice(0, 16).replace(' ', 'T')
}

// "2026-11-29" + 1 -> "2026-11-30"; UTC arithmetic so DST never shifts the date
export function addDays(date: string, days: number) {
  const d = new Date(`${date}T00:00:00Z`)
  d.setUTCDate(d.getUTCDate() + days)
  return d.toISOString().slice(0, 10)
}
