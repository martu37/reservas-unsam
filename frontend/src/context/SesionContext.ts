import { createContext } from 'react'
import type { Usuario } from '../domain/Usuario'

// Quién está usando el sistema: se entra con el mail institucional, sin
// contraseña (es una prueba de concepto)
export type Sesion = {
  usuario: Usuario | null
  cargando: boolean
  ingresar: (usuario: Usuario) => void
  salir: () => void
}

export const SesionContext = createContext<Sesion>({
  usuario: null,
  cargando: false,
  ingresar: () => {},
  salir: () => {},
})
