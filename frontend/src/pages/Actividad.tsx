import Alert from '@mui/material/Alert'
import { useEffect, useState } from 'react'
import { EventoTable } from '../components/historial/EventoTable/EventoTable'
import { MotorChip } from '../components/ui/MotorChip/MotorChip'
import { PageTitle } from '../components/ui/PageTitle/PageTitle'
import { SectionCard } from '../components/ui/SectionCard/SectionCard'
import { useSesion } from '../hooks/useSesion'
import { historialService, type Historial } from '../services/historialService'
import { getErrorMesagge } from '../utils/errorHandling'
import styles from './Pagina.module.css'

export function Actividad() {
  const { usuario } = useSesion()
  const [historial, setHistorial] = useState<Historial | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!usuario) return
    let ignore = false
    historialService
      .getDeUsuario(usuario.id)
      .then((historial) => {
        if (!ignore) setHistorial(historial)
      })
      .catch((e) => setError(getErrorMesagge(e)))
    return () => {
      ignore = true
    }
  }, [usuario])

  return (
    <>
      <PageTitle
        subtitle="Todo lo que pasó con tus pedidos, incluidos los rechazados"
        actions={historial && <MotorChip motor={historial.motor} />}
      >
        Mi actividad
      </PageTitle>

      {error && (
        <Alert severity="error" className={styles.error}>
          {error}
        </Alert>
      )}

      <SectionCard disablePadding>
        <EventoTable eventos={historial?.eventos ?? []} />
      </SectionCard>
    </>
  )
}
