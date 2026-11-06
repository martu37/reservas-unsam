import type { TipoEspacioJSON } from '../domain/TipoEspacio'

// Igual que las tablas tipo_espacio y espacio: el espacio apunta a su tipo por id
export const tiposEspacioMock: TipoEspacioJSON[] = [
  {
    id: 1,
    nombre: 'Cancha fútbol 5',
    duracionMin: 60,
    horaApertura: '09:00',
    horaCierre: '22:00',
  },
  {
    id: 2,
    nombre: 'Cancha fútbol 7',
    duracionMin: 90,
    horaApertura: '09:00',
    horaCierre: '22:00',
  },
  {
    id: 3,
    nombre: 'Parrilla',
    duracionMin: 240,
    horaApertura: '10:00',
    horaCierre: '20:00',
  },
]

export type EspacioFila = {
  id: number
  nombre: string
  tipoEspacioId: number
  ubicacion: string
  activo: boolean
}

export const espaciosMock: EspacioFila[] = [
  {
    id: 1,
    nombre: 'Cancha 1',
    tipoEspacioId: 1,
    ubicacion: 'Sector deportivo',
    activo: true,
  },
  {
    id: 2,
    nombre: 'Cancha 2',
    tipoEspacioId: 1,
    ubicacion: 'Sector deportivo',
    activo: true,
  },
  {
    id: 3,
    nombre: 'Cancha 3',
    tipoEspacioId: 2,
    ubicacion: 'Sector deportivo',
    activo: true,
  },
  {
    id: 4,
    nombre: 'Parrilla 1',
    tipoEspacioId: 3,
    ubicacion: 'Quincho norte',
    activo: true,
  },
  {
    id: 5,
    nombre: 'Parrilla 2',
    tipoEspacioId: 3,
    ubicacion: 'Quincho norte',
    activo: true,
  },
  {
    id: 6,
    nombre: 'Parrilla 3',
    tipoEspacioId: 3,
    ubicacion: 'Patio central',
    activo: true,
  },
]
