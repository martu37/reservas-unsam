import Button from '@mui/material/Button'
import { Link } from 'react-router'
import styles from './FormActions.module.css'

type FormActionsProps = {
  cancelTo: string
}

export function FormActions({ cancelTo }: FormActionsProps) {
  return (
    <div className={styles.actions}>
      <Button component={Link} to={cancelTo} variant="outlined">
        Cancelar
      </Button>
      <Button type="submit" variant="contained">
        Guardar
      </Button>
    </div>
  )
}
