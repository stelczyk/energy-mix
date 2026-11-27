import { getClientId } from './clientId'

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

export interface Charger {
  id: number
  name: string
  location: string
}

export interface Reservation {
  id: number
  chargerId: number
  chargerName: string
  startTime: string
  endTime: string
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response
  try {
    response = await fetch(`${API_URL}${path}`, {
      ...init,
      headers: { 'Content-Type': 'application/json', 'X-Client-Id': getClientId() },
    })
  } catch {
    // fetch throws only when the server can't be reached at all (down, offline, CORS)
    throw new Error('Cannot reach the server. Check your connection and try again.')
  }
  if (!response.ok) {
    // backend returns ProblemDetail - show its "detail" to the user
    const problem = await response.json().catch(() => null)
    throw new Error(problem?.detail ?? `Request failed (${response.status})`)
  }
  return response.status === 204 ? (undefined as T) : response.json() // DELETE returns no body
}

export const getDailyMix = () => request<DailyMix[]>('/api/v1/energy/mix')

export const getChargingWindow = (hours: number) =>
  request<ChargingWindow>(`/api/v1/energy/charging-window?hours=${hours}`)

export const getChargers = () => request<Charger[]>('/api/v1/chargers')

export const getMyReservations = () => request<Reservation[]>('/api/v1/reservations')

// startTime is UK local time, e.g. "2026-11-29T14:30:00"
export const createReservation = (body: { chargerId: number; startTime: string; hours: number }) =>
  request<Reservation>('/api/v1/reservations', { method: 'POST', body: JSON.stringify(body) })

export const cancelReservation = (id: number) =>
  request<void>(`/api/v1/reservations/${id}`, { method: 'DELETE' })

export interface OccupiedSlot {
  startTime: string
  endTime: string
  mine: boolean
}

export const getOccupiedSlots = (chargerId: number, date: string) =>
  request<OccupiedSlot[]>(`/api/v1/chargers/${chargerId}/reservations?date=${date}`)
