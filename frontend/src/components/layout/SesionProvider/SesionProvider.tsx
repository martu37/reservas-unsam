import { useState, type ReactNode } from 'react'
import { SesionContext } from '../../../context/SesionContext'
import type { Usuario } from '../../../domain/Usuario'
import { useOnInit } from '../../../hooks/useOnInit'
import { usuarioService } from '../../../services/usuarioService'

const USUARIO_INICIAL = 1

export function SesionProvider({ children }: { children: ReactNode }) {
  const [usuarios, setUsuarios] = useState<Usuario[]>([])
  const [usuarioId, setUsuarioId] = useState(USUARIO_INICIAL)

  useOnInit(() => {
    usuarioService.getAll().then(setUsuarios)
  })

  const usuario = usuarios.find((usuario) => usuario.id === usuarioId) ?? null

  return (
    <SesionContext.Provider
      value={{ usuario, usuarios, cambiarUsuario: setUsuarioId }}
    >
      {children}
    </SesionContext.Provider>
  )
}
