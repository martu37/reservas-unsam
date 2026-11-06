import Alert from '@mui/material/Alert'
import type { ReactNode } from 'react'
import { useSesion } from '../../../hooks/useSesion'

export function SoloAdmin({ children }: { children: ReactNode }) {
  const { usuario } = useSesion()
  if (!usuario) return null
  if (!usuario.esAdmin()) {
    return (
      <Alert severity="warning">
        Esta sección es solo para quienes administran los espacios.
      </Alert>
    )
  }
  return children
}
