import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import type { BloqueoData } from '../../../domain/Bloqueo'
import type { Espacio } from '../../../domain/Espacio'
import {
  toBloqueoData,
  validateBloqueo,
  type BloqueoFormValues,
} from '../../../forms/bloqueoForm'
import { useForm } from '../../../hooks/useForm'
import { FormActions } from '../../form/FormActions/FormActions'
import styles from './BloqueoForm.module.css'

type BloqueoFormProps = {
  initialValues: BloqueoFormValues
  espacios: Espacio[]
  cancelTo: string
  onSubmit: (data: BloqueoData) => void
}

export function BloqueoForm({
  initialValues,
  espacios,
  cancelTo,
  onSubmit,
}: BloqueoFormProps) {
  const { field, handleSubmit } = useForm(initialValues, validateBloqueo)

  return (
    <form
      noValidate
      onSubmit={handleSubmit((values) => onSubmit(toBloqueoData(values)))}
      className={styles.form}
    >
      <TextField select label="Espacio" {...field('espacioId')}>
        {espacios.map((espacio) => (
          <MenuItem key={espacio.id} value={String(espacio.id)}>
            {espacio.nombre} ({espacio.tipo.nombre})
          </MenuItem>
        ))}
      </TextField>
      <div className={styles.fechas}>
        <TextField type="datetime-local" label="Desde" {...field('inicio')} />
        <TextField type="datetime-local" label="Hasta" {...field('fin')} />
      </div>
      <TextField
        label="Motivo"
        placeholder="Ej.: corte de pasto, cambio de rejilla"
        {...field('motivo')}
      />
      <FormActions cancelTo={cancelTo} />
    </form>
  )
}
