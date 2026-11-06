-- Datos iniciales: los mismos de database/dataset_inicial.csv, ya normalizados.
-- Las fechas son relativas al día en que se carga la base (CURRENT_DATE),
-- para que siempre haya reservas próximas para mostrar.

INSERT INTO usuario (mail, apellido, nombre, claustro, rol) VALUES
    ('juan.perez@estudiantes.unsam.edu.ar',  'Pérez',     'Juan',    'ESTUDIANTE', 'USUARIO'),
    ('lucia.gomez@estudiantes.unsam.edu.ar', 'Gómez',     'Lucía',   'ESTUDIANTE', 'USUARIO'),
    ('mrodriguez@unsam.edu.ar',              'Rodríguez', 'Martín',  'DOCENTE',    'USUARIO'),
    ('sofia.lopez@estudiantes.unsam.edu.ar', 'López',     'Sofía',   'ESTUDIANTE', 'USUARIO'),
    ('dfernandez@unsam.edu.ar',              'Fernández', 'Daniela', 'NO_DOCENTE', 'ADMIN'),
    ('tomas.diaz@estudiantes.unsam.edu.ar',  'Díaz',      'Tomás',   'ESTUDIANTE', 'USUARIO'),
    ('cmartinez@unsam.edu.ar',               'Martínez',  'Carla',   'DOCENTE',    'USUARIO');

INSERT INTO tipo_espacio (nombre, duracion_min, hora_apertura, hora_cierre) VALUES
    ('Cancha fútbol 5', 60,  '09:00', '22:00'),
    ('Cancha fútbol 7', 90,  '09:00', '22:00'),
    ('Parrilla',        240, '10:00', '20:00');

INSERT INTO espacio (nombre, tipo_espacio_id, ubicacion)
SELECT e.nombre, t.id, e.ubicacion
  FROM (VALUES
        ('Cancha 1',   'Cancha fútbol 5', 'Sector deportivo'),
        ('Cancha 2',   'Cancha fútbol 5', 'Sector deportivo'),
        ('Cancha 3',   'Cancha fútbol 7', 'Sector deportivo'),
        ('Parrilla 1', 'Parrilla',        'Quincho norte'),
        ('Parrilla 2', 'Parrilla',        'Quincho norte'),
        ('Parrilla 3', 'Parrilla',        'Patio central')
       ) AS e (nombre, tipo, ubicacion)
  JOIN tipo_espacio t ON t.nombre = e.tipo;

-- dia: días desde hoy. Una fecha + una hora da un TIMESTAMP.
INSERT INTO reserva (usuario_id, espacio_id, inicio, fin, estado, creada_en, cancelada_en)
SELECT u.id,
       e.id,
       CURRENT_DATE + r.dia + r.desde,
       CURRENT_DATE + r.dia + r.hasta,
       r.estado,
       CURRENT_DATE + LEAST(r.dia - 2, -1) + TIME '10:15',
       CASE WHEN r.estado = 'CANCELADA' THEN CURRENT_DATE + TIME '08:30' END
  FROM (VALUES
        ('juan.perez@estudiantes.unsam.edu.ar',   'Cancha 1',    1, TIME '18:00', TIME '19:00', 'CONFIRMADA'),
        ('lucia.gomez@estudiantes.unsam.edu.ar',  'Parrilla 1',  2, TIME '10:00', TIME '14:00', 'CONFIRMADA'),
        ('mrodriguez@unsam.edu.ar',               'Cancha 3',    1, TIME '19:30', TIME '21:00', 'CONFIRMADA'),
        ('sofia.lopez@estudiantes.unsam.edu.ar',  'Parrilla 2',  2, TIME '10:00', TIME '14:00', 'CANCELADA'),
        ('juan.perez@estudiantes.unsam.edu.ar',   'Cancha 2',    3, TIME '18:00', TIME '19:00', 'CONFIRMADA'),
        ('tomas.diaz@estudiantes.unsam.edu.ar',   'Cancha 2',    1, TIME '18:00', TIME '19:00', 'CONFIRMADA'),
        ('juan.perez@estudiantes.unsam.edu.ar',   'Cancha 1',   -3, TIME '18:00', TIME '19:00', 'CONFIRMADA'),
        ('cmartinez@unsam.edu.ar',                'Parrilla 1',  4, TIME '14:00', TIME '18:00', 'CONFIRMADA'),
        ('dfernandez@unsam.edu.ar',               'Cancha 1',    0, TIME '13:00', TIME '14:00', 'CONFIRMADA'),
        ('juan.perez@estudiantes.unsam.edu.ar',   'Cancha 1',    0, TIME '21:00', TIME '22:00', 'CONFIRMADA'),
        ('lucia.gomez@estudiantes.unsam.edu.ar',  'Cancha 2',    0, TIME '17:00', TIME '18:00', 'CONFIRMADA')
       ) AS r (mail, espacio, dia, desde, hasta, estado)
  JOIN usuario u ON u.mail = r.mail
  JOIN espacio e ON e.nombre = r.espacio;

INSERT INTO bloqueo (espacio_id, inicio, fin, motivo, creado_por, creado_en)
SELECT e.id,
       CURRENT_DATE + b.desde_dia + b.desde_hora,
       CURRENT_DATE + b.hasta_dia + b.hasta_hora,
       b.motivo,
       u.id,
       CURRENT_DATE - 2 + TIME '09:00'
  FROM (VALUES
        ('Parrilla 3', -1, TIME '00:00', 10, TIME '00:00', 'Cambio de rejilla'),
        ('Cancha 2',    2, TIME '09:00',  2, TIME '13:00', 'Corte de pasto')
       ) AS b (espacio, desde_dia, desde_hora, hasta_dia, hasta_hora, motivo)
  JOIN espacio e ON e.nombre = b.espacio
  JOIN usuario u ON u.mail = 'dfernandez@unsam.edu.ar';
