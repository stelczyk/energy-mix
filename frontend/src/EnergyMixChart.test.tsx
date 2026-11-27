import { render, screen } from '@testing-library/react'
import { describe, expect, it } from 'vitest'
import { EnergyMixChart } from './EnergyMixChart'

describe('EnergyMixChart', () => {
  it('shows info instead of chart when there is no forecast', () => {
    render(<EnergyMixChart day={{ date: '2026-11-29', fuels: {}, cleanPercentage: null, complete: false }} />)

    screen.getByText('Forecast not available yet')
    expect(screen.queryByText(/Clean energy/)).toBeNull()
  })

  it('marks partial day and folds UNKNOWN into other', () => {
    render(
      <EnergyMixChart
        day={{
          date: '2026-11-28',
          fuels: { WIND: 60, GAS: 38.5, OTHER: 1, UNKNOWN: 0.5 },
          cleanPercentage: 60,
          complete: false,
        }}
      />,
    )

    screen.getByText('Clean energy: 60%')
    screen.getByText(/Partial forecast/)
    screen.getByText('1.5%') // OTHER 1 + UNKNOWN 0.5
    expect(screen.queryByText('unknown')).toBeNull()
  })

  it('does not mark complete day as partial', () => {
    render(<EnergyMixChart day={{ date: '2026-11-27', fuels: { WIND: 100 }, cleanPercentage: 100, complete: true }} />)

    expect(screen.queryByText(/Partial forecast/)).toBeNull()
  })
})
