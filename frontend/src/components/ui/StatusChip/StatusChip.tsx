import Chip from '@mui/material/Chip'
import styles from './StatusChip.module.css'

export type StatusTone = 'success' | 'warning' | 'error' | 'neutral'

type StatusChipProps = {
  label: string
  tone: StatusTone
}

export function StatusChip({ label, tone }: StatusChipProps) {
  return (
    <Chip
      label={label}
      size="small"
      className={`${styles.chip} ${styles[tone]}`}
    />
  )
}
