import { createContext } from 'react'
import type { Usuario } from '../domain/Usuario'

// Mientras no haya login con el mail institucional, la demo deja elegir
// con qué usuario se está usando el sistema
export type Sesion = {
  usuario: Usuario | null
  usuarios: Usuario[]
  cambiarUsuario: (id: number) => void
}

export const SesionContext = createContext<Sesion>({
  usuario: null,
  usuarios: [],
  cambiarUsuario: () => {},
})
