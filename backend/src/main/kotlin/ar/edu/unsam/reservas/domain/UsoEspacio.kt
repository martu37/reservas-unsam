package ar.edu.unsam.reservas.domain

// Una fila de la consulta "uso de cada espacio" (database/consultas.sql)
data class UsoEspacio(
    val espacio: String,
    val tipo: String,
    val confirmadas: Int,
    val canceladas: Int,
    val porcentajeCanceladas: Int,
    val demanda: String,
)
