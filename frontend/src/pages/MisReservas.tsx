import Alert from '@mui/material/Alert'
import { useEffect, useState } from 'react'
import { ReservaTable } from '../components/reservas/ReservaTable/ReservaTable'
import { ConfirmDialog } from '../components/ui/ConfirmDialog/ConfirmDialog'
import { MotorChip } from '../components/ui/MotorChip/MotorChip'
import { PageTitle } from '../components/ui/PageTitle/PageTitle'
import { SectionCard } from '../components/ui/SectionCard/SectionCard'
import type { Reserva } from '../domain/Reserva'
import { useSesion } from '../hooks/useSesion'
import { reservaService } from '../services/reservaService'
import { formatTime, now } from '../utils/dates'
import { getErrorMesagge } from '../utils/errorHandling'
import styles from './Pagina.module.css'

export function MisReservas() {
  const { usuario } = useSesion()
  const [reservas, setReservas] = useState<Reserva[]>([])
  const [aCancelar, setACancelar] = useState<Reserva | null>(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!usuario) return
    let ignore = false
    reservaService
      .getDeUsuario(usuario.id)
      .then((reservas) => {
        if (!ignore) setReservas(reservas)
      })
      .catch((e) => setError(getErrorMesagge(e)))
    return () => {
      ignore = true
    }
  }, [usuario])

  const handleCancelar = async () => {
    if (!aCancelar || !usuario) return
    try {
      const cancelada = await reservaService.cancelar(aCancelar.id, usuario.id)
      setReservas(
        reservas.map((reserva) =>
          reserva.id === cancelada.id ? cancelada : reserva,
        ),
      )
    } catch (e) {
      setError(getErrorMesagge(e))
    } finally {
      setACancelar(null)
    }
  }

  return (
    <>
      <PageTitle
        subtitle={usuario?.nombreCompleto}
        actions={<MotorChip motor="postgres" />}
      >
        Mis reservas
      </PageTitle>

      {error && (
        <Alert severity="error" className={styles.error}>
          {error}
        </Alert>
      )}

      <SectionCard disablePadding>
        <ReservaTable
          reservas={reservas}
          ahora={now()}
          onCancelar={setACancelar}
        />
      </SectionCard>

      <ConfirmDialog
        open={aCancelar !== null}
        title="Cancelar reserva"
        message={
          aCancelar
            ? `Se libera ${aCancelar.espacioNombre} de ${formatTime(aCancelar.inicio)} a ${formatTime(aCancelar.fin)}.`
            : ''
        }
        confirmLabel="Cancelar reserva"
        onConfirm={handleCancelar}
        onCancel={() => setACancelar(null)}
      />
    </>
  )
}
