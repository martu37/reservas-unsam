// Un intervalo de tiempo [inicio, fin). Lo usan reservas, bloqueos y turnos.
export type Franja = { inicio: string; fin: string }

// Dos franjas se pisan si una empieza antes de que termine la otra.
// Es la misma condición que después controla la base con el operador && de rangos.
export const sePisan = (una: Franja, otra: Franja) =>
  una.inicio < otra.fin && otra.inicio < una.fin
