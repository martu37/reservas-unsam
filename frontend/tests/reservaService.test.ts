import { daysFromToday } from '../src/mocks/dates'
import { historialService } from '../src/services/historialService'
import { reservaService } from '../src/services/reservaService'
import { atTime } from '../src/utils/dates'

const turno = (dias: number, desde: string, hasta: string) => ({
  inicio: atTime(daysFromToday(dias), desde),
  fin: atTime(daysFromToday(dias), hasta),
})

const tiposDeEvento = async (usuarioId: number) =>
  (await historialService.getDeUsuario(usuarioId, 'mongo')).eventos.map(
    (evento) => evento.tipo,
  )

test('reservar un turno libre lo confirma y deja el evento en el historial', async () => {
  const reserva = await reservaService.crear(3, 1, turno(5, '10:00', '11:00'))

  expect(reserva.estado).toBe('confirmada')
  expect(reserva.espacioNombre).toBe('Cancha 1')
  expect(await tiposDeEvento(3)).toContain('reserva_creada')
})

test('no se puede reservar un turno que ya está reservado', async () => {
  // La reserva 1 del mock: Cancha 1, mañana de 18 a 19
  await expect(
    reservaService.crear(3, 1, turno(1, '18:00', '19:00')),
  ).rejects.toThrow('El turno ya está reservado')
})

test('el pedido rechazado no se guarda como reserva, pero queda en el historial', async () => {
  const antes = (await reservaService.getDeUsuario(4)).length

  await expect(
    reservaService.crear(4, 1, turno(1, '18:00', '19:00')),
  ).rejects.toThrow()

  expect(await reservaService.getDeUsuario(4)).toHaveLength(antes)
  expect(await tiposDeEvento(4)).toContain('reserva_rechazada')
})

test('no se puede reservar un espacio bloqueado', async () => {
  // La Parrilla 3 está en arreglo durante los próximos días
  await expect(
    reservaService.crear(3, 6, turno(3, '10:00', '14:00')),
  ).rejects.toThrow('El espacio está bloqueado: Cambio de rejilla')
})

test('no se puede reservar un horario que no es un turno del espacio', async () => {
  await expect(
    reservaService.crear(3, 4, turno(5, '12:00', '16:00')),
  ).rejects.toThrow('El horario no corresponde a un turno')
})

test('cancelar una reserva la deja cancelada y libera el turno', async () => {
  const reserva = await reservaService.crear(7, 2, turno(6, '20:00', '21:00'))

  const cancelada = await reservaService.cancelar(reserva.id, 7)
  const otra = await reservaService.crear(6, 2, turno(6, '20:00', '21:00'))

  expect(cancelada.estado).toBe('cancelada')
  expect(otra.estado).toBe('confirmada')
})

test('no se puede cancelar la reserva de otra persona', async () => {
  await expect(reservaService.cancelar(1, 2)).rejects.toThrow(
    'No se encontró la reserva',
  )
})
