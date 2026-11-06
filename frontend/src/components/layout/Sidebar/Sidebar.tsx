import Drawer from '@mui/material/Drawer'
import List from '@mui/material/List'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemText from '@mui/material/ListItemText'
import ListSubheader from '@mui/material/ListSubheader'
import MenuItem from '@mui/material/MenuItem'
import TextField from '@mui/material/TextField'
import { NavLink } from 'react-router'
import { useSesion } from '../../../hooks/useSesion'
import { Logo } from '../../ui/Logo/Logo'
import styles from './Sidebar.module.css'

const SECCIONES = [
  { label: 'Disponibilidad', path: '/' },
  { label: 'Mis reservas', path: '/mis-reservas' },
  { label: 'Mi actividad', path: '/actividad' },
]

const SECCIONES_ADMIN = [{ label: 'Bloqueos', path: '/admin/bloqueos' }]

type Seccion = { label: string; path: string }

const Item = ({ label, path }: Seccion) => (
  <ListItemButton
    component={NavLink}
    to={path}
    end={path === '/'}
    className={styles.item}
  >
    <ListItemText primary={label} />
  </ListItemButton>
)

export function Sidebar() {
  const { usuario, usuarios, cambiarUsuario } = useSesion()

  return (
    <Drawer variant="permanent" className={styles.drawer}>
      <Logo />

      <List component="nav" aria-label="Secciones">
        {SECCIONES.map((seccion) => (
          <Item key={seccion.path} {...seccion} />
        ))}
      </List>

      {usuario?.esAdmin() && (
        <List
          component="nav"
          aria-label="Administración"
          subheader={<ListSubheader>Administración</ListSubheader>}
        >
          {SECCIONES_ADMIN.map((seccion) => (
            <Item key={seccion.path} {...seccion} />
          ))}
        </List>
      )}

      {usuario && (
        <TextField
          select
          label="Usando como"
          value={usuario.id}
          onChange={(e) => cambiarUsuario(Number(e.target.value))}
          className={styles.usuario}
          helperText={usuario.mail}
        >
          {usuarios.map((opcion) => (
            <MenuItem key={opcion.id} value={opcion.id}>
              {opcion.nombreCompleto}
              {opcion.esAdmin() ? ' (admin)' : ''}
            </MenuItem>
          ))}
        </TextField>
      )}
    </Drawer>
  )
}
