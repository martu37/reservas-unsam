import { Usuario } from '../domain/Usuario'
import { usuariosMock } from '../mocks/usuariosMock'

class UsuarioService {
  async getAll(): Promise<Usuario[]> {
    return structuredClone(usuariosMock).map(Usuario.fromJson)
  }
}

export const usuarioService = new UsuarioService()
