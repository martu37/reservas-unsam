import { sePisan, type Franja } from './Franja'

export type EstadoReserva = 'confirmada' | 'cancelada'

export type ReservaJSON = {
  id: number
  usuarioId: number
  espacioId: number
  espacioNombre: string
  inicio: string
  fin: string
  estado: EstadoReserva
  creadaEn: string
  canceladaEn: string | null
}

export class Reserva {
  id = 0
  usuarioId = 0
  espacioId = 0
  espacioNombre = ''
  inicio = ''
  fin = ''
  estado: EstadoReserva = 'confirmada'
  creadaEn = ''
  canceladaEn: string | null = null

  static fromJson(json: ReservaJSON): Reserva {
    return Object.assign(new Reserva(), json)
  }

  estaConfirmada() {
    return this.estado === 'confirmada'
  }

  // Una reserva cancelada libera el turno, así que no ocupa nada
  ocupa(franja: Franja) {
    return this.estaConfirmada() && sePisan(this, franja)
  }

  yaEmpezo(ahora: string) {
    return this.inicio <= ahora
  }

  puedeCancelarse(ahora: string) {
    return this.estaConfirmada() && !this.yaEmpezo(ahora)
  }
}
