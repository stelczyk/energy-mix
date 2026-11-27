const API_URL = import.meta.env.VITE_API_URL

export interface DailyMix {
  date: string
  fuels: Record<string, number>
  cleanPercentage: number | null // null = no forecast for this day yet
  complete: boolean // false = some half-hours missing
}

export interface ChargingWindow {
  start: string
  end: string
  cleanPercentage: number
}

async function getJson<T>(path: string): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API_URL}${path}`)
  } catch {
    // fetch throws only when the server can't be reached at all (down, offline, CORS)
    throw new Error('Cannot reach the server. Check your connection and try again.')
  }
  if (!response.ok) {
    // backend returns ProblemDetail - show its "detail" to the user
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `Request failed (${response.status})`)
  }
  return response.json()
}

export const getDailyMix = () => getJson<DailyMix[]>('/api/v1/energy/mix')

export const getChargingWindow = (hours: number) =>
  getJson<ChargingWindow>(`/api/v1/energy/charging-window?hours=${hours}`)
