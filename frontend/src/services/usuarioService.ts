import axios from 'axios'
import {
  Usuario,
  type NuevoUsuarioData,
  type UsuarioJSON,
} from '../domain/Usuario'
import { REST_SERVER_URL } from './constants'

class UsuarioService {
  // El ingreso: si el mail todavía no está dado de alta, el backend responde 404
  async buscarPorMail(mail: string): Promise<Usuario> {
    const { data } = await axios.get<UsuarioJSON>(
      `${REST_SERVER_URL}/usuarios/buscar`,
      { params: { mail } },
    )
    return Usuario.fromJson(data)
  }

  // La base vuelve a controlar el mail (ck_usuario_mail) y que no se repita
  // (uq_usuario_mail)
  async crear(nuevo: NuevoUsuarioData): Promise<Usuario> {
    const { data } = await axios.post<UsuarioJSON>(
      `${REST_SERVER_URL}/usuarios`,
      nuevo,
    )
    return Usuario.fromJson(data)
  }
}

export const usuarioService = new UsuarioService()
