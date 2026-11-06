import Storage from '@mui/icons-material/Storage'
import Chip from '@mui/material/Chip'
import { NOMBRE_MOTOR, type Motor } from '../../../domain/Motor'
import styles from './MotorChip.module.css'

export function MotorChip({ motor }: { motor: Motor }) {
  return (
    <Chip
      icon={<Storage />}
      label={`Datos: ${NOMBRE_MOTOR[motor]}`}
      size="small"
      variant="outlined"
      className={`${styles.chip} ${styles[motor]}`}
    />
  )
}
