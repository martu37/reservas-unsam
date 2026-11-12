# Reservas UNSAM

**Reservar una cancha o una parrilla de la universidad sin mandar un mail y
esperar que alguien conteste.**

TP Integrador de Base de Datos (Tecnicatura en Programación Informática, UNSAM).

## El problema

Hoy las canchas de fútbol y las parrillas se piden por mail. Alguien de la
universidad lee el pedido, se fija en una planilla si está libre y contesta.
Puede tardar, dos personas pueden pedir lo mismo, y los pedidos rechazados no
quedan registrados en ningún lado.

Con este sistema cada persona entra con su mail institucional, ve qué turnos
están libres y reserva en el momento. Los admins pueden bloquear un espacio por
mantenimiento.

## Dos bases, cada una para lo que hace mejor

| | PostgreSQL | MongoDB |
|---|---|---|
| **Guarda** | usuarios, espacios, reservas y bloqueos | el historial de cada usuario: reservas, cancelaciones y pedidos rechazados |
| **Por qué** | hay relaciones entre los datos y reglas que no se pueden romper (un turno no se reserva dos veces) | cada evento trae datos distintos y no hace falta una tabla rígida |
| **Detalle** | [documentacion/postgresql.md](documentacion/postgresql.md) | [documentacion/mongodb.md](documentacion/mongodb.md) |

No compiten: **PostgreSQL decide** si un turno está libre y **MongoDB registra**
lo que pasó. El backend tiene una misma interfaz para el historial con las dos
implementaciones ([backend.md](documentacion/backend.md)).

```
frontend (React)
      |
backend (Kotlin + Spring Boot)
      |
 capa de persistencia
   /          \
PostgreSQL   MongoDB
```

## Qué hay en cada carpeta

```
backend/         la API: dominio, services, repositorios y controllers
frontend/        la interfaz para probar el sistema
database/        scripts de las bases y la planilla original
documentacion/   normalización, DER y el detalle de cada parte
```

## Cómo levantarlo

Hace falta Docker Desktop abierto y make. Desde la carpeta del proyecto:

```
make start
```

Construye y levanta todo en Docker (las bases, el backend y el frontend) y
devuelve la terminal. La primera vez tarda unos minutos porque compila.

El `docker-compose.yml` define cuatro servicios, cada uno en su contenedor:

| Servicio | Qué es | Puerto |
|---|---|---|
| `frontend` | la interfaz en React, servida con nginx | `http://localhost:5173` |
| `backend` | la API en Kotlin + Spring Boot | `http://localhost:8080` |
| `postgres` | PostgreSQL 16 con el modelo y los datos de `database/` | `5432` |
| `mongo` | MongoDB 7 | `27017` |

El backend espera a que `postgres` esté listo antes de arrancar y se conecta a
él por el nombre del servicio (`postgres`), no por `localhost`.

| Comando | Qué hace |
|---|---|
| `make start` | levanta todo (si cambió el código, lo recompila) |
| `make restart` | después de editar código: recompila y reinicia backend y frontend, sin tocar las bases |
| `make logs` | muestra los logs de backend y frontend en vivo |
| `make stop` | baja todo |
| `make reset` | borra los datos, vuelve a cargar los scripts de `database/` y levanta todo |
| `make db` | levanta solo las bases, para correr backend y frontend fuera de Docker |

En la app se entra con un mail de la UNSAM (sin contraseña: es una prueba de
concepto). Si el mail no existe, pide nombre, apellido y claustro y lo da de
alta. Para ver la sección de administración: `dfernandez@unsam.edu.ar`.

Para subirlo a internet: [documentacion/railway.md](documentacion/railway.md).

**Si cambiaron los scripts de `database/`:** se cargan solo cuando la base está
vacía, así que hay que correr `make reset`. Ojo, esto borra también lo que haya
en MongoDB.

## Estado

- [x] Planilla inicial, normalización a 3FN y DER
- [x] Base PostgreSQL con datos y pruebas de restricciones
- [x] Backend con API y tests
- [x] Frontend conectado al backend
- [x] Historial en MongoDB y elección del motor por configuración
- [x] Consulta compleja (agregaciones)
- [ ] Informe
