# PostgreSQL: la base de las reservas

Es la **fuente de verdad**: si una reserva está acá, está confirmada. Guarda
todo lo que necesita reglas firmes y relaciones entre datos.

## Tablas

| tabla | qué guarda |
|---|---|
| `usuario` | quién reserva, identificado por el mail de la universidad. Tiene un `rol` (usuario o admin) |
| `tipo_espacio` | cancha de fútbol 5, de fútbol 7 o parrilla, con la duración del turno y el horario |
| `espacio` | cada cancha o parrilla concreta (Cancha 1, Parrilla 2...) y de qué tipo es |
| `reserva` | quién reservó qué espacio, de qué hora a qué hora, y si está confirmada o cancelada |
| `bloqueo` | un espacio fuera de servicio por mantenimiento, con el motivo y el admin que lo cargó |
| `historial_evento` | respaldo del historial de MongoDB (ver [mongodb.md](mongodb.md)) |

Además hay una vista, `reserva_detalle`, que junta cada reserva con su usuario,
su espacio y su tipo (JOIN de 4 tablas).

## Las reglas más importantes

- **Un turno no se reserva dos veces.** Lo controla el índice único
  `uq_reserva_turno`: no puede haber dos reservas confirmadas del mismo espacio
  que empiecen a la misma hora. Las canceladas no cuentan.
- **Una persona no está en dos lugares a la vez.** Con la misma idea,
  `uq_reserva_usuario_turno` no deja que alguien tenga dos reservas confirmadas
  que empiecen a la misma hora (por ejemplo Cancha 1 y Cancha 2 a las 18).
- **No se borra lo que tiene historia.** Un usuario o un espacio con reservas no
  se puede borrar (`ON DELETE RESTRICT`). Si se borra un espacio sin reservas,
  sus bloqueos se borran con él (`ON DELETE CASCADE`).
- **Los datos tienen que tener sentido.** Con `CHECK`: el fin es posterior al
  inicio, los estados y claustros son los permitidos, el mail es de la UNSAM.

La lista completa, y qué reglas controla el backend en lugar de la base, está en
el [DER](DER.md#restricciones-que-no-se-ven-en-el-diagrama).

## Para ver el detalle

| quiero ver... | dónde |
|---|---|
| cómo pasamos de la planilla de mails a las tablas (1FN, 2FN, 3FN) | [normalizacion.md](normalizacion.md) |
| el diagrama entidad-relación y las cardinalidades | [DER.md](DER.md) |
| el script que crea la base | [`database/modelo_relacional.sql`](../database/modelo_relacional.sql) |
| los datos de prueba | [`database/datos_iniciales.sql`](../database/datos_iniciales.sql) |
| la planilla original | [`database/dataset_inicial.csv`](../database/dataset_inicial.csv) |
| la consulta de agregación (uso de cada espacio) | [`database/consultas.sql`](../database/consultas.sql) |
| que las restricciones funcionan | [`database/pruebas_restricciones.sql`](../database/pruebas_restricciones.sql) y su [salida](pruebas_restricciones_salida.txt) |

Para correr las pruebas de las restricciones:

```
docker exec -i reservas-postgres sh -c "psql -U reservas -d reservas 2>&1" < database/pruebas_restricciones.sql
```

## Mirar la base a mano

Con las bases levantadas (`docker compose up -d`):

```
docker exec -it reservas-postgres psql -U reservas -d reservas
```

Algunos comandos para empezar:

```sql
\dt                                   -- lista las tablas
\d reserva                            -- columnas y restricciones de una tabla
SELECT * FROM reserva_detalle;        -- las reservas con todos sus datos
SELECT * FROM bloqueo;
\q                                    -- salir
```
