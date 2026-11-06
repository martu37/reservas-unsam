-- Pruebas de las restricciones del modelo.
-- Cada prueba intenta algo y muestra si la base lo acepta o lo rechaza.
-- Todo corre en una transacción que al final se deshace: no modifica los datos.
--
-- Cómo correrlo (con las bases levantadas):
--   docker exec -i reservas-postgres psql -U reservas -d reservas < database/pruebas_restricciones.sql

\set VERBOSITY terse
BEGIN;

\echo
\echo '== RESERVAS =='

\echo '1. Reservar un turno ya reservado (Cancha 1, mañana 18 a 19). Esperado: ERROR ex_reserva_superpuesta'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES ((SELECT id FROM usuario WHERE mail = 'lucia.gomez@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 1 + TIME '18:00', CURRENT_DATE + 1 + TIME '19:00');
ROLLBACK TO SAVEPOINT p;

\echo '2. Superposición parcial (Cancha 3 está de 19:30 a 21; pido de 20 a 21:30). Esperado: ERROR ex_reserva_superpuesta'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES ((SELECT id FROM usuario WHERE mail = 'tomas.diaz@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Cancha 3'),
        CURRENT_DATE + 1 + TIME '20:00', CURRENT_DATE + 1 + TIME '21:30');
ROLLBACK TO SAVEPOINT p;

\echo '3. El turno siguiente (Cancha 1, mañana 19 a 20) no se pisa. Esperado: INSERT 0 1'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES ((SELECT id FROM usuario WHERE mail = 'lucia.gomez@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 1 + TIME '19:00', CURRENT_DATE + 1 + TIME '20:00');
ROLLBACK TO SAVEPOINT p;

\echo '4. Una reserva cancelada libera el turno (Parrilla 2, pasado mañana 10 a 14). Esperado: INSERT 0 1'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES ((SELECT id FROM usuario WHERE mail = 'tomas.diaz@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Parrilla 2'),
        CURRENT_DATE + 2 + TIME '10:00', CURRENT_DATE + 2 + TIME '14:00');
ROLLBACK TO SAVEPOINT p;

\echo '5. Reservar un espacio bloqueado (Parrilla 3). Esperado: ERROR El espacio está bloqueado'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES ((SELECT id FROM usuario WHERE mail = 'tomas.diaz@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Parrilla 3'),
        CURRENT_DATE + 3 + TIME '10:00', CURRENT_DATE + 3 + TIME '14:00');
ROLLBACK TO SAVEPOINT p;

\echo '6. Una reserva de 30 minutos en una cancha de turnos de 60. Esperado: ERROR Un turno de Cancha fútbol 5 dura 60 minutos'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES ((SELECT id FROM usuario WHERE mail = 'tomas.diaz@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 5 + TIME '18:00', CURRENT_DATE + 5 + TIME '18:30');
ROLLBACK TO SAVEPOINT p;

\echo '7. Una reserva después del cierre (22 a 23). Esperado: ERROR fuera del horario'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES ((SELECT id FROM usuario WHERE mail = 'tomas.diaz@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 5 + TIME '22:00', CURRENT_DATE + 5 + TIME '23:00');
ROLLBACK TO SAVEPOINT p;

\echo '8. Una reserva que termina antes de empezar. Esperado: ERROR ck_reserva_fin'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin, estado, cancelada_en)
VALUES ((SELECT id FROM usuario WHERE mail = 'tomas.diaz@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 5 + TIME '19:00', CURRENT_DATE + 5 + TIME '18:00', 'CANCELADA', LOCALTIMESTAMP);
ROLLBACK TO SAVEPOINT p;

\echo '9. Un estado que no existe. Esperado: ERROR ck_reserva_estado'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin, estado)
VALUES ((SELECT id FROM usuario WHERE mail = 'tomas.diaz@estudiantes.unsam.edu.ar'),
        (SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 5 + TIME '18:00', CURRENT_DATE + 5 + TIME '19:00', 'PENDIENTE');
ROLLBACK TO SAVEPOINT p;

\echo '10. Una reserva cancelada sin fecha de cancelación. Esperado: ERROR ck_reserva_cancelada'
SAVEPOINT p;
UPDATE reserva SET estado = 'CANCELADA'
 WHERE espacio_id = (SELECT id FROM espacio WHERE nombre = 'Cancha 1')
   AND inicio = CURRENT_DATE + 1 + TIME '18:00';
ROLLBACK TO SAVEPOINT p;

\echo '11. Una reserva de un usuario que no existe. Esperado: ERROR fk_reserva_usuario'
SAVEPOINT p;
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin)
VALUES (9999, (SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 6 + TIME '10:00', CURRENT_DATE + 6 + TIME '11:00');
ROLLBACK TO SAVEPOINT p;

\echo '12. Borrar un espacio que tiene reservas. Esperado: ERROR fk_reserva_espacio'
SAVEPOINT p;
DELETE FROM espacio WHERE nombre = 'Cancha 1';
ROLLBACK TO SAVEPOINT p;

\echo
\echo '== USUARIOS =='

\echo '13. Un mail que no es institucional. Esperado: ERROR ck_usuario_mail_institucional'
SAVEPOINT p;
INSERT INTO usuario (mail, apellido, nombre, claustro) VALUES ('juanperez@gmail.com', 'Pérez', 'Juan', 'ESTUDIANTE');
ROLLBACK TO SAVEPOINT p;

\echo '14. Un mail que ya está registrado. Esperado: ERROR uq_usuario_mail'
SAVEPOINT p;
INSERT INTO usuario (mail, apellido, nombre, claustro) VALUES ('juan.perez@estudiantes.unsam.edu.ar', 'Pérez', 'Juan', 'ESTUDIANTE');
ROLLBACK TO SAVEPOINT p;

\echo
\echo '== BLOQUEOS =='

\echo '15. Un bloqueo cargado por alguien que no es admin. Esperado: ERROR Solo un administrador'
SAVEPOINT p;
INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por)
VALUES ((SELECT id FROM espacio WHERE nombre = 'Cancha 3'),
        CURRENT_DATE + 6 + TIME '09:00', CURRENT_DATE + 6 + TIME '12:00', 'Arreglo de red',
        (SELECT id FROM usuario WHERE mail = 'juan.perez@estudiantes.unsam.edu.ar'));
ROLLBACK TO SAVEPOINT p;

\echo '16. Bloquear un horario con reservas confirmadas sin cancelarlas antes. Esperado: ERROR Hay reservas confirmadas'
SAVEPOINT p;
INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por)
VALUES ((SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 1 + TIME '17:00', CURRENT_DATE + 1 + TIME '20:00', 'Arco roto',
        (SELECT id FROM usuario WHERE mail = 'dfernandez@unsam.edu.ar'));
ROLLBACK TO SAVEPOINT p;

\echo '17. Lo que va a hacer el backend: cancelar las reservas afectadas y bloquear, en la misma transacción. Esperado: UPDATE 1 e INSERT 0 1'
SAVEPOINT p;
UPDATE reserva SET estado = 'CANCELADA', cancelada_en = LOCALTIMESTAMP
 WHERE espacio_id = (SELECT id FROM espacio WHERE nombre = 'Cancha 1')
   AND estado = 'CONFIRMADA'
   AND inicio > LOCALTIMESTAMP
   AND tsrange(inicio, fin) && tsrange(CURRENT_DATE + 1 + TIME '17:00', CURRENT_DATE + 1 + TIME '20:00')
RETURNING id, usuario_id, inicio, estado;
INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por)
VALUES ((SELECT id FROM espacio WHERE nombre = 'Cancha 1'),
        CURRENT_DATE + 1 + TIME '17:00', CURRENT_DATE + 1 + TIME '20:00', 'Arco roto',
        (SELECT id FROM usuario WHERE mail = 'dfernandez@unsam.edu.ar'));
ROLLBACK TO SAVEPOINT p;

\echo '18. Dos bloqueos superpuestos en el mismo espacio. Esperado: ERROR ex_bloqueo_superpuesto'
SAVEPOINT p;
INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por)
VALUES ((SELECT id FROM espacio WHERE nombre = 'Parrilla 3'),
        CURRENT_DATE + 5 + TIME '10:00', CURRENT_DATE + 5 + TIME '14:00', 'Pintura',
        (SELECT id FROM usuario WHERE mail = 'dfernandez@unsam.edu.ar'));
ROLLBACK TO SAVEPOINT p;

\echo
\echo '== CONSULTA CON INFORMACIÓN RELACIONADA =='
\echo '19. Las reservas de Juan, con espacio y tipo (vista reserva_detalle, JOIN de 4 tablas)'
SELECT espacio_nombre, tipo_espacio, to_char(inicio, 'DD/MM HH24:MI') AS inicio,
       to_char(fin, 'HH24:MI') AS fin, estado
  FROM reserva_detalle
 WHERE usuario_mail = 'juan.perez@estudiantes.unsam.edu.ar'
 ORDER BY inicio;

ROLLBACK;
