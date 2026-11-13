# Análisis del dataset y normalización

## 1. De dónde sale el dataset

Hoy las canchas y las parrillas se piden por mail y una persona del área responde.
Para registrar los pedidos se usa una planilla. No tenemos la planilla real, así
que armamos una con datos ficticios que respeta cómo funciona el proceso:
[`database/dataset_inicial.csv`](../database/dataset_inicial.csv) (18 pedidos).

Cada fila es un pedido que llegó por mail:

| columna | ejemplo |
|---|---|
| fecha_pedido | 2026-11-02 |
| mail | juan.perez@estudiantes.unsam.edu.ar |
| nombre_completo | Pérez, Juan |
| claustro | Estudiante |
| espacio | Cancha 1 |
| tipo_espacio | Cancha fútbol 5 |
| ubicacion | Sector deportivo |
| duracion_turno | 60 min |
| horario_tipo | 09:00-22:00 |
| fecha_reserva | 2026-11-05 |
| horario | 18:00-19:00 |
| estado | Confirmada / Rechazada (ocupado) / Cancelada |
| respondido_por | Ana (Deportes) |
| observaciones | texto libre |

## 2. Análisis inicial

**Datos repetidos.** El nombre y el claustro de una persona se repiten en cada
pedido que hace (Juan Pérez aparece 4 veces). La duración del turno y el horario
de apertura se repiten en cada fila del mismo tipo de espacio.

**Problemas que ya aparecen en la planilla:**

- **Actualización.** Lucía figura como "Gómez, Lucía" y como "Gomez, Lucia"; Juan
  como "Pérez, Juan" y "Perez, Juan". Es la misma persona escrita distinto. Si
  cambia el horario de las parrillas hay que corregir todas las filas de
  parrillas, y si se olvida alguna la planilla queda contradictoria.
- **Inserción.** No hay forma de cargar un espacio nuevo (por ejemplo una Cancha
  4) o una persona hasta que exista un pedido. Tampoco hay lugar para decir que
  un espacio está en mantenimiento: hoy queda como texto en observaciones
  ("parrilla 3 en arreglo hasta el 20/11").
- **Eliminación.** La Cancha 3 solo aparece en los pedidos de Martín. Si se borran
  esos pedidos se pierde que existe una cancha de fútbol 7 con turnos de 90 min.

**Otras cosas que vemos:**

- Hay valores que no son atómicos: `nombre_completo`, `horario` ("18:00-19:00"),
  `horario_tipo` ("09:00-22:00") y `duracion_turno` ("60 min", número con texto).
- El estado mezcla dos datos: el estado y el motivo ("Rechazada (ocupado)").
- Los pedidos rechazados no son reservas: quedan en la planilla solo porque es el
  único registro que hay.
- Nada impide cargar dos reservas confirmadas en el mismo espacio y horario; lo
  controla la persona que responde, a mano.

## 3. Dependencias funcionales

Tomamos como clave de cada pedido **(mail, espacio, fecha_reserva, hora_inicio)**:
una persona no pide dos veces el mismo espacio para el mismo turno.

- DF1: `mail → apellido, nombre, claustro`
- DF2: `espacio → tipo_espacio, ubicacion`
- DF3: `tipo_espacio → duracion_min, hora_apertura, hora_cierre`
- DF4: `(mail, espacio, fecha_reserva, hora_inicio) → fecha_pedido, hora_fin,
  estado, motivo, respondido_por, observaciones`

## 4. Primera forma normal (1FN)

Separamos los valores compuestos para que cada celda tenga un solo dato:

- `nombre_completo` → `apellido`, `nombre`
- `horario` → `hora_inicio`, `hora_fin`
- `horario_tipo` → `hora_apertura`, `hora_cierre`
- `duracion_turno` → `duracion_min` (entero)
- `estado` → `estado` (confirmada / rechazada / cancelada) y `motivo`

Queda una sola tabla:

**PEDIDO**(<u>mail</u>, apellido, nombre, claustro, <u>espacio</u>, tipo_espacio,
ubicacion, duracion_min, hora_apertura, hora_cierre, <u>fecha_reserva</u>,
<u>hora_inicio</u>, hora_fin, fecha_pedido, estado, motivo, respondido_por,
observaciones)

Está en 1FN pero tiene todos los problemas del punto 2.

## 5. Segunda forma normal (2FN)

DF1 y DF2 dependen de una parte de la clave, no de la clave entera: el nombre
depende solo del mail y la ubicación depende solo del espacio. Sacamos esas
columnas a tablas propias:

- **USUARIO**(<u>mail</u>, apellido, nombre, claustro)
- **ESPACIO**(<u>espacio</u>, tipo_espacio, ubicacion, duracion_min,
  hora_apertura, hora_cierre)
- **PEDIDO**(<u>mail</u>, <u>espacio</u>, <u>fecha_reserva</u>, <u>hora_inicio</u>,
  hora_fin, fecha_pedido, estado, motivo, respondido_por, observaciones)

Con esto el nombre de Lucía se escribe una sola vez y se puede cargar una cancha
sin pedidos.

## 6. Tercera forma normal (3FN)

En ESPACIO queda una dependencia transitiva (DF3):
`espacio → tipo_espacio → duracion_min, hora_apertura, hora_cierre`.
La duración y el horario son del tipo, no de cada espacio. La separamos:

- **TIPO_ESPACIO**(<u>tipo_espacio</u>, duracion_min, hora_apertura, hora_cierre)
- **ESPACIO**(<u>espacio</u>, tipo_espacio, ubicacion)

Ahora cambiar el horario de las parrillas es modificar una sola fila.

## 7. Del modelo normalizado al modelo final

Además de normalizar, ajustamos el modelo al sistema nuevo:

1. **Claves.** Usamos un `id` numérico como clave primaria en todas las tablas.
   El mail del usuario y el nombre del espacio y del tipo quedan como `UNIQUE`.
   Así las claves foráneas son simples y, si cambia un nombre, no hay que tocar
   otras tablas.
2. **Fecha y hora juntas.** `fecha_reserva` + `hora_inicio` / `hora_fin` pasan a
   `inicio` y `fin` (fecha y hora). Así se comparan horarios directamente (por
   ejemplo, si una reserva cae dentro de un bloqueo) y el índice único puede
   mirar el `inicio` de cada turno.
3. **`fin` se guarda aunque se pueda calcular** (inicio + duración del tipo). Lo
   guardamos porque si mañana cambia la duración de un tipo, las reservas viejas
   tienen que conservar su horario original. Además, para saber si una reserva
   cae dentro de un bloqueo se compara su inicio y su fin con los del bloqueo.
4. **Los rechazos no son reservas.** En el sistema nuevo un pedido sobre un turno
   ocupado se rechaza en el momento y no se guarda como reserva. El intento queda
   en el historial de eventos (MongoDB). Por eso RESERVA solo tiene los estados
   `confirmada` y `cancelada`, y `motivo` desaparece.
5. **El mantenimiento pasa a ser un dato.** Lo que hoy es texto en observaciones
   ("en arreglo hasta el 20/11") pasa a la tabla **BLOQUEO**, con espacio, desde,
   hasta y motivo. Mientras haya un bloqueo, el espacio no se puede reservar.
6. **`respondido_por` desaparece.** Nadie confirma a mano. Las personas que
   administran el sistema son usuarios con `rol = 'admin'` y quedan registradas
   como autoras de cada bloqueo.
7. **`observaciones` y `fecha_pedido`.** `fecha_pedido` pasa a `creada_en`. Las
   observaciones libres no se guardan en la reserva; si hacen falta, van al
   historial.

## 8. Modelo final (3FN)

- **usuario**(<u>id</u>, mail UNIQUE, apellido, nombre, claustro, rol, activo)
- **tipo_espacio**(<u>id</u>, nombre UNIQUE, duracion_min, hora_apertura,
  hora_cierre)
- **espacio**(<u>id</u>, nombre UNIQUE, tipo_espacio_id → tipo_espacio, ubicacion,
  activo)
- **reserva**(<u>id</u>, usuario_id → usuario, espacio_id → espacio, inicio, fin,
  estado, creada_en, cancelada_en)
- **bloqueo**(<u>id</u>, espacio_id → espacio, inicio, fin, motivo,
  creado_por → usuario, creado_en)

El diagrama está en [`DER.md`](DER.md).
