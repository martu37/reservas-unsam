import axios from 'axios'
import { Usuario, type UsuarioJSON } from '../domain/Usuario'
import { REST_SERVER_URL } from './constants'

class UsuarioService {
  async getAll(): Promise<Usuario[]> {
    const { data } = await axios.get<UsuarioJSON[]>(
      `${REST_SERVER_URL}/usuarios`,
    )
    return data.map(Usuario.fromJson)
  }
}

export const usuarioService = new UsuarioService()
