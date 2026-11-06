import { sePisan, type Franja } from './Franja'

export type BloqueoJSON = {
  id: number
  espacioId: number
  espacioNombre: string
  inicio: string
  fin: string
  motivo: string
  creadoPor: number
  creadoEn: string
}

export type BloqueoData = Pick<
  BloqueoJSON,
  'espacioId' | 'inicio' | 'fin' | 'motivo'
>

export class Bloqueo {
  id = 0
  espacioId = 0
  espacioNombre = ''
  inicio = ''
  fin = ''
  motivo = ''
  creadoPor = 0
  creadoEn = ''

  static fromJson(json: BloqueoJSON): Bloqueo {
    return Object.assign(new Bloqueo(), json)
  }

  cubre(franja: Franja) {
    return sePisan(this, franja)
  }

  yaTermino(ahora: string) {
    return this.fin <= ahora
  }
}
