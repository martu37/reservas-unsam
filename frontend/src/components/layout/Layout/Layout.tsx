import Box from '@mui/material/Box'
import { Outlet } from 'react-router'
import { Sidebar } from '../Sidebar/Sidebar'
import styles from './Layout.module.css'

export function Layout() {
  return (
    <Box className={styles.container}>
      <Sidebar />
      <Box component="main" className={styles.content}>
        <Outlet />
      </Box>
    </Box>
  )
}
