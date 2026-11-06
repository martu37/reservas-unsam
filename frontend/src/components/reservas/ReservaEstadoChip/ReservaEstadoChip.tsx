import type { Reserva } from '../../../domain/Reserva'
import { StatusChip } from '../../ui/StatusChip/StatusChip'

type ReservaEstadoChipProps = { reserva: Reserva; ahora: string }

export function ReservaEstadoChip({ reserva, ahora }: ReservaEstadoChipProps) {
  if (!reserva.estaConfirmada()) {
    return <StatusChip label="Cancelada" tone="error" />
  }
  if (reserva.yaEmpezo(ahora)) {
    return <StatusChip label="Finalizada" tone="neutral" />
  }
  return <StatusChip label="Confirmada" tone="success" />
}
