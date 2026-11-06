import type { ReactNode } from 'react'
import Typography from '@mui/material/Typography'
import styles from './PageTitle.module.css'

type PageTitleProps = {
  children: ReactNode
  subtitle?: string
  actions?: ReactNode
}

export function PageTitle({ children, subtitle, actions }: PageTitleProps) {
  return (
    <header className={styles.header}>
      <div>
        <Typography variant="h5" component="h1">
          {children}
        </Typography>
        {subtitle && (
          <Typography variant="body2" className={styles.subtitle}>
            {subtitle}
          </Typography>
        )}
      </div>
      {actions}
    </header>
  )
}
