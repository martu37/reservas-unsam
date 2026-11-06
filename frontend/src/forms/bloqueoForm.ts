import type { Bloqueo, BloqueoData } from '../domain/Bloqueo'
import type { FormErrors } from '../hooks/useForm'
import { fromDateTimeInput, toDateTimeInput } from '../utils/dates'
import { isBlank } from '../utils/validations'

export type BloqueoFormValues = {
  espacioId: string
  inicio: string
  fin: string
  motivo: string
}

export const EMPTY_BLOQUEO: BloqueoFormValues = {
  espacioId: '',
  inicio: '',
  fin: '',
  motivo: '',
}

export const fromBloqueo = (bloqueo: Bloqueo): BloqueoFormValues => ({
  espacioId: String(bloqueo.espacioId),
  inicio: toDateTimeInput(bloqueo.inicio),
  fin: toDateTimeInput(bloqueo.fin),
  motivo: bloqueo.motivo,
})

export const toBloqueoData = (values: BloqueoFormValues): BloqueoData => ({
  espacioId: Number(values.espacioId),
  inicio: fromDateTimeInput(values.inicio),
  fin: fromDateTimeInput(values.fin),
  motivo: values.motivo.trim(),
})

export function validateBloqueo(
  values: BloqueoFormValues,
): FormErrors<BloqueoFormValues> {
  const errors: FormErrors<BloqueoFormValues> = {}

  if (!values.espacioId) errors.espacioId = 'Elegí un espacio'
  if (!values.inicio) errors.inicio = 'Ingresá desde cuándo'
  if (!values.fin) {
    errors.fin = 'Ingresá hasta cuándo'
  } else if (values.inicio && values.fin <= values.inicio) {
    errors.fin = 'Tiene que ser posterior al inicio'
  }
  if (isBlank(values.motivo)) errors.motivo = 'Ingresá el motivo'

  return errors
}
