import Button from '@mui/material/Button'
import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import type { Reserva } from '../../../domain/Reserva'
import { formatDate, formatDayName, formatTime } from '../../../utils/dates'
import { ReservaEstadoChip } from '../ReservaEstadoChip/ReservaEstadoChip'

type ReservaTableProps = {
  reservas: Reserva[]
  ahora: string
  onCancelar: (reserva: Reserva) => void
}

export function ReservaTable({
  reservas,
  ahora,
  onCancelar,
}: ReservaTableProps) {
  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Espacio</TableCell>
            <TableCell>Día</TableCell>
            <TableCell>Horario</TableCell>
            <TableCell>Estado</TableCell>
            <TableCell align="right">Acciones</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {reservas.map((reserva) => (
            <TableRow hover key={reserva.id}>
              <TableCell>{reserva.espacioNombre}</TableCell>
              <TableCell>
                {formatDayName(reserva.inicio)} ({formatDate(reserva.inicio)})
              </TableCell>
              <TableCell>
                {formatTime(reserva.inicio)} a {formatTime(reserva.fin)}
              </TableCell>
              <TableCell>
                <ReservaEstadoChip reserva={reserva} ahora={ahora} />
              </TableCell>
              <TableCell align="right">
                {reserva.puedeCancelarse(ahora) && (
                  <Button
                    size="small"
                    color="error"
                    onClick={() => onCancelar(reserva)}
                  >
                    Cancelar
                  </Button>
                )}
              </TableCell>
            </TableRow>
          ))}
          {reservas.length === 0 && (
            <TableRow>
              <TableCell colSpan={5} align="center">
                No hay reservas
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
