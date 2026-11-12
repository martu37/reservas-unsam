# Subirlo a Railway

Para que se pueda probar desde el celular durante la presentación. Son los
mismos cuatro servicios del `docker-compose.yml`, en un proyecto de Railway.

## 1. Crear el proyecto

1. En Railway: **New Project → Deploy from GitHub repo** y elegir
   `reservas-unsam`. Se crea un servicio: será el **backend**.
   En su **Settings → Source → Root Directory** poner `/backend`.
2. **+ New → GitHub Repo**, el mismo repo otra vez: será el **frontend**.
   Root Directory: `/frontend`.
3. **+ New → Database → PostgreSQL**.
4. **+ New → Database → MongoDB**.

Railway encuentra el `Dockerfile` de cada carpeta y construye con él.

## 2. Direcciones públicas

En **Settings → Networking → Generate Domain** de cada uno:

- backend: puerto `8080`
- frontend: puerto `80`

Quedan algo como `https://backend-xxxx.up.railway.app` y
`https://frontend-xxxx.up.railway.app`.

## 3. Variables

En **Variables** de cada servicio (lo que va entre `${{ }}` lo completa Railway
con los datos de las bases; si las bases tienen otro nombre que `Postgres` y
`MongoDB`, cambiarlo):

**backend**

```
SPRING_DATASOURCE_URL=jdbc:postgresql://${{Postgres.PGHOST}}:${{Postgres.PGPORT}}/${{Postgres.PGDATABASE}}
SPRING_DATASOURCE_USERNAME=${{Postgres.PGUSER}}
SPRING_DATASOURCE_PASSWORD=${{Postgres.PGPASSWORD}}
SPRING_DATA_MONGODB_URI=${{MongoDB.MONGO_URL}}/reservas?authSource=admin
HISTORIAL_MOTOR=mongo
CORS_ORIGEN=https://frontend-xxxx.up.railway.app
TZ=America/Argentina/Buenos_Aires
```

**frontend** (se usa al compilar, así que después de cambiarla hay que volver a
desplegar)

```
VITE_API_URL=https://backend-xxxx.up.railway.app
```

## 4. Cargar los datos

Railway no corre los scripts de `database/` como Docker. Se cargan una vez desde
la compu, con `psql` y `mongosh` de los contenedores locales (con
`make db` levantado), en **Git Bash** desde la carpeta del proyecto.

Las direcciones están en **Variables** de cada base: `DATABASE_PUBLIC_URL`
(PostgreSQL) y `MONGO_PUBLIC_URL` (MongoDB). Tienen la contraseña: no
subirlas al repo.

```
PG="pegar DATABASE_PUBLIC_URL"
MONGO="pegar MONGO_PUBLIC_URL"

# La hora argentina, para que CURRENT_DATE y NOW() den el día de acá
docker exec -i reservas-postgres psql "$PG" -c "ALTER DATABASE railway SET timezone TO 'America/Argentina/Buenos_Aires'"

docker exec -i reservas-postgres psql "$PG" < database/modelo_relacional.sql
docker exec -i reservas-postgres psql "$PG" < database/datos_iniciales.sql
docker exec -i reservas-mongo mongosh "$MONGO" < database/scripts_nosql/historial.js
```

## 5. Volver a cargar (la noche antes de presentar)

Las fechas de los datos son relativas al día en que se cargan ("mañana a las
18"). Para que coincidan con el día de la presentación, se borra todo y se
carga de nuevo:

```
docker exec -i reservas-postgres psql "$PG" -c "DROP SCHEMA public CASCADE; CREATE SCHEMA public;"
docker exec -i reservas-mongo mongosh "$MONGO" --eval 'db.getSiblingDB("reservas").dropDatabase()'
```

y después los tres comandos de carga del punto 4.

## Probarlo

Entrar a la dirección del frontend desde el celular, ingresar con un mail de la
UNSAM (si no existe, pide nombre, apellido y claustro) y reservar. Para entrar
como admin: `dfernandez@unsam.edu.ar`.
