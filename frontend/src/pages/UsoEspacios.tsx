import Alert from '@mui/material/Alert'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import { useState } from 'react'
import { SoloAdmin } from '../components/layout/SoloAdmin/SoloAdmin'
import { MotorChip } from '../components/ui/MotorChip/MotorChip'
import { PageTitle } from '../components/ui/PageTitle/PageTitle'
import { SectionCard } from '../components/ui/SectionCard/SectionCard'
import type { UsoEspacio } from '../domain/UsoEspacio'
import { useOnInit } from '../hooks/useOnInit'
import { espacioService } from '../services/espacioService'
import { getErrorMesagge } from '../utils/errorHandling'
import styles from './Pagina.module.css'

export function UsoEspacios() {
  const [uso, setUso] = useState<UsoEspacio[]>([])
  const [error, setError] = useState('')

  useOnInit(() => {
    espacioService
      .getUso()
      .then(setUso)
      .catch((e) => setError(getErrorMesagge(e)))
  })

  return (
    <SoloAdmin>
      <PageTitle
        subtitle="Reservas confirmadas y canceladas de cada espacio"
        actions={<MotorChip motor="postgres" />}
      >
        Uso de espacios
      </PageTitle>

      {error && (
        <Alert severity="error" className={styles.error}>
          {error}
        </Alert>
      )}

      <SectionCard disablePadding>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Espacio</TableCell>
                <TableCell>Tipo</TableCell>
                <TableCell align="right">Confirmadas</TableCell>
                <TableCell align="right">Canceladas</TableCell>
                <TableCell align="right">% canceladas</TableCell>
                <TableCell>Demanda</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {uso.map((fila) => (
                <TableRow hover key={fila.espacio}>
                  <TableCell>{fila.espacio}</TableCell>
                  <TableCell>{fila.tipo}</TableCell>
                  <TableCell align="right">{fila.confirmadas}</TableCell>
                  <TableCell align="right">{fila.canceladas}</TableCell>
                  <TableCell align="right">
                    {fila.porcentajeCanceladas}%
                  </TableCell>
                  <TableCell>{fila.demanda}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </SectionCard>
    </SoloAdmin>
  )
}
