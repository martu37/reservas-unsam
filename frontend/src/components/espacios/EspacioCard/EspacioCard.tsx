import Typography from '@mui/material/Typography'
import type { Espacio } from '../../../domain/Espacio'
import type { Turno } from '../../../domain/Turno'
import { SectionCard } from '../../ui/SectionCard/SectionCard'
import { TurnoButton } from '../TurnoButton/TurnoButton'
import styles from './EspacioCard.module.css'

type EspacioCardProps = {
  espacio: Espacio
  turnos: Turno[]
  onReservar: (espacio: Espacio, turno: Turno) => void
}

export function EspacioCard({ espacio, turnos, onReservar }: EspacioCardProps) {
  const libres = turnos.filter((turno) => turno.estado === 'libre').length

  return (
    <SectionCard title={espacio.nombre}>
      <Typography variant="body2" className={styles.detalle}>
        {espacio.tipo.nombre} · {espacio.ubicacion} · turnos de{' '}
        {espacio.tipo.duracionMin} min
      </Typography>
      <div className={styles.turnos}>
        {turnos.map((turno) => (
          <TurnoButton
            key={turno.inicio}
            turno={turno}
            onReservar={(turno) => onReservar(espacio, turno)}
          />
        ))}
      </div>
      <Typography variant="caption" className={styles.libres}>
        {libres === 0 ? 'Sin turnos libres' : `${libres} turnos libres`}
      </Typography>
    </SectionCard>
  )
}
