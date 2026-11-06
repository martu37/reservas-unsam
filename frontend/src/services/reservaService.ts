import { Bloqueo } from '../domain/Bloqueo'
import type { Franja } from '../domain/Franja'
import { ReglaDeNegocioError } from '../domain/ReglaDeNegocioError'
import { Reserva } from '../domain/Reserva'
import { TipoEspacio } from '../domain/TipoEspacio'
import { espaciosMock, tiposEspacioMock } from '../mocks/espaciosMock'
import {
  bloqueosMock,
  reservasMock,
  type ReservaFila,
} from '../mocks/reservasMock'
import { datePart, now } from '../utils/dates'
import { historialService } from './historialService'

const nombreEspacio = (espacioId: number) =>
  espaciosMock.find((espacio) => espacio.id === espacioId)?.nombre ?? '-'

const toReserva = (fila: ReservaFila) =>
  Reserva.fromJson(
    structuredClone({ ...fila, espacioNombre: nombreEspacio(fila.espacioId) }),
  )

class ReservaService {
  async getDeUsuario(usuarioId: number): Promise<Reserva[]> {
    return reservasMock
      .filter((reserva) => reserva.usuarioId === usuarioId)
      .sort((una, otra) => una.inicio.localeCompare(otra.inicio))
      .map(toReserva)
  }

  async getDeEspacio(espacioId: number, fecha: string): Promise<Reserva[]> {
    return reservasMock
      .filter(
        (reserva) =>
          reserva.espacioId === espacioId && datePart(reserva.inicio) === fecha,
      )
      .map(toReserva)
  }

  async crear(
    usuarioId: number,
    espacioId: number,
    franja: Franja,
  ): Promise<Reserva> {
    const espacio = espaciosMock.find((espacio) => espacio.id === espacioId)
    if (!espacio || !espacio.activo) {
      throw new ReglaDeNegocioError('El espacio no está disponible')
    }
    const tipo = TipoEspacio.fromJson(
      tiposEspacioMock.find((tipo) => tipo.id === espacio.tipoEspacioId)!,
    )
    if (!tipo.esTurnoValido(franja)) {
      throw new ReglaDeNegocioError('El horario no corresponde a un turno')
    }
    if (franja.inicio <= now()) {
      throw new ReglaDeNegocioError('El turno ya pasó')
    }

    const bloqueo = bloqueosMock
      .map((fila) =>
        Bloqueo.fromJson({ ...fila, espacioNombre: espacio.nombre }),
      )
      .find(
        (bloqueo) => bloqueo.espacioId === espacioId && bloqueo.cubre(franja),
      )
    if (bloqueo) {
      return this.rechazar(
        usuarioId,
        espacio.nombre,
        franja,
        `El espacio está bloqueado: ${bloqueo.motivo}`,
      )
    }

    // En el sistema real esto lo frena la restricción EXCLUDE de PostgreSQL,
    // aunque dos personas reserven al mismo tiempo. Acá lo simulamos.
    const ocupado = reservasMock
      .map(toReserva)
      .some(
        (reserva) => reserva.espacioId === espacioId && reserva.ocupa(franja),
      )
    if (ocupado) {
      return this.rechazar(
        usuarioId,
        espacio.nombre,
        franja,
        'El turno ya está reservado',
      )
    }

    const fila: ReservaFila = {
      id: Math.max(0, ...reservasMock.map((reserva) => reserva.id)) + 1,
      usuarioId,
      espacioId,
      inicio: franja.inicio,
      fin: franja.fin,
      estado: 'confirmada',
      creadaEn: now(),
      canceladaEn: null,
    }
    reservasMock.push(fila)

    // Primero se guarda la reserva (PostgreSQL) y después el evento (MongoDB)
    await historialService.registrar(usuarioId, 'reserva_creada', {
      reservaId: fila.id,
      espacio: espacio.nombre,
      ...franja,
    })
    return toReserva(fila)
  }

  async cancelar(id: number, usuarioId: number): Promise<Reserva> {
    const fila = reservasMock.find((reserva) => reserva.id === id)
    if (!fila || fila.usuarioId !== usuarioId) {
      throw new ReglaDeNegocioError('No se encontró la reserva')
    }
    const ahora = now()
    if (!toReserva(fila).puedeCancelarse(ahora)) {
      throw new ReglaDeNegocioError('La reserva ya no se puede cancelar')
    }

    fila.estado = 'cancelada'
    fila.canceladaEn = ahora
    const horas = Math.floor(
      (Date.parse(fila.inicio) - Date.parse(ahora)) / (60 * 60 * 1000),
    )
    await historialService.registrar(usuarioId, 'reserva_cancelada', {
      reservaId: fila.id,
      espacio: nombreEspacio(fila.espacioId),
      inicio: fila.inicio,
      fin: fila.fin,
      anticipacionHoras: horas,
    })
    return toReserva(fila)
  }

  // Un pedido rechazado no es una reserva: no se guarda en PostgreSQL,
  // solo queda el intento en el historial
  private async rechazar(
    usuarioId: number,
    espacio: string,
    franja: Franja,
    motivo: string,
  ): Promise<never> {
    await historialService.registrar(usuarioId, 'reserva_rechazada', {
      espacio,
      ...franja,
      motivo,
    })
    throw new ReglaDeNegocioError(motivo)
  }
}

export const reservaService = new ReservaService()
