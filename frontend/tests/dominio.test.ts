import { Bloqueo } from '../src/domain/Bloqueo'
import { Espacio } from '../src/domain/Espacio'
import { Reserva } from '../src/domain/Reserva'
import { TipoEspacio } from '../src/domain/TipoEspacio'
import {
  EMPTY_INGRESO,
  esMailUnsam,
  validateIngreso,
} from '../src/forms/ingresoForm'

const parrilla = TipoEspacio.fromJson({
  id: 3,
  nombre: 'Parrilla',
  duracionMin: 240,
  horaApertura: '10:00',
  horaCierre: '20:00',
})

const futbol7 = TipoEspacio.fromJson({
  id: 2,
  nombre: 'Cancha fútbol 7',
  duracionMin: 90,
  horaApertura: '09:00',
  horaCierre: '22:00',
})

const reserva = (
  inicio: string,
  fin: string,
  estado: 'confirmada' | 'cancelada' = 'confirmada',
) =>
  Reserva.fromJson({
    id: 1,
    usuarioId: 1,
    espacioId: 1,
    espacioNombre: 'Cancha 1',
    inicio,
    fin,
    estado,
    creadaEn: '2026-11-01T10:00:00',
    canceladaEn: null,
  })

test('los turnos salen del horario y la duración del tipo, sin pasarse del cierre', () => {
  const turnos = parrilla.turnosDelDia('2026-11-10')

  expect(turnos).toEqual([
    { inicio: '2026-11-10T10:00:00', fin: '2026-11-10T14:00:00' },
    { inicio: '2026-11-10T14:00:00', fin: '2026-11-10T18:00:00' },
  ])
})

test('una cancha de 90 minutos arma 8 turnos entre las 9 y las 22', () => {
  const turnos = futbol7.turnosDelDia('2026-11-10')

  expect(turnos).toHaveLength(8)
  expect(turnos.at(-1)).toEqual({
    inicio: '2026-11-10T19:30:00',
    fin: '2026-11-10T21:00:00',
  })
})

test('un horario que no coincide con un turno no es válido', () => {
  expect(
    parrilla.esTurnoValido({
      inicio: '2026-11-10T12:00:00',
      fin: '2026-11-10T16:00:00',
    }),
  ).toBe(false)
  expect(
    parrilla.esTurnoValido({
      inicio: '2026-11-10T14:00:00',
      fin: '2026-11-10T18:00:00',
    }),
  ).toBe(true)
})

test('una reserva confirmada ocupa los turnos que se le superponen, pero no los contiguos', () => {
  const r = reserva('2026-11-10T18:00:00', '2026-11-10T19:00:00')

  expect(
    r.ocupa({ inicio: '2026-11-10T18:30:00', fin: '2026-11-10T19:30:00' }),
  ).toBe(true)
  expect(
    r.ocupa({ inicio: '2026-11-10T19:00:00', fin: '2026-11-10T20:00:00' }),
  ).toBe(false)
})

test('una reserva cancelada libera el turno', () => {
  const r = reserva('2026-11-10T18:00:00', '2026-11-10T19:00:00', 'cancelada')

  expect(
    r.ocupa({ inicio: '2026-11-10T18:00:00', fin: '2026-11-10T19:00:00' }),
  ).toBe(false)
})

test('solo se puede cancelar una reserva confirmada que todavía no empezó', () => {
  const r = reserva('2026-11-10T18:00:00', '2026-11-10T19:00:00')

  expect(r.puedeCancelarse('2026-11-10T17:59:00')).toBe(true)
  expect(r.puedeCancelarse('2026-11-10T18:00:00')).toBe(false)
})

test('el espacio marca cada turno como libre, reservado, bloqueado o pasado', () => {
  const espacio = Espacio.fromJson({
    id: 4,
    nombre: 'Parrilla 1',
    ubicacion: 'Quincho norte',
    activo: true,
    tipo: {
      id: 3,
      nombre: 'Parrilla',
      duracionMin: 240,
      horaApertura: '10:00',
      horaCierre: '20:00',
    },
  })
  const bloqueo = Bloqueo.fromJson({
    id: 1,
    espacioId: 4,
    espacioNombre: 'Parrilla 1',
    inicio: '2026-11-11T00:00:00',
    fin: '2026-11-12T00:00:00',
    motivo: 'Arreglo',
    creadoPor: 5,
    creadoEn: '2026-11-01T10:00:00',
  })

  const martes = espacio.turnosPara(
    '2026-11-10',
    [reserva('2026-11-10T14:00:00', '2026-11-10T18:00:00')],
    [bloqueo],
    '2026-11-10T11:00:00',
  )
  const miercoles = espacio.turnosPara(
    '2026-11-11',
    [],
    [bloqueo],
    '2026-11-10T11:00:00',
  )

  expect(martes.map((turno) => turno.estado)).toEqual(['pasado', 'reservado'])
  expect(miercoles.map((turno) => turno.estado)).toEqual([
    'bloqueado',
    'bloqueado',
  ])
})

describe('el mail para ingresar', () => {
  test('tiene que ser de la UNSAM, de docente o de estudiante', () => {
    expect(esMailUnsam('dfernandez@unsam.edu.ar')).toBe(true)
    expect(esMailUnsam(' Juan.Perez@Estudiantes.UNSAM.edu.ar ')).toBe(true)
    expect(esMailUnsam('juanperez@gmail.com')).toBe(false)
    expect(esMailUnsam('juan@unsam.edu.ar.com')).toBe(false)
  })

  test('la primera vez también pide nombre, apellido y claustro', () => {
    const valores = { ...EMPTY_INGRESO, mail: 'ana@unsam.edu.ar' }

    expect(validateIngreso(valores, false)).toEqual({})
    expect(Object.keys(validateIngreso(valores, true))).toEqual([
      'nombre',
      'apellido',
      'claustro',
    ])
  })
})
