// Qué base de datos responde cada parte del sistema (punto 8 de la consigna)
export type Motor = 'postgres' | 'mongo'

export const NOMBRE_MOTOR: Record<Motor, string> = {
  postgres: 'PostgreSQL',
  mongo: 'MongoDB',
}
