import { atTime, toIsoDate } from '../utils/dates'

// Los mocks usan fechas relativas a hoy para que siempre haya datos para mostrar
export function daysFromToday(days: number): string {
  const date = new Date()
  date.setDate(date.getDate() + days)
  return toIsoDate(date)
}

export const at = (days: number, time: string) =>
  atTime(daysFromToday(days), time)
