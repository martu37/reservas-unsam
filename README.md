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

Hace falta Docker Desktop abierto, Java 21, Node 20 o superior y pnpm.

**1. Las bases** (desde la carpeta del proyecto):

```
docker compose up -d
```

**2. El backend** (en otra terminal), queda en `http://localhost:8080`:

```
cd backend
./gradlew bootRun
```

**3. El frontend** (en otra terminal), queda en `http://localhost:5173`:

```
cd frontend
pnpm install
pnpm dev
```

En la app, desde "Usando como" se cambia de usuario. Daniela Fernández es admin
y ve la sección de bloqueos.

**Si cambiaron los scripts de `database/`:** se cargan solo cuando la base está
vacía, así que hay que borrar los datos y volver a levantar. Ojo, esto borra
también lo que haya en MongoDB.

```
docker compose down -v
docker compose up -d
```

## Estado

- [x] Planilla inicial, normalización a 3FN y DER
- [x] Base PostgreSQL con datos y pruebas de restricciones
- [x] Backend con API y tests
- [x] Frontend conectado al backend
- [ ] Historial en MongoDB y elección del motor por configuración
- [ ] Consulta compleja (agregaciones)
- [ ] Informe
