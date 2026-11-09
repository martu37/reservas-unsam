import axios, { AxiosError, AxiosHeaders } from 'axios'
import { bloqueoService } from '../src/services/bloqueoService'
import { espacioService } from '../src/services/espacioService'
import { historialService } from '../src/services/historialService'
import { reservaService } from '../src/services/reservaService'
import { getErrorMesagge } from '../src/utils/errorHandling'

// Los services solo hablan con el backend: se espía axios para ver qué pedido
// arman y qué hacen con la respuesta, sin levantar el servidor

const URL = 'http://localhost:8080'

const reservaJSON = {
  id: 12,
  usuarioId: 1,
  espacioId: 2,
  espacioNombre: 'Cancha 1',
  inicio: '2026-12-02T18:00:00',
  fin: '2026-12-02T19:00:00',
  estado: 'confirmada' as const,
  creadaEn: '2026-11-07T14:41:00',
  canceladaEn: null,
}

const turno = { inicio: '2026-12-02T18:00:00', fin: '2026-12-02T19:00:00' }

afterEach(() => {
  vi.restoreAllMocks()
})

test('reservar manda el turno en el cuerpo y quién reserva como parámetro', async () => {
  const post = vi
    .spyOn(axios, 'post')
    .mockResolvedValueOnce({ data: reservaJSON })

  const reserva = await reservaService.crear(1, 2, turno)

  expect(post).toHaveBeenCalledWith(
    `${URL}/reservas`,
    { espacioId: 2, ...turno },
    { params: { usuarioId: 1 } },
  )
  expect(reserva.estaConfirmada()).toBe(true)
})

test('si el turno está ocupado se muestra el motivo que manda el backend', async () => {
  const conflicto = new AxiosError('Conflict', '409', undefined, undefined, {
    status: 409,
    statusText: 'Conflict',
    headers: {},
    config: { headers: new AxiosHeaders() },
    data: { status: 409, message: 'El turno ya está reservado' },
  })
  vi.spyOn(axios, 'post').mockRejectedValueOnce(conflicto)
  vi.spyOn(console, 'error').mockImplementation(() => {})

  const error = await reservaService.crear(2, 2, turno).catch((e) => e)

  expect(getErrorMesagge(error)).toBe('El turno ya está reservado')
})

test('cancelar le pega a la reserva con quién la cancela', async () => {
  const patch = vi.spyOn(axios, 'patch').mockResolvedValueOnce({
    data: {
      ...reservaJSON,
      estado: 'cancelada',
      canceladaEn: '2026-11-07T15:00:00',
    },
  })

  const cancelada = await reservaService.cancelar(12, 1)

  expect(patch).toHaveBeenCalledWith(`${URL}/reservas/12/cancelar`, null, {
    params: { usuarioId: 1 },
  })
  expect(cancelada.estaConfirmada()).toBe(false)
})

test('la grilla de un espacio se pide por día', async () => {
  const get = vi.spyOn(axios, 'get').mockResolvedValueOnce({
    data: [{ ...turno, estado: 'libre' }],
  })

  const turnos = await espacioService.getTurnos(2, '2026-12-02')

  expect(get).toHaveBeenCalledWith(`${URL}/espacios/2/turnos`, {
    params: { fecha: '2026-12-02' },
  })
  expect(turnos[0].estado).toBe('libre')
})

test('el historial dice qué motor contestó y arma los eventos', async () => {
  vi.spyOn(axios, 'get').mockResolvedValueOnce({
    data: {
      motor: 'postgres',
      eventos: [
        {
          id: '44',
          usuarioId: 2,
          tipo: 'reserva_rechazada',
          fecha: '2026-11-07T14:41:00',
          detalle: {
            espacio: 'Cancha 1',
            motivo: 'El turno ya está reservado',
          },
        },
      ],
    },
  })

  const historial = await historialService.getDeUsuario(2, 'mongo')

  expect(historial.motor).toBe('postgres')
  expect(historial.eventos[0].titulo).toBe('Pedido rechazado')
})

test('borrar un bloqueo manda quién lo borra', async () => {
  const del = vi.spyOn(axios, 'delete').mockResolvedValueOnce({ data: '' })

  await bloqueoService.eliminar(3, 5)

  expect(del).toHaveBeenCalledWith(`${URL}/bloqueos/3`, {
    params: { usuarioId: 5 },
  })
})
