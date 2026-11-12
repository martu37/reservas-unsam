# MongoDB: el historial de cada usuario

> **Estado:** en desarrollo. Mientras tanto, el historial se guarda en la tabla
> de respaldo `historial_evento` de PostgreSQL y la app funciona igual.

PostgreSQL dice qué reservas existen. MongoDB guarda **todo lo que le pasó a
cada usuario**: reservas confirmadas, cancelaciones y también los pedidos
rechazados, que no son reservas y por eso no están en PostgreSQL. Con eso se
puede analizar qué horarios se piden más, cuántas veces rechazamos a alguien o
con cuánta anticipación se cancela.

## Por qué en MongoDB

Cada tipo de evento trae datos distintos:

| evento | datos |
|---|---|
| reserva confirmada | reserva, espacio, horario |
| reserva cancelada | reserva, espacio, horario, horas de anticipación (o el motivo, si la canceló un bloqueo) |
| pedido rechazado | espacio, horario, motivo del rechazo |

En una tabla habría que poner todas esas columnas y dejar en `NULL` las que no
corresponden (es lo que hace la tabla de respaldo). En MongoDB cada documento
guarda solo lo suyo, y si mañana aparece un evento nuevo no hay que cambiar
ninguna tabla.

Lo que perdemos: MongoDB no controla FK ni la forma de los documentos como lo
hace PostgreSQL. Por eso acá solo va el historial, que es un registro de lo que
pasó, y **nunca decide si un turno está libre**: eso lo decide PostgreSQL.

## Cómo es un documento

Colección `historial`:

```js
{
  _id: ObjectId("..."),            // lo genera MongoDB
  usuarioId: 1,
  tipo: "RESERVA_RECHAZADA",
  fecha: ISODate("2026-11-09T18:00:00Z"),
  detalle: {                       // cambia según el tipo
    espacio: "Cancha 1",
    inicio: ISODate("2026-11-20T21:00:00Z"),
    fin: ISODate("2026-11-20T22:00:00Z"),
    motivo: "El turno ya está reservado"
  }
}
```

MongoDB guarda las fechas en UTC: las 18:00 de Argentina se ven como `21:00Z`.

Índice: `{ usuarioId: 1, fecha: -1 }`, porque la consulta de siempre es "el
historial de un usuario, lo último primero". Es el mismo que
`ix_historial_usuario_fecha` en PostgreSQL.

## Datos de ejemplo

[`database/scripts_nosql/historial.js`](../database/scripts_nosql/historial.js)
carga 16 eventos que corresponden a los datos de PostgreSQL: uno por cada una de
las 11 reservas, la cancelación de la reserva de Sofía y 4 pedidos rechazados
(estos solo existen en MongoDB). Como los `.sql`, se carga solo la primera vez
que se crea la base; para volver a cargarlo, `make reset`.

## Las dos implementaciones

El backend tiene una sola interfaz para el historial (`HistorialRepository`) y
dos formas de guardarlo: en MongoDB (la principal) y en PostgreSQL (el
respaldo). Cuál se usa se elige por configuración, y el resto del backend no se
entera. Es el backend polimórfico que pide la consigna (ver [backend.md](backend.md)).

## Mirar la base a mano

```
docker exec -it reservas-mongo mongosh
```

```js
show dbs
use reservas
db.historial.find({ usuarioId: 1 })
db.historial.find({ "detalle.espacio": "Cancha 1" })
exit
```
