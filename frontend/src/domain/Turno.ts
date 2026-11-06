import type { Franja } from './Franja'

export type EstadoTurno = 'libre' | 'reservado' | 'bloqueado' | 'pasado'

export type Turno = Franja & { estado: EstadoTurno }
