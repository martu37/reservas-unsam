import Alert from '@mui/material/Alert'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { useEffect, useState } from 'react'
import { EspacioCard } from '../components/espacios/EspacioCard/EspacioCard'
import { ConfirmDialog } from '../components/ui/ConfirmDialog/ConfirmDialog'
import { MotorChip } from '../components/ui/MotorChip/MotorChip'
import { PageTitle } from '../components/ui/PageTitle/PageTitle'
import type { Espacio } from '../domain/Espacio'
import type { Turno } from '../domain/Turno'
import { useOnInit } from '../hooks/useOnInit'
import { useSesion } from '../hooks/useSesion'
import { espacioService } from '../services/espacioService'
import { reservaService } from '../services/reservaService'
import { formatDayName, formatTime, today } from '../utils/dates'
import { getErrorMesagge } from '../utils/errorHandling'
import styles from './Disponibilidad.module.css'

type Pedido = { espacio: Espacio; turno: Turno }
type Mensaje = { severity: 'success' | 'error'; texto: string }

const describir = ({ espacio, turno }: Pedido) =>
  `${espacio.nombre}, ${formatDayName(turno.inicio)} de ${formatTime(turno.inicio)} a ${formatTime(turno.fin)}`

export function Disponibilidad() {
  const { usuario } = useSesion()
  const [espacios, setEspacios] = useState<Espacio[]>([])
  const [turnos, setTurnos] = useState<Record<number, Turno[]>>({})
  const [fecha, setFecha] = useState(today())
  const [tipo, setTipo] = useState('')
  const [pedido, setPedido] = useState<Pedido | null>(null)
  const [mensaje, setMensaje] = useState<Mensaje | null>(null)
  const [recargas, setRecargas] = useState(0)

  useOnInit(() => {
    espacioService
      .getAll()
      .then(setEspacios)
      .catch((e) =>
        setMensaje({ severity: 'error', texto: getErrorMesagge(e) }),
      )
  })

  useEffect(() => {
    let ignore = false
    Promise.all(
      espacios.map((espacio) => espacioService.getTurnos(espacio.id, fecha)),
    )
      .then((listas) => {
        if (ignore) return
        setTurnos(
          Object.fromEntries(
            espacios.map((espacio, i) => [espacio.id, listas[i]]),
          ),
        )
      })
      .catch((e) =>
        setMensaje({ severity: 'error', texto: getErrorMesagge(e) }),
      )
    return () => {
      ignore = true
    }
  }, [espacios, fecha, recargas])

  const tipos = [...new Set(espacios.map((espacio) => espacio.tipo.nombre))]
  const visibles = espacios.filter(
    (espacio) => !tipo || espacio.tipo.nombre === tipo,
  )

  const handleConfirmar = async () => {
    if (!pedido || !usuario) return
    try {
      await reservaService.crear(usuario.id, pedido.espacio.id, pedido.turno)
      setMensaje({
        severity: 'success',
        texto: `Reserva confirmada: ${describir(pedido)}`,
      })
    } catch (e) {
      setMensaje({ severity: 'error', texto: getErrorMesagge(e) })
    } finally {
      setPedido(null)
      setRecargas((n) => n + 1)
    }
  }

  return (
    <>
      <PageTitle
        subtitle="Elegí un día y tocá un turno libre para reservarlo"
        actions={<MotorChip motor="postgres" />}
      >
        Disponibilidad
      </PageTitle>

      <div className={styles.filtros}>
        <TextField
          type="date"
          label="Día"
          value={fecha}
          onChange={(e) => e.target.value && setFecha(e.target.value)}
          slotProps={{ htmlInput: { min: today() } }}
        />
        <TextField
          select
          label="Tipo"
          value={tipo}
          onChange={(e) => setTipo(e.target.value)}
          className={styles.tipo}
          slotProps={{ select: { displayEmpty: true } }}
        >
          <MenuItem value="">Todos</MenuItem>
          {tipos.map((nombre) => (
            <MenuItem key={nombre} value={nombre}>
              {nombre}
            </MenuItem>
          ))}
        </TextField>
      </div>

      {mensaje && (
        <Alert
          severity={mensaje.severity}
          onClose={() => setMensaje(null)}
          className={styles.mensaje}
        >
          {mensaje.texto}
        </Alert>
      )}

      <div className={styles.grilla}>
        {visibles.map((espacio) => (
          <EspacioCard
            key={espacio.id}
            espacio={espacio}
            turnos={turnos[espacio.id] ?? []}
            onReservar={(espacio, turno) => setPedido({ espacio, turno })}
          />
        ))}
      </div>

      <ConfirmDialog
        open={pedido !== null}
        title="Confirmar reserva"
        message={pedido ? describir(pedido) : ''}
        confirmLabel="Reservar"
        confirmColor="primary"
        onConfirm={handleConfirmar}
        onCancel={() => setPedido(null)}
      />
    </>
  )
}
