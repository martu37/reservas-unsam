import axios from 'axios'
import { Bloqueo, type BloqueoData, type BloqueoJSON } from '../domain/Bloqueo'
import { REST_SERVER_URL } from './constants'

// Las altas, cambios y bajas llevan quién las pide: el backend controla que sea admin
class BloqueoService {
  async getAll(): Promise<Bloqueo[]> {
    const { data } = await axios.get<BloqueoJSON[]>(
      `${REST_SERVER_URL}/bloqueos`,
    )
    return data.map(Bloqueo.fromJson)
  }

  async getById(id: number): Promise<Bloqueo> {
    const { data } = await axios.get<BloqueoJSON>(
      `${REST_SERVER_URL}/bloqueos/${id}`,
    )
    return Bloqueo.fromJson(data)
  }

  async crear(data: BloqueoData, usuarioId: number): Promise<Bloqueo> {
    const respuesta = await axios.post<BloqueoJSON>(
      `${REST_SERVER_URL}/bloqueos`,
      data,
      { params: { usuarioId } },
    )
    return Bloqueo.fromJson(respuesta.data)
  }

  async actualizar(
    id: number,
    data: BloqueoData,
    usuarioId: number,
  ): Promise<Bloqueo> {
    const respuesta = await axios.put<BloqueoJSON>(
      `${REST_SERVER_URL}/bloqueos/${id}`,
      data,
      { params: { usuarioId } },
    )
    return Bloqueo.fromJson(respuesta.data)
  }

  async eliminar(id: number, usuarioId: number): Promise<void> {
    await axios.delete(`${REST_SERVER_URL}/bloqueos/${id}`, {
      params: { usuarioId },
    })
  }
}

export const bloqueoService = new BloqueoService()
