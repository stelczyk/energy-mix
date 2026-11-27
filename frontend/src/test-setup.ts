import { cleanup } from '@testing-library/react'
import { afterEach } from 'vitest'

// unmount rendered components after each test so tests don't see each other's DOM
afterEach(cleanup)

// jsdom has no ResizeObserver, which Recharts' ResponsiveContainer needs
globalThis.ResizeObserver = class {
  observe() {}
  unobserve() {}
  disconnect() {}
}
