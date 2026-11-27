import { Cell, Pie, PieChart, ResponsiveContainer, Tooltip } from 'recharts'
import type { DailyMix } from './api'

// fixed order = order of the colour-blind-safe palette, so neighbouring slices stay distinguishable
const FUELS = ['WIND', 'GAS', 'HYDRO', 'SOLAR', 'IMPORTS', 'BIOMASS', 'NUCLEAR', 'COAL', 'OTHER']

function toChartData(fuels: Record<string, number>) {
  return FUELS.map((fuel) => ({
    fuel,
    // UNKNOWN (fuel the API added later) folds into OTHER instead of getting a 10th colour
    value: (fuels[fuel] ?? 0) + (fuel === 'OTHER' ? (fuels.UNKNOWN ?? 0) : 0),
  })).filter((slice) => slice.value > 0)
}

export function EnergyMixChart({ day }: { day: DailyMix }) {
  // date-only string parses as UTC midnight - format in UTC so it doesn't shift a day in other time zones
  const title = new Date(day.date).toLocaleDateString('en-GB', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    timeZone: 'UTC',
  })
  const data = toChartData(day.fuels)

  return (
    <article className="day-card">
      <h2>{title}</h2>
      {day.cleanPercentage === null ? (
        <p className="note">Forecast not available yet</p>
      ) : (
        <>
          <p className="clean-value">Clean energy: {day.cleanPercentage}%</p>
          {!day.complete && <p className="note">Partial forecast – based on available hours</p>}
          <ResponsiveContainer width="100%" height={240}>
            <PieChart>
              <Pie
                data={data}
                dataKey="value"
                nameKey="fuel"
                innerRadius="55%"
                outerRadius="90%"
                paddingAngle={1}
              >
                {data.map((slice) => (
                  <Cell key={slice.fuel} className={`fuel-${slice.fuel}`} />
                ))}
              </Pie>
              <Tooltip formatter={(value) => `${value}%`} />
            </PieChart>
          </ResponsiveContainer>
          {/* exact values - tiny slices (e.g. 0.4% solar) are invisible on the chart */}
          <ul className="fuel-values">
            {data.map((slice) => (
              <li key={slice.fuel}>
                <svg width="12" height="12" aria-hidden="true">
                  <rect width="12" height="12" rx="2" className={`fuel-${slice.fuel}`} />
                </svg>
                <span>{slice.fuel.toLowerCase()}</span>
                <span className="value">{slice.value.toFixed(1)}%</span>
              </li>
            ))}
          </ul>
        </>
      )}
    </article>
  )
}
