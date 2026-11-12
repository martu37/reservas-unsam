// Una fila de la consulta de uso de cada espacio (la arma PostgreSQL)
export type UsoEspacio = {
  espacio: string
  tipo: string
  confirmadas: number
  canceladas: number
  porcentajeCanceladas: number
  demanda: string
}
