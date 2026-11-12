-- Consulta del punto 7 de la consigna: uso de cada espacio.
-- Es la que muestra la pantalla "Uso de espacios" (la misma está en
-- ReservaRepositoryPostgres.usoPorEspacio).
--
-- Para correrla:
--   docker exec -i reservas-postgres psql -U reservas -d reservas < database/consultas.sql

-- 1) La CTE cuenta las reservas de cada espacio. LEFT JOIN para que aparezcan
--    también los espacios sin reservas (con 0), y SUM(CASE ...) para contar
--    confirmadas y canceladas por separado en el mismo GROUP BY.
-- 2) El SELECT de afuera usa esos totales: el porcentaje de cancelaciones
--    (con CASE para no dividir por cero) y un nivel de demanda.
WITH uso AS (
    SELECT e.nombre                                                 AS espacio,
           t.nombre                                                 AS tipo,
           COUNT(r.id)                                              AS total,
           SUM(CASE WHEN r.estado = 'CONFIRMADA' THEN 1 ELSE 0 END) AS confirmadas,
           SUM(CASE WHEN r.estado = 'CANCELADA'  THEN 1 ELSE 0 END) AS canceladas
      FROM espacio e
      JOIN tipo_espacio t ON t.id = e.tipo_espacio_id
      LEFT JOIN reserva r ON r.espacio_id = e.id
     WHERE e.activo
     GROUP BY e.id, e.nombre, t.nombre
)
SELECT espacio,
       tipo,
       confirmadas,
       canceladas,
       CASE WHEN total = 0 THEN 0
            ELSE ROUND(100.0 * canceladas / total)
       END AS porcentaje_canceladas,
       CASE WHEN confirmadas >= 3 THEN 'Alta'
            WHEN confirmadas >= 1 THEN 'Media'
            ELSE 'Sin uso'
       END AS demanda
  FROM uso
 ORDER BY confirmadas DESC, espacio;
