import axios from 'axios'
import type { Franja } from '../domain/Franja'
import { Reserva, type ReservaJSON } from '../domain/Reserva'
import { REST_SERVER_URL } from './constants'

class ReservaService {
  async getDeUsuario(usuarioId: number): Promise<Reserva[]> {
    const { data } = await axios.get<ReservaJSON[]>(
      `${REST_SERVER_URL}/usuarios/${usuarioId}/reservas`,
    )
    return data.map(Reserva.fromJson)
  }

  // Si el turno está ocupado o bloqueado el backend responde 409 con el motivo.
  // Lo frena PostgreSQL aunque dos personas reserven al mismo tiempo.
  async crear(
    usuarioId: number,
    espacioId: number,
    franja: Franja,
  ): Promise<Reserva> {
    const { data } = await axios.post<ReservaJSON>(
      `${REST_SERVER_URL}/reservas`,
      { espacioId, inicio: franja.inicio, fin: franja.fin },
      { params: { usuarioId } },
    )
    return Reserva.fromJson(data)
  }

  async cancelar(id: number, usuarioId: number): Promise<Reserva> {
    const { data } = await axios.patch<ReservaJSON>(
      `${REST_SERVER_URL}/reservas/${id}/cancelar`,
      null,
      { params: { usuarioId } },
    )
    return Reserva.fromJson(data)
  }
}

export const reservaService = new ReservaService()
