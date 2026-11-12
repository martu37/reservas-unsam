import Box from '@mui/material/Box'
import Button from '@mui/material/Button'
import Drawer from '@mui/material/Drawer'
import List from '@mui/material/List'
import ListItemButton from '@mui/material/ListItemButton'
import ListItemText from '@mui/material/ListItemText'
import ListSubheader from '@mui/material/ListSubheader'
import Typography from '@mui/material/Typography'
import { NavLink } from 'react-router'
import { useSesion } from '../../../hooks/useSesion'
import { Logo } from '../../ui/Logo/Logo'
import styles from './Sidebar.module.css'

const SECCIONES = [
  { label: 'Disponibilidad', path: '/' },
  { label: 'Mis reservas', path: '/mis-reservas' },
  { label: 'Mi actividad', path: '/actividad' },
]

const SECCIONES_ADMIN = [
  { label: 'Bloqueos', path: '/admin/bloqueos' },
  { label: 'Uso de espacios', path: '/admin/uso' },
]

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
  const { usuario, salir } = useSesion()

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
        <Box className={styles.usuario}>
          <Typography variant="body2" className={styles.nombre}>
            {usuario.nombreCompleto}
            {usuario.esAdmin() ? ' (admin)' : ''}
          </Typography>
          <Typography variant="caption" color="text.secondary" component="p">
            {usuario.mail}
          </Typography>
          <Button size="small" onClick={salir} className={styles.salir}>
            Salir
          </Button>
        </Box>
      )}
    </Drawer>
  )
}
