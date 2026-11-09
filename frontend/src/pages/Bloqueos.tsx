import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import { useState } from 'react'
import { Link } from 'react-router'
import { BloqueoTable } from '../components/bloqueos/BloqueoTable/BloqueoTable'
import { SoloAdmin } from '../components/layout/SoloAdmin/SoloAdmin'
import { ConfirmDialog } from '../components/ui/ConfirmDialog/ConfirmDialog'
import { MotorChip } from '../components/ui/MotorChip/MotorChip'
import { PageTitle } from '../components/ui/PageTitle/PageTitle'
import { SectionCard } from '../components/ui/SectionCard/SectionCard'
import type { Bloqueo } from '../domain/Bloqueo'
import { useOnInit } from '../hooks/useOnInit'
import { useSesion } from '../hooks/useSesion'
import { bloqueoService } from '../services/bloqueoService'
import { now } from '../utils/dates'
import { getErrorMesagge } from '../utils/errorHandling'
import styles from './Pagina.module.css'

export function Bloqueos() {
  const { usuario } = useSesion()
  const [bloqueos, setBloqueos] = useState<Bloqueo[]>([])
  const [aEliminar, setAEliminar] = useState<Bloqueo | null>(null)
  const [error, setError] = useState('')

  useOnInit(() => {
    bloqueoService
      .getAll()
      .then(setBloqueos)
      .catch((e) => setError(getErrorMesagge(e)))
  })

  const handleEliminar = async () => {
    if (!aEliminar || !usuario) return
    try {
      await bloqueoService.eliminar(aEliminar.id, usuario.id)
      setBloqueos(bloqueos.filter((bloqueo) => bloqueo.id !== aEliminar.id))
    } catch (e) {
      setError(getErrorMesagge(e))
    } finally {
      setAEliminar(null)
    }
  }

  return (
    <SoloAdmin>
      <PageTitle
        subtitle="Mientras un espacio está bloqueado no se puede reservar"
        actions={
          <div className={styles.acciones}>
            <MotorChip motor="postgres" />
            <Button
              component={Link}
              to="/admin/bloqueos/nuevo"
              variant="contained"
            >
              Nuevo bloqueo
            </Button>
          </div>
        }
      >
        Bloqueos por mantenimiento
      </PageTitle>

      {error && (
        <Alert severity="error" className={styles.error}>
          {error}
        </Alert>
      )}

      <SectionCard disablePadding>
        <BloqueoTable
          bloqueos={bloqueos}
          ahora={now()}
          onEliminar={setAEliminar}
        />
      </SectionCard>

      <ConfirmDialog
        open={aEliminar !== null}
        title="Eliminar bloqueo"
        message={
          aEliminar
            ? `${aEliminar.espacioNombre} vuelve a quedar disponible en ese horario.`
            : ''
        }
        confirmLabel="Eliminar"
        onConfirm={handleEliminar}
        onCancel={() => setAEliminar(null)}
      />
    </SoloAdmin>
  )
}
