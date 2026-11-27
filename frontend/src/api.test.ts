import { afterEach, describe, expect, it, vi } from 'vitest'
import { getChargingWindow } from './api'

describe('api', () => {
  afterEach(() => vi.unstubAllGlobals())

  it('shows backend ProblemDetail message when request fails', async () => {
    vi.stubGlobal(
      'fetch',
      vi.fn().mockResolvedValue(new Response(JSON.stringify({ detail: 'Not enough forecast data' }), { status: 503 })),
    )

    await expect(getChargingWindow(3)).rejects.toThrow('Not enough forecast data')
  })

  it('shows friendly message when server cannot be reached', async () => {
    vi.stubGlobal('fetch', vi.fn().mockRejectedValue(new TypeError('Failed to fetch')))

    await expect(getChargingWindow(3)).rejects.toThrow('Cannot reach the server')
  })
})
