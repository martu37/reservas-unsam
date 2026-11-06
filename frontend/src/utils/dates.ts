// Fechas como texto local, igual que un LocalDateTime de Spring:
// fecha 'YYYY-MM-DD' y fecha-hora 'YYYY-MM-DDTHH:mm:ss'.
// Al tener siempre el mismo formato se pueden comparar como strings.

const pad = (value: number) => String(value).padStart(2, '0')

export const toIsoDate = (date: Date) => date.toLocaleDateString('en-CA')

export const toIsoDateTime = (date: Date) =>
  `${toIsoDate(date)}T${pad(date.getHours())}:${pad(date.getMinutes())}:00`

export const today = () => toIsoDate(new Date())

export const now = () => toIsoDateTime(new Date())

export const atTime = (isoDate: string, time: string) => `${isoDate}T${time}:00`

export const addMinutes = (isoDateTime: string, minutes: number) => {
  const date = new Date(isoDateTime)
  date.setMinutes(date.getMinutes() + minutes)
  return toIsoDateTime(date)
}

export const datePart = (isoDateTime: string) => isoDateTime.slice(0, 10)

export const formatDate = (isoDate: string) =>
  datePart(isoDate).split('-').reverse().join('/')

export const formatTime = (isoDateTime: string) => isoDateTime.slice(11, 16)

export const formatDateTime = (isoDateTime: string) =>
  `${formatDate(isoDateTime)} ${formatTime(isoDateTime)}`

// 'viernes 6/11'
export const formatDayName = (isoDate: string) => {
  const date = new Date(`${datePart(isoDate)}T00:00:00`)
  const weekday = date.toLocaleDateString('es-AR', { weekday: 'long' })
  return `${weekday} ${date.getDate()}/${date.getMonth() + 1}`
}

// Un <input type="datetime-local"> devuelve 'YYYY-MM-DDTHH:mm', sin segundos
export const fromDateTimeInput = (value: string) =>
  value.length === 16 ? `${value}:00` : value

export const toDateTimeInput = (isoDateTime: string) => isoDateTime.slice(0, 16)
