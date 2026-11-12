import { useState, type ReactNode } from 'react'
import { SesionContext } from '../../../context/SesionContext'
import type { Usuario } from '../../../domain/Usuario'
import { useOnInit } from '../../../hooks/useOnInit'
import { usuarioService } from '../../../services/usuarioService'

// El navegador recuerda el mail, así al recargar la página no hay que volver
// a ingresar
const CLAVE = 'reservas-unsam.mail'

const leerMail = () => {
  try {
    return localStorage.getItem(CLAVE)
  } catch {
    return null
  }
}

const guardarMail = (mail: string | null) => {
  try {
    if (mail) localStorage.setItem(CLAVE, mail)
    else localStorage.removeItem(CLAVE)
  } catch {
    // sin localStorage (modo incógnito estricto) solo no se recuerda
  }
}

export function SesionProvider({ children }: { children: ReactNode }) {
  const [usuario, setUsuario] = useState<Usuario | null>(null)
  const [cargando, setCargando] = useState(() => leerMail() !== null)

  useOnInit(() => {
    const mail = leerMail()
    if (!mail) return
    usuarioService
      .buscarPorMail(mail)
      .then(setUsuario)
      .catch(() => guardarMail(null))
      .finally(() => setCargando(false))
  })

  const ingresar = (nuevo: Usuario) => {
    guardarMail(nuevo.mail)
    setUsuario(nuevo)
  }

  const salir = () => {
    guardarMail(null)
    setUsuario(null)
  }

  return (
    <SesionContext.Provider value={{ usuario, cargando, ingresar, salir }}>
      {children}
    </SesionContext.Provider>
  )
}
