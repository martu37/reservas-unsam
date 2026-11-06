import type { EventoHistorialJSON } from '../domain/EventoHistorial'
import { at } from './dates'

// Simula la colección "eventos" de MongoDB: cada documento tiene un detalle distinto
export const historialMock: EventoHistorialJSON[] = [
  {
    id: 'ev-1',
    usuarioId: 1,
    tipo: 'reserva_creada',
    fecha: at(-5, '10:15'),
    detalle: {
      reservaId: 7,
      espacio: 'Cancha 1',
      inicio: at(-3, '18:00'),
      fin: at(-3, '19:00'),
    },
  },
  {
    id: 'ev-2',
    usuarioId: 1,
    tipo: 'reserva_rechazada',
    fecha: at(-1, '09:40'),
    detalle: {
      espacio: 'Parrilla 3',
      inicio: at(1, '10:00'),
      fin: at(1, '14:00'),
      motivo: 'El espacio está bloqueado: Cambio de rejilla',
    },
  },
  {
    id: 'ev-3',
    usuarioId: 1,
    tipo: 'reserva_creada',
    fecha: at(-1, '10:15'),
    detalle: {
      reservaId: 1,
      espacio: 'Cancha 1',
      inicio: at(1, '18:00'),
      fin: at(1, '19:00'),
    },
  },
  {
    id: 'ev-4',
    usuarioId: 1,
    tipo: 'reserva_creada',
    fecha: at(-1, '10:20'),
    detalle: {
      reservaId: 5,
      espacio: 'Cancha 2',
      inicio: at(3, '18:00'),
      fin: at(3, '19:00'),
    },
  },
  {
    id: 'ev-5',
    usuarioId: 4,
    tipo: 'reserva_cancelada',
    fecha: at(0, '08:30'),
    detalle: {
      reservaId: 4,
      espacio: 'Parrilla 2',
      inicio: at(2, '10:00'),
      fin: at(2, '14:00'),
      anticipacionHoras: 49,
    },
  },
  {
    id: 'ev-6',
    usuarioId: 6,
    tipo: 'reserva_rechazada',
    fecha: at(-1, '12:05'),
    detalle: {
      espacio: 'Cancha 1',
      inicio: at(1, '18:00'),
      fin: at(1, '19:00'),
      motivo: 'El turno ya está reservado',
    },
  },
]
