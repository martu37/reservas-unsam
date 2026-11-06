import { Bloqueo, type BloqueoData } from '../domain/Bloqueo'
import { ReglaDeNegocioError } from '../domain/ReglaDeNegocioError'
import { espaciosMock } from '../mocks/espaciosMock'
import { bloqueosMock, type BloqueoFila } from '../mocks/reservasMock'
import { now } from '../utils/dates'

const toBloqueo = (fila: BloqueoFila) =>
  Bloqueo.fromJson(
    structuredClone({
      ...fila,
      espacioNombre:
        espaciosMock.find((espacio) => espacio.id === fila.espacioId)?.nombre ??
        '-',
    }),
  )

const validar = (data: BloqueoData) => {
  if (!espaciosMock.some((espacio) => espacio.id === data.espacioId)) {
    throw new ReglaDeNegocioError('El espacio no existe')
  }
  if (data.fin <= data.inicio) {
    throw new ReglaDeNegocioError('El fin tiene que ser posterior al inicio')
  }
}

class BloqueoService {
  async getAll(): Promise<Bloqueo[]> {
    return [...bloqueosMock]
      .sort((uno, otro) => uno.inicio.localeCompare(otro.inicio))
      .map(toBloqueo)
  }

  async getDeEspacio(espacioId: number): Promise<Bloqueo[]> {
    return bloqueosMock
      .filter((bloqueo) => bloqueo.espacioId === espacioId)
      .map(toBloqueo)
  }

  async getById(id: number): Promise<Bloqueo> {
    return toBloqueo(this.find(id))
  }

  async crear(data: BloqueoData, creadoPor: number): Promise<Bloqueo> {
    validar(data)
    const fila: BloqueoFila = {
      ...data,
      id: Math.max(0, ...bloqueosMock.map((bloqueo) => bloqueo.id)) + 1,
      creadoPor,
      creadoEn: now(),
    }
    bloqueosMock.push(fila)
    return toBloqueo(fila)
  }

  async actualizar(id: number, data: BloqueoData): Promise<Bloqueo> {
    validar(data)
    return toBloqueo(Object.assign(this.find(id), data))
  }

  async eliminar(id: number): Promise<void> {
    const index = bloqueosMock.findIndex((bloqueo) => bloqueo.id === id)
    if (index === -1) throw new ReglaDeNegocioError('No se encontró el bloqueo')
    bloqueosMock.splice(index, 1)
  }

  private find(id: number): BloqueoFila {
    const fila = bloqueosMock.find((bloqueo) => bloqueo.id === id)
    if (!fila) throw new ReglaDeNegocioError('No se encontró el bloqueo')
    return fila
  }
}

export const bloqueoService = new BloqueoService()
