import type { Claustro, NuevoUsuarioData } from '../domain/Usuario'
import type { FormErrors } from '../hooks/useForm'
import { isBlank } from '../utils/validations'

export type IngresoFormValues = {
  mail: string
  nombre: string
  apellido: string
  claustro: string
}

export const EMPTY_INGRESO: IngresoFormValues = {
  mail: '',
  nombre: '',
  apellido: '',
  claustro: '',
}

export const CLAUSTROS: Claustro[] = ['estudiante', 'docente', 'no docente']

// La misma regla que el backend y que la base (ck_usuario_mail):
// usuario@unsam.edu.ar o usuario@estudiantes.unsam.edu.ar
const MAIL_UNSAM = /^[^@\s]+@([a-z0-9-]+\.)?unsam\.edu\.ar$/i

export const esMailUnsam = (mail: string) => MAIL_UNSAM.test(mail.trim())

// La primera vez que alguien entra también tiene que decir quién es
export function validateIngreso(
  values: IngresoFormValues,
  esNuevo: boolean,
): FormErrors<IngresoFormValues> {
  const errors: FormErrors<IngresoFormValues> = {}

  if (!esMailUnsam(values.mail)) {
    errors.mail = 'Tiene que ser tu mail de la UNSAM (@unsam.edu.ar)'
  }
  if (esNuevo) {
    if (isBlank(values.nombre)) errors.nombre = 'Ingresá tu nombre'
    if (isBlank(values.apellido)) errors.apellido = 'Ingresá tu apellido'
    if (!values.claustro) errors.claustro = 'Elegí tu claustro'
  }
  return errors
}

export const toNuevoUsuario = (
  values: IngresoFormValues,
): NuevoUsuarioData => ({
  mail: values.mail.trim().toLowerCase(),
  nombre: values.nombre.trim(),
  apellido: values.apellido.trim(),
  claustro: values.claustro as Claustro,
})
