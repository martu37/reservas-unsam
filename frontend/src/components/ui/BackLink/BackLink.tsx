import ArrowBack from '@mui/icons-material/ArrowBack'
import Button from '@mui/material/Button'
import { Link } from 'react-router'
import styles from './BackLink.module.css'

type BackLinkProps = {
  to: string
  children: string
}

export function BackLink({ to, children }: BackLinkProps) {
  return (
    <Button
      component={Link}
      to={to}
      size="small"
      startIcon={<ArrowBack />}
      className={styles.back}
    >
      {children}
    </Button>
  )
}
