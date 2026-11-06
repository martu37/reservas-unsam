import { formatDateTime, formatTime } from '../utils/dates'

export type TipoEvento =
  'reserva_creada' | 'reserva_cancelada' | 'reserva_rechazada'

// El detalle cambia según el tipo de evento. Por eso va a MongoDB:
// cada documento guarda solo los campos que le corresponden.
export type DetalleEvento = {
  reservaId?: number
  espacio?: string
  inicio?: string
  fin?: string
  motivo?: string
  anticipacionHoras?: number
}

export type EventoHistorialJSON = {
  id: string
  usuarioId: number
  tipo: TipoEvento
  fecha: string
  detalle: DetalleEvento
}

const TITULOS: Record<TipoEvento, string> = {
  reserva_creada: 'Reserva confirmada',
  reserva_cancelada: 'Reserva cancelada',
  reserva_rechazada: 'Pedido rechazado',
}

export class EventoHistorial {
  id = ''
  usuarioId = 0
  tipo: TipoEvento = 'reserva_creada'
  fecha = ''
  detalle: DetalleEvento = {}

  static fromJson(json: EventoHistorialJSON): EventoHistorial {
    return Object.assign(new EventoHistorial(), json)
  }

  get titulo() {
    return TITULOS[this.tipo]
  }

  get descripcion() {
    const { espacio, inicio, fin, motivo, anticipacionHoras } = this.detalle
    const turno =
      inicio && fin ? `${formatDateTime(inicio)} a ${formatTime(fin)}` : ''
    const partes = [espacio, turno]
    if (motivo) partes.push(motivo)
    if (anticipacionHoras !== undefined) {
      partes.push(`con ${anticipacionHoras} h de anticipación`)
    }
    return partes.filter(Boolean).join(' · ')
  }
}
