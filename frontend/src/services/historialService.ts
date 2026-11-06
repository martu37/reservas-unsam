import {
  EventoHistorial,
  type DetalleEvento,
  type TipoEvento,
} from '../domain/EventoHistorial'
import type { Motor } from '../domain/Motor'
import { historialMock } from '../mocks/historialMock'
import { now } from '../utils/dates'

export type Historial = { motor: Motor; eventos: EventoHistorial[] }

class HistorialService {
  // En el backend el historial tiene dos implementaciones: MongoDB (la principal)
  // y una tabla JSONB en PostgreSQL de respaldo. La respuesta dice cuál contestó.
  async getDeUsuario(usuarioId: number, motor: Motor): Promise<Historial> {
    const eventos = historialMock
      .filter((evento) => evento.usuarioId === usuarioId)
      .sort((uno, otro) => otro.fecha.localeCompare(uno.fecha))
      .map((evento) => EventoHistorial.fromJson(structuredClone(evento)))
    return { motor, eventos }
  }

  async registrar(
    usuarioId: number,
    tipo: TipoEvento,
    detalle: DetalleEvento,
  ): Promise<void> {
    historialMock.push({
      id: `ev-${historialMock.length + 1}`,
      usuarioId,
      tipo,
      fecha: now(),
      detalle,
    })
  }
}

export const historialService = new HistorialService()
