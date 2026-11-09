import axios from 'axios'
import {
  EventoHistorial,
  type EventoHistorialJSON,
} from '../domain/EventoHistorial'
import type { Motor } from '../domain/Motor'
import { REST_SERVER_URL } from './constants'

export type Historial = { motor: Motor; eventos: EventoHistorial[] }

type HistorialJSON = { motor: Motor; eventos: EventoHistorialJSON[] }

class HistorialService {
  // En el backend el historial tiene dos implementaciones: MongoDB (la principal)
  // y una tabla JSONB en PostgreSQL de respaldo. La respuesta dice cuál contestó.
  async getDeUsuario(usuarioId: number, motor: Motor): Promise<Historial> {
    const { data } = await axios.get<HistorialJSON>(
      `${REST_SERVER_URL}/usuarios/${usuarioId}/historial`,
      { params: { motor } },
    )
    return {
      motor: data.motor,
      eventos: data.eventos.map(EventoHistorial.fromJson),
    }
  }
}

export const historialService = new HistorialService()
