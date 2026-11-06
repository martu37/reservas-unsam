import { addMinutes, atTime } from '../utils/dates'
import type { Franja } from './Franja'

export type TipoEspacioJSON = {
  id: number
  nombre: string
  duracionMin: number
  horaApertura: string
  horaCierre: string
}

export class TipoEspacio {
  id = 0
  nombre = ''
  duracionMin = 0
  horaApertura = '00:00'
  horaCierre = '00:00'

  static fromJson(json: TipoEspacioJSON): TipoEspacio {
    return Object.assign(new TipoEspacio(), json)
  }

  // Los turnos no se guardan en la base: salen del horario y la duración del tipo
  turnosDelDia(fecha: string): Franja[] {
    const turnos: Franja[] = []
    const cierre = atTime(fecha, this.horaCierre)
    let inicio = atTime(fecha, this.horaApertura)
    let fin = addMinutes(inicio, this.duracionMin)

    while (fin <= cierre) {
      turnos.push({ inicio, fin })
      inicio = fin
      fin = addMinutes(inicio, this.duracionMin)
    }
    return turnos
  }

  esTurnoValido(franja: Franja): boolean {
    return this.turnosDelDia(franja.inicio.slice(0, 10)).some(
      (turno) => turno.inicio === franja.inicio && turno.fin === franja.fin,
    )
  }
}
