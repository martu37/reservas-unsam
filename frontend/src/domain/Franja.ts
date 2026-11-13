// Un intervalo de tiempo [inicio, fin). Lo usan reservas, bloqueos y turnos.
export type Franja = { inicio: string; fin: string }

// Dos franjas se pisan si una empieza antes de que termine la otra.
// Es la misma condición que usa el backend.
export const sePisan = (una: Franja, otra: Franja) =>
  una.inicio < otra.fin && otra.inicio < una.fin
