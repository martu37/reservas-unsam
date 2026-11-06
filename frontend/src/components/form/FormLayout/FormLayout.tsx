import type { ReactNode } from 'react'
import Alert from '@mui/material/Alert'
import { BackLink } from '../../ui/BackLink/BackLink'
import { PageTitle } from '../../ui/PageTitle/PageTitle'
import { SectionCard } from '../../ui/SectionCard/SectionCard'
import styles from './FormLayout.module.css'

type FormLayoutProps = {
  title: string
  backTo: string
  backLabel: string
  error?: string
  children: ReactNode
}

export function FormLayout({
  title,
  backTo,
  backLabel,
  error,
  children,
}: FormLayoutProps) {
  return (
    <div className={styles.container}>
      <BackLink to={backTo}>{backLabel}</BackLink>
      <PageTitle>{title}</PageTitle>
      {error && (
        <Alert severity="error" className={styles.error}>
          {error}
        </Alert>
      )}
      <SectionCard>{children}</SectionCard>
    </div>
  )
}
