INSERT INTO Usuario(id, email, password, rol, activo) VALUES
  (null, 'test@unlam.edu.ar', 'test', 'USER', true),
  (null, 'admin@unlam.edu.ar', 'admin', 'ADMIN', true);

INSERT INTO TipoDeAmbiente(id, nombre) VALUES
  (1, 'Living'),
  (2, 'Habitación'),
  (3, 'Pasillo'),
  (4, 'Cocina'),
  (5, 'Comedor');

INSERT INTO CategoriaDeMueble(id, nombre, anchoPromedio, profundidadPromedio) VALUES
  (1, 'Biblioteca', 0.80, 0.35),
  (2, 'Cama doble', 1.40, 1.90),
  (3, 'Cama individual', 0.90, 1.90),
  (4, 'Cama queen', 1.60, 2.00),
  (5, 'Escritorio', 1.20, 0.60),
  (6, 'Mesa de comedor', 1.60, 0.90),
  (7, 'Mesa de luz', 0.50, 0.40),
  (8, 'Mesa ratona', 1.10, 0.60),
  (9, 'Placard', 1.80, 0.60),
  (10, 'Rack de TV', 1.60, 0.40),
  (11, 'Silla', 0.45, 0.50),
  (12, 'Sillón', 0.80, 0.85),
  (13, 'Sofá 3 cuerpos', 2.10, 0.90),
  (14, 'Lampara de pie', 0.40, 0.40),
  (15, 'Lampara de mesa', 0.30, 0.30),
  (16, 'Alacena', 1.20, 0.50),
  (17, 'Zapatero', 1.00, 0.30);

INSERT INTO Material(id, nombre, sostenible) VALUES
  (1, 'Madera certificada', true),
  (2, 'Madera reciclada', true),
  (3, 'Bambú', true),
  (4, 'Madera común', false),
  (5, 'Metal', false),
  (6, 'Melamina', false),
  (7, 'Plástico', false);

-- Hacer datos mas realistas... Scraping de muebles
INSERT INTO Mueble(id, nombre, descripcion, precio, ancho, largo, estilo, material_id, categoria_de_mueble_id, enCatalogo) VALUES
  (1, 'Sillón Retró', 'Sillón de voluteadas tapizado en terciopelo color mostaza, con patas de madera maciza.', 85000.0, 85.0, 80.0, 'Retro', 4, 12, true),
  (2, 'Lámpara de Pie Retro', 'Lámpara de pie con pantalla de tela y base de metal esmaltado, para rincón de lectura.', 25000.0, 40.0, 40.0, 'Retro', 5, 14, true),
  (3, 'Escritorio Minimalista', 'Escritorio de melamina clara con cajones y un solo cajón para documentos.', 120000.0, 120.0, 60.0, 'Minimalista', 6, 5, true),
  (4, 'Biblioteca Minimalista', 'Biblioteca abierta de cinco estantes en madera clara, ideal para pocos libros.', 95000.0, 80.0, 30.0, 'Minimalista', 1, 1, true),
  (5, 'Mesa de Comedor Japandi', 'Mesa de comedor con tablero de madera maciza y patas cónicas de línea simple.', 150000.0, 160.0, 90.0, 'Japandi', 1, 6, true),
  (6, 'Sillón Japandi', 'Sillón bajo con cojines de lino natural y estructura de madera clara.', 60000.0, 90.0, 80.0, 'Japandi', 1, 12, true),
  (7, 'Estantería Industrial', 'Estantería de hierro y madera con estantes reforzados, estilo fábrica.', 75000.0, 100.0, 35.0, 'Industrial', 2, 1, true),
  (8, 'Cama Boho', 'Cama doble con dosel de telas livianas y estructura de madera clara.', 180000.0, 140.0, 190.0, 'Boho', 1, 2, true),
  (9, 'Escritorio Escandinavo', 'Escritorio de líneas escandinavas, con terminación clara y tiradores redondos.', 110000.0, 120.0, 60.0, 'Escandinavo', 1, 5, true),
  (10, 'Silla de Comedor Retro', 'Silla de comedor con estructura de madera curvada y asiento tapizado en tela afrutada.', 35000.0, 45.0, 50.0, 'Retro', 4, 11, true),
  (11, 'Mesada de Cocina Minimalista', 'Mesada de cocina con cajones ocultos y superficie continua sin tiradores.', 88000.0, 180.0, 60.0, 'Minimalista', 6, 7, true),
  (12, 'Mesa de Comedor Industrial', 'Mesa de comedor con estructura de hierro negro y tablero de madera rústica.', 135000.0, 180.0, 90.0, 'Industrial', 2, 6, true),
  (13, 'Cama Escandinava', 'Cama de madera clara con cabecero de listones y patas afinadas.', 165000.0, 140.0, 190.0, 'Escandinavo', 1, 2, true),
  (14, 'Butaca Boho', 'Butaca con flecos de algodón, cojines estampados y patas de madera tallada.', 48000.0, 75.0, 75.0, 'Boho', 4, 12, true),
  (15, 'Lámpara de Mesa Japandi', 'Lámpara de mesa con pantalla de papel de arroz y base de cerámica mate.', 18000.0, 30.0, 30.0, 'Japandi', 3, 15, true),
  (16, 'Zapatero Escandinavo', 'Zapatero bajo de madera clara con cajón y patas cónicas, ideal para pasillo.', 52000.0, 80.0, 30.0, 'Escandinavo', 1, 17, true),
  (17, 'Escritorio Boho', 'Escritorio de madera con detalle trenzado en las laterales y cajón espacioso.', 98000.0, 110.0, 55.0, 'Boho', 3, 17, true),
  (18, 'Alacena Industrial', 'Alacena cerrada de hierro y madera, ideal para ordenar la cocina.', 70000.0, 90.0, 40.0, 'Industrial', 5, 16, true);

INSERT INTO mueble_tipos_de_ambiente(mueble_id, tipo_de_ambiente_id) VALUES
  (1, 1),
  (2, 1),
  (2, 3),
  (3, 2),
  (3, 1),
  (4, 1),
  (4, 3),
  (5, 5),
  (6, 1),
  (7, 3),
  (7, 4),
  (8, 2),
  (9, 2),
  (9, 1),
  (10, 5),
  (11, 4),
  (12, 5),
  (13, 2),
  (14, 1),
  (14, 2),
  (15, 1),
  (15, 2),
  (16, 3),
  (17, 1),
  (17, 2),
  (18, 4);

INSERT INTO Ambiente(id, ancho, largo, tipo_de_ambiente_id, usuario_id) VALUES
  (1, 4.0, 3.0, 2, 1),
  (2, 5.0, 3.0, 4, 1);

INSERT INTO ambiente_muebles(ambiente_id, mueble_id) VALUES
  (1, 8),
  (1, 3),
  (1, 15),
  (2, 11),
  (2, 18),
  (2, 16);
