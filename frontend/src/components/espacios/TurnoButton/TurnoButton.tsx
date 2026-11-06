import Button from '@mui/material/Button'
import Tooltip from '@mui/material/Tooltip'
import type { EstadoTurno, Turno } from '../../../domain/Turno'
import { formatTime } from '../../../utils/dates'
import styles from './TurnoButton.module.css'

const DESCRIPCION: Record<EstadoTurno, string> = {
  libre: 'Libre: tocá para reservar',
  reservado: 'Reservado',
  bloqueado: 'En mantenimiento',
  pasado: 'Ya pasó',
}

type TurnoButtonProps = {
  turno: Turno
  onReservar: (turno: Turno) => void
}

export function TurnoButton({ turno, onReservar }: TurnoButtonProps) {
  const libre = turno.estado === 'libre'

  return (
    <Tooltip title={DESCRIPCION[turno.estado]}>
      <span>
        <Button
          size="small"
          variant={libre ? 'outlined' : 'text'}
          disabled={!libre}
          onClick={() => onReservar(turno)}
          className={`${styles.turno} ${styles[turno.estado]}`}
        >
          {formatTime(turno.inicio)}
        </Button>
      </span>
    </Tooltip>
  )
}
