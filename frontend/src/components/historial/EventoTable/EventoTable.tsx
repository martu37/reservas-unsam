import Table from '@mui/material/Table'
import TableBody from '@mui/material/TableBody'
import TableCell from '@mui/material/TableCell'
import TableContainer from '@mui/material/TableContainer'
import TableHead from '@mui/material/TableHead'
import TableRow from '@mui/material/TableRow'
import type {
  EventoHistorial,
  TipoEvento,
} from '../../../domain/EventoHistorial'
import { formatDateTime } from '../../../utils/dates'
import { StatusChip, type StatusTone } from '../../ui/StatusChip/StatusChip'

const TONO: Record<TipoEvento, StatusTone> = {
  reserva_creada: 'success',
  reserva_cancelada: 'neutral',
  reserva_rechazada: 'error',
}

export function EventoTable({ eventos }: { eventos: EventoHistorial[] }) {
  return (
    <TableContainer>
      <Table>
        <TableHead>
          <TableRow>
            <TableCell>Fecha</TableCell>
            <TableCell>Evento</TableCell>
            <TableCell>Detalle</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {eventos.map((evento) => (
            <TableRow hover key={evento.id}>
              <TableCell>{formatDateTime(evento.fecha)}</TableCell>
              <TableCell>
                <StatusChip label={evento.titulo} tone={TONO[evento.tipo]} />
              </TableCell>
              <TableCell>{evento.descripcion}</TableCell>
            </TableRow>
          ))}
          {eventos.length === 0 && (
            <TableRow>
              <TableCell colSpan={3} align="center">
                Todavía no hay actividad
              </TableCell>
            </TableRow>
          )}
        </TableBody>
      </Table>
    </TableContainer>
  )
}
