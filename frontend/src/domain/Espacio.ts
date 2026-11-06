import type { Bloqueo } from './Bloqueo'
import type { Reserva } from './Reserva'
import { TipoEspacio, type TipoEspacioJSON } from './TipoEspacio'
import type { Turno } from './Turno'

export type EspacioJSON = {
  id: number
  nombre: string
  tipo: TipoEspacioJSON
  ubicacion: string
  activo: boolean
}

export class Espacio {
  id = 0
  nombre = ''
  tipo = new TipoEspacio()
  ubicacion = ''
  activo = true

  static fromJson(json: EspacioJSON): Espacio {
    return Object.assign(new Espacio(), {
      ...json,
      tipo: TipoEspacio.fromJson(json.tipo),
    })
  }

  // Arma la grilla del día: cada turno del tipo, con su estado según las
  // reservas y los bloqueos de este espacio
  turnosPara(
    fecha: string,
    reservas: Reserva[],
    bloqueos: Bloqueo[],
    ahora: string,
  ): Turno[] {
    return this.tipo.turnosDelDia(fecha).map((franja) => {
      if (bloqueos.some((bloqueo) => bloqueo.cubre(franja))) {
        return { ...franja, estado: 'bloqueado' }
      }
      if (reservas.some((reserva) => reserva.ocupa(franja))) {
        return { ...franja, estado: 'reservado' }
      }
      if (franja.inicio <= ahora) {
        return { ...franja, estado: 'pasado' }
      }
      return { ...franja, estado: 'libre' }
    })
  }
}
