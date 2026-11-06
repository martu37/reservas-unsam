import type { ReactNode } from 'react'
import Paper from '@mui/material/Paper'
import Typography from '@mui/material/Typography'
import styles from './SectionCard.module.css'

type SectionCardProps = {
  title?: string
  actions?: ReactNode
  disablePadding?: boolean
  children: ReactNode
}

export function SectionCard({
  title,
  actions,
  disablePadding = false,
  children,
}: SectionCardProps) {
  return (
    <Paper
      variant="outlined"
      className={disablePadding ? undefined : styles.padded}
    >
      {(title || actions) && (
        <header
          className={
            disablePadding
              ? `${styles.header} ${styles.divided}`
              : styles.header
          }
        >
          {title && (
            <Typography
              variant="subtitle2"
              component="h2"
              className={styles.title}
            >
              {title}
            </Typography>
          )}
          {actions}
        </header>
      )}
      {children}
    </Paper>
  )
}
