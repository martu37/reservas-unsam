import Alert from '@mui/material/Alert'
import ToggleButton from '@mui/material/ToggleButton'
import ToggleButtonGroup from '@mui/material/ToggleButtonGroup'
import { useEffect, useState } from 'react'
import { EventoTable } from '../components/historial/EventoTable/EventoTable'
import { MotorChip } from '../components/ui/MotorChip/MotorChip'
import { PageTitle } from '../components/ui/PageTitle/PageTitle'
import { SectionCard } from '../components/ui/SectionCard/SectionCard'
import type { Motor } from '../domain/Motor'
import { useSesion } from '../hooks/useSesion'
import { historialService, type Historial } from '../services/historialService'
import { getErrorMesagge } from '../utils/errorHandling'
import styles from './Pagina.module.css'

export function Actividad() {
  const { usuario } = useSesion()
  const [motor, setMotor] = useState<Motor>('mongo')
  const [historial, setHistorial] = useState<Historial | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!usuario) return
    let ignore = false
    historialService
      .getDeUsuario(usuario.id, motor)
      .then((historial) => {
        if (!ignore) setHistorial(historial)
      })
      .catch((e) => setError(getErrorMesagge(e)))
    return () => {
      ignore = true
    }
  }, [usuario, motor])

  return (
    <>
      <PageTitle
        subtitle="Todo lo que pasó con tus pedidos, incluidos los rechazados"
        actions={historial && <MotorChip motor={historial.motor} />}
      >
        Mi actividad
      </PageTitle>

      <ToggleButtonGroup
        exclusive
        size="small"
        value={motor}
        onChange={(_, valor: Motor | null) => valor && setMotor(valor)}
        className={styles.selector}
      >
        <ToggleButton value="mongo">MongoDB</ToggleButton>
        <ToggleButton value="postgres">PostgreSQL (respaldo)</ToggleButton>
      </ToggleButtonGroup>

      {motor === 'postgres' && (
        <Alert severity="info" className={styles.info}>
          Si MongoDB no responde, el historial se sigue guardando en una tabla
          de respaldo de PostgreSQL.
        </Alert>
      )}

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
