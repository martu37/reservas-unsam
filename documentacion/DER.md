# Diagrama Entidad-Relación

```mermaid
erDiagram
    USUARIO ||--o{ RESERVA : hace
    ESPACIO ||--o{ RESERVA : "se reserva en"
    TIPO_ESPACIO ||--o{ ESPACIO : clasifica
    ESPACIO ||--o{ BLOQUEO : tiene
    USUARIO ||--o{ BLOQUEO : registra

    USUARIO {
        int id PK
        varchar mail UK "mail institucional"
        varchar apellido
        varchar nombre
        varchar claustro "estudiante, docente, no docente"
        varchar rol "usuario, admin"
        boolean activo
    }

    TIPO_ESPACIO {
        int id PK
        varchar nombre UK "Cancha fútbol 5, Parrilla..."
        int duracion_min
        time hora_apertura
        time hora_cierre
    }

    ESPACIO {
        int id PK
        varchar nombre UK "Cancha 1, Parrilla 2..."
        int tipo_espacio_id FK
        varchar ubicacion
        boolean activo
    }

    RESERVA {
        int id PK
        int usuario_id FK
        int espacio_id FK
        timestamp inicio
        timestamp fin
        varchar estado "confirmada, cancelada"
        timestamp creada_en
        timestamp cancelada_en "null si sigue confirmada"
    }

    BLOQUEO {
        int id PK
        int espacio_id FK
        timestamp inicio
        timestamp fin
        varchar motivo
        int creado_por FK "usuario con rol admin"
        timestamp creado_en
    }
```

## Relaciones y cardinalidades

| relación | cardinalidad | lectura |
|---|---|---|
| USUARIO hace RESERVA | 1 a N | un usuario puede tener muchas reservas (o ninguna); cada reserva es de un solo usuario |
| ESPACIO se reserva en RESERVA | 1 a N | un espacio tiene muchas reservas; cada reserva es de un solo espacio |
| TIPO_ESPACIO clasifica ESPACIO | 1 a N | un tipo agrupa varios espacios; cada espacio es de un solo tipo |
| ESPACIO tiene BLOQUEO | 1 a N | un espacio puede tener varios bloqueos por mantenimiento en distintas fechas |
| USUARIO registra BLOQUEO | 1 a N | cada bloqueo lo carga un admin |

## Restricciones que no se ven en el diagrama

Todas están implementadas en [`modelo_relacional.sql`](../database/modelo_relacional.sql)
y probadas en [`pruebas_restricciones.sql`](../database/pruebas_restricciones.sql)
(salida en [`pruebas_restricciones_salida.txt`](pruebas_restricciones_salida.txt)).

| regla | cómo la garantiza la base |
|---|---|
| Un turno no se reserva dos veces: no hay dos reservas **confirmadas** del mismo espacio con el mismo inicio | índice único `uq_reserva_turno (espacio_id, inicio) WHERE estado = 'CONFIRMADA'` |
| Una persona no tiene dos reservas **confirmadas** que empiecen a la misma hora (en espacios distintos) | índice único `uq_reserva_usuario_turno (usuario_id, inicio) WHERE estado = 'CONFIRMADA'` |
| `fin` es posterior a `inicio`, en reservas y bloqueos | `CHECK` |
| Si la reserva está cancelada tiene `cancelada_en`; si no, no | `CHECK` |
| Estado, claustro y rol solo toman los valores permitidos | `CHECK ... IN (...)` |
| El mail es de la universidad y no se repite | `CHECK` con `LIKE` y `UNIQUE` |
| El bloqueo tiene un motivo | `CHECK` |
| No se borran espacios ni usuarios con reservas, ni tipos con espacios | `FOREIGN KEY ... ON DELETE RESTRICT` |
| Al borrar un espacio se borran sus bloqueos | `FOREIGN KEY ... ON DELETE CASCADE` |

Alcanza con mirar el inicio porque los turnos son fijos: salen de la duración y
el horario del tipo de espacio (por ejemplo, fútbol 5 de 9 a 10, de 10 a 11...).
Dos reservas del mismo espacio solo se pueden pisar si empiezan a la misma hora.
Para la misma persona en espacios distintos no alcanza: los turnos de fútbol 7
(90 min) no empiezan cuando los de fútbol 5, así que una reserva de 19:30 a 21
se pisa con una de 20 a 21. Por eso esa regla la completa el backend.

### Reglas que controla el backend

Algunas reglas cruzan datos de varias tablas, y un `CHECK` solo puede mirar la
fila que se está guardando. Esas las controla el backend antes de guardar:

- que el horario pedido sea un turno del tipo de espacio y no haya pasado;
- que el espacio esté activo y no esté bloqueado en ese horario;
- que la persona no tenga otra reserva que se pise con ese horario, en ningún espacio;
- que solo un admin cargue bloqueos, y que dos bloqueos del mismo espacio no se pisen.

La que no puede fallar nunca, que un turno no se reserve dos veces, la controla
además la base con el índice único. Así, aunque dos personas reserven el mismo
turno en el mismo momento y las dos pasen el control del backend, la base deja
guardar solo una (lo probamos mandando 6 pedidos a la vez: entró uno y los otros
cinco los frenó `uq_reserva_turno`). Lo mismo con `uq_reserva_usuario_turno` si
una persona manda dos pedidos a la vez para la misma hora.

**Decisión sobre los bloqueos:** si un admin bloquea un horario con reservas
confirmadas, esas reservas se cancelan. El backend las cancela y crea el bloqueo
juntos (o se hacen las dos cosas o ninguna) y registra el evento en el historial
de cada usuario.
