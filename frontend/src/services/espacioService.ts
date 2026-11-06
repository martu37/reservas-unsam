import { Espacio } from '../domain/Espacio'
import { ReglaDeNegocioError } from '../domain/ReglaDeNegocioError'
import type { Turno } from '../domain/Turno'
import {
  espaciosMock,
  tiposEspacioMock,
  type EspacioFila,
} from '../mocks/espaciosMock'
import { now } from '../utils/dates'
import { bloqueoService } from './bloqueoService'
import { reservaService } from './reservaService'

// Equivale al JOIN espacio-tipo_espacio que va a hacer el backend
const conTipo = (fila: EspacioFila) => {
  const { tipoEspacioId, ...espacio } = fila
  const tipo = tiposEspacioMock.find((tipo) => tipo.id === tipoEspacioId)!
  return Espacio.fromJson(structuredClone({ ...espacio, tipo }))
}

class EspacioService {
  async getAll(): Promise<Espacio[]> {
    return espaciosMock.filter((espacio) => espacio.activo).map(conTipo)
  }

  async getById(id: number): Promise<Espacio> {
    const fila = espaciosMock.find((espacio) => espacio.id === id)
    if (!fila) throw new ReglaDeNegocioError('No se encontró el espacio')
    return conTipo(fila)
  }

  async getTurnos(espacioId: number, fecha: string): Promise<Turno[]> {
    const espacio = await this.getById(espacioId)
    const [reservas, bloqueos] = await Promise.all([
      reservaService.getDeEspacio(espacioId, fecha),
      bloqueoService.getDeEspacio(espacioId),
    ])
    return espacio.turnosPara(fecha, reservas, bloqueos, now())
  }
}

export const espacioService = new EspacioService()
