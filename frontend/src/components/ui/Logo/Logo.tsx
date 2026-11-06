import Box from '@mui/material/Box'
import styles from './Logo.module.css'
import Typography from '@mui/material/Typography'

export function Logo() {
  return (
    <Box className={styles.logo}>
      <img src="/logo.png" alt="" className={styles.imagen} />
      <Typography variant="h6" component="span" className={styles.title}>
        Reservas UNSAM
      </Typography>
    </Box>
  )
}
