import Button from '@mui/material/Button'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import { Link } from 'react-router'
import type { Bloqueo } from '../../../domain/Bloqueo'
import { formatDateTime } from '../../../utils/dates'
import { StatusChip } from '../../ui/StatusChip/StatusChip'

type BloqueoTableProps = {
  bloqueos: Bloqueo[]
  ahora: string
  onEliminar: (bloqueo: Bloqueo) => void
}

export function BloqueoTable({
  bloqueos,
  ahora,
  onEliminar,
}: BloqueoTableProps) {
  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Espacio</TableCell>
            <TableCell>Desde</TableCell>
            <TableCell>Hasta</TableCell>
            <TableCell>Motivo</TableCell>
            <TableCell>Estado</TableCell>
            <TableCell align="right">Acciones</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {bloqueos.map((bloqueo) => (
            <TableRow hover key={bloqueo.id}>
              <TableCell>{bloqueo.espacioNombre}</TableCell>
              <TableCell>{formatDateTime(bloqueo.inicio)}</TableCell>
              <TableCell>{formatDateTime(bloqueo.fin)}</TableCell>
              <TableCell>{bloqueo.motivo}</TableCell>
              <TableCell>
                {bloqueo.yaTermino(ahora) ? (
                  <StatusChip label="Terminado" tone="neutral" />
                ) : (
                  <StatusChip label="Vigente" tone="warning" />
                )}
              </TableCell>
              <TableCell align="right">
                <Button
                  component={Link}
                  to={`/admin/bloqueos/${bloqueo.id}/editar`}
                  size="small"
                >
                  Editar
                </Button>
                <Button
                  size="small"
                  color="error"
                  onClick={() => onEliminar(bloqueo)}
                >
                  Eliminar
                </Button>
              </TableCell>
            </TableRow>
          ))}
          {bloqueos.length === 0 && (
            <TableRow>
              <TableCell colSpan={6} align="center">
                No hay bloqueos
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
