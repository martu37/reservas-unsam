import axios from 'axios'
import { Espacio, type EspacioJSON } from '../domain/Espacio'
import type { Turno } from '../domain/Turno'
import { REST_SERVER_URL } from './constants'

class EspacioService {
  async getAll(): Promise<Espacio[]> {
    const { data } = await axios.get<EspacioJSON[]>(
      `${REST_SERVER_URL}/espacios`,
    )
    return data.map(Espacio.fromJson)
  }

  async getById(id: number): Promise<Espacio> {
    const { data } = await axios.get<EspacioJSON>(
      `${REST_SERVER_URL}/espacios/${id}`,
    )
    return Espacio.fromJson(data)
  }

  // La grilla la arma el backend con las reservas y bloqueos de PostgreSQL
  async getTurnos(espacioId: number, fecha: string): Promise<Turno[]> {
    const { data } = await axios.get<Turno[]>(
      `${REST_SERVER_URL}/espacios/${espacioId}/turnos`,
      { params: { fecha } },
    )
    return data
  }
}

export const espacioService = new EspacioService()
