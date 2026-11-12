import Alert from '@mui/material/Alert'
import Button from '@mui/material/Button'
import MenuItem from '@mui/material/MenuItem'
import Paper from '@mui/material/Paper'
import TextField from '@mui/material/TextField'
import Typography from '@mui/material/Typography'
import { AxiosError } from 'axios'
import { useState } from 'react'
import { Logo } from '../components/ui/Logo/Logo'
import {
  CLAUSTROS,
  EMPTY_INGRESO,
  toNuevoUsuario,
  validateIngreso,
  type IngresoFormValues,
} from '../forms/ingresoForm'
import { useForm } from '../hooks/useForm'
import { useSesion } from '../hooks/useSesion'
import { usuarioService } from '../services/usuarioService'
import { getErrorMesagge } from '../utils/errorHandling'
import styles from './Ingreso.module.css'

// Se entra con el mail de la UNSAM. Si es la primera vez, se piden nombre,
// apellido y claustro y se da de alta como usuario común
export function Ingreso() {
  const { ingresar } = useSesion()
  const [esNuevo, setEsNuevo] = useState(false)
  const [enviando, setEnviando] = useState(false)
  const [error, setError] = useState('')
  const { field, handleSubmit } = useForm(EMPTY_INGRESO, (values) =>
    validateIngreso(values, esNuevo),
  )

  const entrar = async (values: IngresoFormValues) => {
    setError('')
    setEnviando(true)
    try {
      ingresar(
        esNuevo
          ? await usuarioService.crear(toNuevoUsuario(values))
          : await usuarioService.buscarPorMail(values.mail.trim()),
      )
    } catch (e) {
      if (!esNuevo && e instanceof AxiosError && e.response?.status === 404) {
        setEsNuevo(true)
      } else {
        setError(getErrorMesagge(e))
      }
    } finally {
      setEnviando(false)
    }
  }

  return (
    <main className={styles.fondo}>
      <Paper className={styles.tarjeta}>
        <Logo />
        <Typography variant="h5" component="h1">
          Ingresá
        </Typography>
        <Typography color="text.secondary">
          Con tu mail de la UNSAM reservás canchas y parrillas de la
          universidad.
        </Typography>

        <form
          noValidate
          onSubmit={handleSubmit(entrar)}
          className={styles.form}
        >
          <TextField
            label="Mail institucional"
            type="email"
            placeholder="nombre@estudiantes.unsam.edu.ar"
            autoComplete="email"
            disabled={esNuevo}
            {...field('mail')}
          />

          {esNuevo && (
            <>
              <Alert severity="info">
                Es la primera vez que entrás con este mail: contanos quién sos.
              </Alert>
              <TextField label="Nombre" {...field('nombre')} />
              <TextField label="Apellido" {...field('apellido')} />
              <TextField select label="Claustro" {...field('claustro')}>
                {CLAUSTROS.map((claustro) => (
                  <MenuItem key={claustro} value={claustro}>
                    {claustro.charAt(0).toUpperCase() + claustro.slice(1)}
                  </MenuItem>
                ))}
              </TextField>
            </>
          )}

          {error && <Alert severity="error">{error}</Alert>}

          <Button type="submit" variant="contained" disabled={enviando}>
            {esNuevo ? 'Crear mi usuario' : 'Ingresar'}
          </Button>
          {esNuevo && (
            <Button onClick={() => setEsNuevo(false)} disabled={enviando}>
              Usar otro mail
            </Button>
          )}
        </form>
      </Paper>
    </main>
  )
}
