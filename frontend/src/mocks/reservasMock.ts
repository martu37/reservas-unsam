import type { BloqueoJSON } from '../domain/Bloqueo'
import type { ReservaJSON } from '../domain/Reserva'
import { at } from './dates'

export type ReservaFila = Omit<ReservaJSON, 'espacioNombre'>
export type BloqueoFila = Omit<BloqueoJSON, 'espacioNombre'>

const confirmada = (
  id: number,
  usuarioId: number,
  espacioId: number,
  dia: number,
  desde: string,
  hasta: string,
): ReservaFila => ({
  id,
  usuarioId,
  espacioId,
  inicio: at(dia, desde),
  fin: at(dia, hasta),
  estado: 'confirmada',
  creadaEn: at(Math.min(dia - 2, -1), '10:15'),
  canceladaEn: null,
})

export const reservasMock: ReservaFila[] = [
  confirmada(1, 1, 1, 1, '18:00', '19:00'),
  confirmada(2, 2, 4, 2, '10:00', '14:00'),
  confirmada(3, 3, 3, 1, '19:30', '21:00'),
  {
    ...confirmada(4, 4, 5, 2, '10:00', '14:00'),
    estado: 'cancelada',
    canceladaEn: at(0, '08:30'),
  },
  confirmada(5, 1, 2, 3, '18:00', '19:00'),
  confirmada(6, 6, 2, 1, '18:00', '19:00'),
  confirmada(7, 1, 1, -3, '18:00', '19:00'),
  confirmada(8, 7, 4, 4, '14:00', '18:00'),
  confirmada(9, 5, 1, 0, '13:00', '14:00'),
  confirmada(10, 1, 1, 0, '21:00', '22:00'),
  confirmada(11, 2, 2, 0, '17:00', '18:00'),
]

export const bloqueosMock: BloqueoFila[] = [
  {
    id: 1,
    espacioId: 6,
    inicio: at(-1, '00:00'),
    fin: at(10, '00:00'),
    motivo: 'Cambio de rejilla',
    creadoPor: 5,
    creadoEn: at(-2, '09:00'),
  },
  {
    id: 2,
    espacioId: 2,
    inicio: at(2, '09:00'),
    fin: at(2, '13:00'),
    motivo: 'Corte de pasto',
    creadoPor: 5,
    creadoEn: at(-1, '11:20'),
  },
]
